package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.AgentRegisterRequest;
import com.company.pcmgmt.api.dto.request.AgentReportRequest;
import com.company.pcmgmt.domain.entity.Agent;
import com.company.pcmgmt.domain.entity.Employee;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.entity.PcHardwareInfo;
import com.company.pcmgmt.domain.entity.RentalVendor;
import com.company.pcmgmt.domain.mapper.AgentMapper;
import com.company.pcmgmt.domain.mapper.EmployeeMapper;
import com.company.pcmgmt.domain.mapper.RentalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.UUID;

/**
 * エージェント情報収集サービス
 *
 * <p>Tauri エージェントから受信したハードウェア・ソフトウェア・ネットワーク情報を
 * データベースに保存する処理を担当する。</p>
 *
 * <p>V4 変更: ハードウェア・ソフトウェア・ネットワーク情報のリレーションを
 * {@code pc_assets.id} ベースから {@code agents.agent_number} ベースに変更。
 * PC資産の更新 ({@code updateAgentLastSeen} / {@code updateAcquisitionType}) の
 * WHERE 条件も {@code agent_number} に変更。</p>
 *
 * <p>処理フロー（report）:
 * <ol>
 *   <li>エージェント番号の存在確認 → isNew フラグを設定</li>
 *   <li>isNew の場合: agents テーブルに INSERT（初回）、isNew でない場合: UPDATE（2回目以降）</li>
 *   <li>エージェント番号でPC資産を検索 → 未設定ならホスト名で検索して紐付け</li>
 *   <li>ハードウェア情報を upsert（初回: INSERT、2回目以降: UPDATE）</li>
 *   <li>ソフトウェア情報を全削除→再登録（DELETE + INSERT）</li>
 *   <li>ネットワーク情報を全削除→再登録（DELETE + INSERT）</li>
 *   <li>エージェント最終接続日時・設置場所・使用者を更新（agent_number で特定）</li>
 *   <li>取得区分を更新（エージェントが選択した場合のみ、agent_number で特定）</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    /** エージェント関連の DB アクセスを担うマッパー */
    private final AgentMapper agentMapper;

    /** 社員マスタの DB アクセスを担うマッパー（未登録ユーザーの自動登録に使用） */
    private final EmployeeMapper employeeMapper;

    /** レンタル契約・ベンダーの DB アクセスを担うマッパー（レンタル自動登録に使用） */
    private final RentalMapper rentalMapper;

    /**
     * エージェント報告で使用者名が employees テーブルに存在しない場合に設定する
     * ダミー社員コード（仮登録を示すプレースホルダー値）
     */
    private static final String DUMMY_EMPLOYEE_CODE = "dummyCode";

    // =========================================================
    // 資産情報取得（エージェント起動時）
    // =========================================================

    /**
     * エージェント番号に対応するPC資産の取得区分を返す
     *
     * <p>エージェントアプリ起動時に呼び出され、バックエンドに既に取得区分が
     * 設定されているかを確認するために使用する。
     * 設定済みの場合はエージェント側の選択欄を読み取り専用にする。</p>
     *
     * <p>V4 変更: ホスト名による検索からエージェント番号による検索に変更。
     * エージェント番号は管理者画面・DB の値と直接対応するため、
     * ホスト名の不一致による検索漏れが発生しない。</p>
     *
     * @param agentNumber エージェント番号（例: "AGT-A1B2C3D4"）
     * @return 取得区分（"PURCHASE" / "RENTAL"）、未登録・未設定の場合は null
     */
    /**
     * エージェント番号に対応するPC資産の取得区分を返す
     *
     * <p>検索順序:
     * <ol>
     *   <li>エージェント番号で検索（{@code pc_assets.agent_number} が設定済みの場合）</li>
     *   <li>ホスト名でフォールバック検索（管理者が先に資産を登録した場合など、
     *       {@code pc_assets.agent_number} が未紐付けでもホスト名が一致すれば取得できる）</li>
     * </ol>
     * </p>
     *
     * <p>自動登録時に設定したプレースホルダー値 "UNKNOWN" は未設定扱いとして null を返す。
     * これにより、エージェント側の取得区分ドロップダウンが有効のまま維持される。</p>
     *
     * @param agentNumber エージェント番号（例: "AGT-A1B2C3D4"）
     * @param hostname    フォールバック検索用のホスト名（null 可）
     * @return 取得区分（"PURCHASE" / "RENTAL"）、未登録・未設定・UNKNOWN の場合は null
     */
    public String getAcquisitionType(String agentNumber, String hostname) {
        PcAsset asset = null;

        // ① エージェント番号で検索（pc_assets.agent_number が設定済みの場合）
        if (agentNumber != null && !agentNumber.isBlank()) {
            asset = agentMapper.findAssetByAgentNumber(agentNumber);
        }

        // ② ホスト名でフォールバック検索（管理者が先に資産登録した場合など、agent_number 未紐付け時）
        if (asset == null && hostname != null && !hostname.isBlank()) {
            asset = agentMapper.findAssetByHostname(hostname);
            log.debug("取得区分: エージェント番号では見つからず、ホスト名で検索しました: hostname={}", hostname);
        }

        if (asset == null) {
            return null;
        }

        // 自動登録時に設定したプレースホルダー値 "UNKNOWN" は未設定扱いとして null を返す。
        // これにより、エージェント側の取得区分ドロップダウンが有効のまま維持される。
        String acquisitionType = asset.getAcquisitionType();
        return "UNKNOWN".equals(acquisitionType) ? null : acquisitionType;
    }

    // =========================================================
    // エージェント登録（初回起動時）
    // =========================================================

    /**
     * エージェントを新規登録してエージェント番号を返す
     *
     * <p>エージェントアプリの初回起動時（ローカルにエージェント番号が存在しない場合）に呼び出される。
     * UUID の先頭 8 文字を大文字変換して "AGT-XXXXXXXX" 形式のエージェント番号を生成する。</p>
     *
     * @param req エージェント初回登録リクエスト（ホスト名を含む）
     * @return 生成したエージェント番号（例: "AGT-A1B2C3D4"）
     */
    @Transactional
    public String register(AgentRegisterRequest req) {
        // UUID の先頭 8 文字（ハイフン除去後）を大文字で使用して一意なエージェント番号を生成
        String uuid8 = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String agentNumber = "AGT-" + uuid8;

        // agents テーブルに新規登録
        agentMapper.insertAgent(agentNumber, req.getHostname());

        log.info("エージェント新規登録: agentNumber={}, hostname={}", agentNumber, req.getHostname());
        return agentNumber;
    }

    // =========================================================
    // エージェント報告処理（定期送信 / 手動送信）
    // =========================================================

    /**
     * エージェントからの報告を処理してDBに保存する
     *
     * <p>処理の先頭でエージェント番号の存在有無を確認し {@code isNew} フラグを設定する。
     * 以降の agents テーブル操作はすべてこのフラグで INSERT / UPDATE を判断する。</p>
     *
     * <p>V4 変更: PC資産の特定に {@code agent_number} を優先して使用する。
     * 初回レポート時（{@code pc_assets.agent_number} 未設定）はホスト名で検索し、
     * 紐付け後は以後 agent_number で検索できるようにする。</p>
     *
     * @param req エージェント報告リクエスト（ハードウェア・ソフトウェア・ネットワーク情報）
     * @return 処理結果コード
     *         <ul>
     *           <li>"HOSTNAME_MISSING": ホスト名が未指定</li>
     *           <li>"ASSET_NOT_FOUND": 対応するPC資産が見つからない</li>
     *           <li>"OK": 正常処理完了</li>
     *         </ul>
     */
    @Transactional
    public String processReport(AgentReportRequest req) {
        // ホスト名が未指定の場合はエラーコードを返す
        if (req.getHostname() == null || req.getHostname().isBlank()) {
            return "HOSTNAME_MISSING";
        }

        // ---- ① エージェント番号の存在確認 → isNew フラグを設定 ----
        String agentNumber = req.getAgentNumber();
        boolean isNew = false;

        if (agentNumber != null && !agentNumber.isBlank()) {
            Agent existingAgent = agentMapper.findAgentByNumber(agentNumber);
            if (existingAgent == null) {
                isNew = true;
                agentMapper.insertAgent(agentNumber, req.getHostname());
                log.info("エージェント初回報告（INSERT）: agentNumber={}, hostname={}", agentNumber, req.getHostname());
            } else {
                agentMapper.updateAgent(agentNumber);
                log.debug("エージェント更新（UPDATE）: agentNumber={}", agentNumber);
            }
        } else {
            log.debug("エージェント番号なし: hostname={}", req.getHostname());
        }

        // ---- ② PC資産の特定（agent_number 優先 → hostname フォールバック）----
        // V4: エージェント番号でまず検索し、未設定の場合はホスト名で初回紐付けを行う
        PcAsset asset = null;

        if (agentNumber != null && !agentNumber.isBlank()) {
            // エージェント番号で検索（2回目以降のレポートはこちらがヒットする）
            asset = agentMapper.findAssetByAgentNumber(agentNumber);
        }

        if (asset == null) {
            // ホスト名でフォールバック検索（初回レポートまたはエージェント番号未設定時）
            asset = agentMapper.findAssetByHostname(req.getHostname());
            if (asset == null) {
                // ホスト名でも見つからない場合 → PC資産を自動登録して処理を継続する
                log.info("エージェント報告: ホスト名 '{}' に対応するPC資産が見つかりません。自動登録します。",
                    req.getHostname());
                asset = autoRegisterAsset(req, agentNumber);
                // 自動登録時は agent_number をINSERT時に設定済みのため linkAgentNumberToAsset は不要
            } else {
                // 初回紐付け: pc_assets.agent_number を設定して次回から agent_number で検索できるようにする
                if (agentNumber != null && !agentNumber.isBlank()) {
                    agentMapper.linkAgentNumberToAsset(agentNumber, req.getHostname());
                    log.info("PC資産にエージェント番号を紐付け: agentNumber={}, hostname={}",
                        agentNumber, req.getHostname());
                }
            }
        }

        // ---- ③④⑤ ハードウェア・ソフトウェア・ネットワーク情報の保存（agent_number が必要）----
        // エージェント番号が存在する場合のみ保存する（V4: agent_number が PK の代わり）
        if (agentNumber != null && !agentNumber.isBlank()) {

            // ---- ③ ハードウェア情報を upsert（存在すれば UPDATE、なければ INSERT）----
            PcHardwareInfo existingHw = agentMapper.findHardwareByAgentNumber(agentNumber);
            AgentReportRequest.Hardware hw_dto = req.getHardware();
            PcHardwareInfo hw = new PcHardwareInfo();
            hw.setAgentNumber(agentNumber);                                         // V4: agent_number で識別
            hw.setCpuModel (hw_dto != null ? hw_dto.getCpuModel()   : null);       // CPU モデル名
            hw.setCpuCores (hw_dto != null ? hw_dto.getCpuCores()   : null);       // CPU コア数
            hw.setMemoryGb (hw_dto != null ? hw_dto.getMemoryGb()   : null);       // メモリ容量(GB)
            hw.setDiskGb   (hw_dto != null ? hw_dto.getDiskGb()     : null);       // ディスク容量(GB)
            hw.setDiskFreeGb(hw_dto != null ? hw_dto.getDiskFreeGb(): null);       // ディスク空き容量(GB)

            if (existingHw == null) {
                agentMapper.insertHardware(hw);
            } else {
                agentMapper.updateHardware(hw);
            }

            // ---- ④ ソフトウェア情報を全削除→再登録（最新状態に置き換え）----
            agentMapper.deleteSoftwareByAgentNumber(agentNumber);
            if (!CollectionUtils.isEmpty(req.getSoftware())) {
                for (AgentReportRequest.Software sw : req.getSoftware()) {
                    agentMapper.insertSoftware(
                        agentNumber,       // V4: agent_number で識別
                        sw.getName(),      // ソフトウェア名
                        sw.getVersion(),   // バージョン
                        null,              // 発行元（JSONに含まれないため null）
                        null               // インストール日（JSONに含まれないため null）
                    );
                }
            }

            // ---- ⑤ ネットワーク情報を全削除→再登録（最新状態に置き換え）----
            agentMapper.deleteNetworkByAgentNumber(agentNumber);
            if (!CollectionUtils.isEmpty(req.getNetwork())) {
                for (AgentReportRequest.Network nic : req.getNetwork()) {
                    agentMapper.insertNetwork(
                        agentNumber,       // V4: agent_number で識別
                        null,              // NIC 名称（JSONに含まれないため null）
                        nic.getIp(),       // IP アドレス
                        nic.getMac()       // MAC アドレス
                    );
                }
            }
        }

        // ---- ⑥ 使用者名を社員IDに変換（社員名で employees テーブルを検索）----
        // 存在しない場合は employee_code = 'dummyCode' で employees テーブルに自動登録する
        Long assignedEmployeeId = null;
        if (req.getUserName() != null && !req.getUserName().isBlank()) {
            assignedEmployeeId = agentMapper.findEmployeeIdByName(req.getUserName());
            if (assignedEmployeeId == null) {
                // 一致する社員が存在しないため、ダミーコードで新規登録する
                log.info("エージェント報告: 使用者名 '{}' が employees テーブルに存在しないため自動登録します", req.getUserName());
                assignedEmployeeId = registerDummyEmployee(req.getUserName());
            }
        }

        // ---- ⑦ PC資産の各フィールドを更新（agent_number で特定）----
        // V4 変更: WHERE 条件を id から agent_number に変更
        // agent_number が設定済みの場合のみ更新する（null ではヒットしない）
        if (agentNumber != null && !agentNumber.isBlank()) {
            agentMapper.updateAgentLastSeen(
                agentNumber,
                req.getLocation(),
                req.getUserName(),
                assignedEmployeeId
            );
        }

        // ---- ⑧ 取得区分の更新（エージェントが選択した場合のみ）----
        // 仕様: 購入・レンタルどちらの選択でも更新する。
        //       バックエンドに既に設定済みの場合はエージェント側でフィールドがdisabledになるため
        //       このルートに到達しない（クライアント側で送信しない）。
        if (agentNumber != null && !agentNumber.isBlank()
                && req.getAcquisitionType() != null && !req.getAcquisitionType().isBlank()) {
            agentMapper.updateAcquisitionType(agentNumber, req.getAcquisitionType());
            log.info("取得区分を更新: agentNumber={}, acquisitionType={}", agentNumber, req.getAcquisitionType());
        }

        // ---- ⑨ レンタル区分の場合、pc_acquisition_rental に登録（未登録の場合のみ）----
        // 仕様変更（codeChange03）: エージェントから RENTAL でリクエストされた場合、
        // pc_acquisition_rental テーブルにレコードが存在しなければ自動登録する。
        // すでに登録済みの場合は既存レコードをそのまま使用する（更新は行わない）。
        if ("RENTAL".equals(req.getAcquisitionType())) {
            registerRentalIfAbsent(asset);
        }

        log.info("エージェント報告受信: hostname={}, agentNumber={}, isNew={}",
            req.getHostname(), agentNumber, isNew);
        return "OK";
    }

    // =========================================================
    // プライベートユーティリティ
    // =========================================================

    /**
     * エージェント報告時にPC資産が見つからない場合、pc_assets テーブルに自動登録する。
     *
     * <p>登録される PC 資産の内容:
     * <ul>
     *   <li>{@code asset_number}: "AUTO-XXXXXXXX"（UUID先頭8文字、自動生成）</li>
     *   <li>{@code device_name}: エージェントのホスト名</li>
     *   <li>{@code acquisition_type}: リクエストの値。未指定の場合は "UNKNOWN"（プレースホルダー）</li>
     *   <li>{@code status}: "IN_STORAGE"（DB デフォルト値）</li>
     *   <li>{@code hostname}: エージェントのホスト名</li>
     *   <li>{@code agent_number}: エージェント番号（agents テーブルと紐付け）</li>
     *   <li>{@code location}: エージェントが報告した設置場所（null 可）</li>
     *   <li>{@code user_name}: エージェントが報告した使用者名（null 可）</li>
     * </ul>
     * </p>
     *
     * <p>取得区分に "UNKNOWN" が設定された資産は、{@link #getAcquisitionType} が null を返すため、
     * エージェントの取得区分ドロップダウンは有効のまま維持される。
     * ユーザーが次回送信時に正しい値を選択することで step ⑧ の {@code updateAcquisitionType} が更新する。</p>
     *
     * @param req         エージェント報告リクエスト
     * @param agentNumber エージェント番号（null の場合もある）
     * @return 自動登録した PcAsset エンティティ（id はセットされていないが後続処理には不要）
     */
    private PcAsset autoRegisterAsset(AgentReportRequest req, String agentNumber) {
        // 資産番号: "AUTO-" + UUID先頭8文字（大文字）で一意な識別子を生成する
        String uuid8 = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String assetNumber = "AUTO-" + uuid8;

        // 取得区分: リクエストに含まれる場合はその値を使用。
        // 未指定の場合は "UNKNOWN"（プレースホルダー）を設定し、
        // getAcquisitionType() では null として扱うことでエージェントUIのドロップダウンを有効にする。
        String acquisitionType = (req.getAcquisitionType() != null && !req.getAcquisitionType().isBlank())
            ? req.getAcquisitionType()
            : "UNKNOWN";

        // pc_assets テーブルに INSERT する
        agentMapper.insertAsset(
            assetNumber,          // 自動生成した資産番号
            req.getHostname(),    // 端末名（ホスト名をそのまま使用）
            acquisitionType,      // 取得区分
            req.getHostname(),    // ホスト名
            agentNumber,          // エージェント番号（null 可）
            req.getLocation(),    // 設置場所（null 可）
            req.getUserName()     // 使用者名（null 可）
        );

        log.info("PC資産自動登録: assetNumber={}, hostname={}, agentNumber={}, acquisitionType={}",
            assetNumber, req.getHostname(), agentNumber, acquisitionType);

        // INSERT 後に DB から再取得して自動採番された id を含む完全なエンティティを返す。
        // step ⑨ のレンタル自動登録で pc_asset_id（= asset.getId()）が必要なため。
        PcAsset fetched = null;
        if (agentNumber != null && !agentNumber.isBlank()) {
            fetched = agentMapper.findAssetByAgentNumber(agentNumber);
        }
        if (fetched == null) {
            fetched = agentMapper.findAssetByHostname(req.getHostname());
        }
        if (fetched != null) {
            return fetched;
        }

        // フォールバック（通常はここに到達しない）
        PcAsset created = new PcAsset();
        created.setAssetNumber(assetNumber);
        created.setHostname(req.getHostname());
        created.setAgentNumber(agentNumber);
        created.setAcquisitionType(acquisitionType);
        return created;
    }

    /**
     * 取得区分が "RENTAL" の場合に pc_acquisition_rental へ自動登録する（未登録時のみ）。
     *
     * <p>処理手順:
     * <ol>
     *   <li>pc_asset_id で既存レンタル契約を確認する（UNIQUE 制約あり）</li>
     *   <li>既存レコードがあれば何もしない（既存の更新処理に委ねる）</li>
     *   <li>未登録の場合はシステムベンダーを取得（なければ作成）してレンタル契約を INSERT する</li>
     * </ol>
     * </p>
     *
     * <p>登録されるレンタル契約の値:
     * <ul>
     *   <li>rental_start_date : 本日（システム日付）</li>
     *   <li>rental_end_date   : 本日 + 1年（暫定値。管理者が後から更新すること）</li>
     *   <li>rental_vendor_id  : "エージェント自動登録" ベンダー（なければ自動作成）</li>
     *   <li>contract_number, monthly_fee 等 : 未設定（null）</li>
     * </ul>
     * </p>
     *
     * @param asset 対象PC資産エンティティ（id が必須）
     */
    private void registerRentalIfAbsent(PcAsset asset) {
        if (asset == null || asset.getId() == null) {
            log.warn("レンタル自動登録スキップ: PC資産のIDが取得できません（asset={}）", asset);
            return;
        }

        // 既にレンタル登録があるか確認（pc_acquisition_rental の UNIQUE(pc_asset_id) を考慮）
        PcAcquisitionRental existing = rentalMapper.findByPcAssetId(asset.getId());
        if (existing != null) {
            log.debug("レンタル登録済みのためスキップ: pcAssetId={}, rentalId={}", asset.getId(), existing.getId());
            return;
        }

        // システムベンダー（"エージェント自動登録"）を取得、なければ新規作成する
        // rental_vendor_id は NOT NULL のためプレースホルダーとして使用する
        Long vendorId = rentalMapper.findSystemVendorId();
        if (vendorId == null) {
            RentalVendor systemVendor = new RentalVendor();
            systemVendor.setCompanyName("エージェント自動登録");
            systemVendor.setNote(
                "エージェントアプリから RENTAL でリクエストされた機器を自動登録する際に使用するシステムベンダーです。"
                + " 管理者が実際のベンダー情報に更新してください。");
            rentalMapper.insertSystemVendor(systemVendor);
            vendorId = systemVendor.getId();
            log.info("システムベンダーを新規作成しました: id={}", vendorId);
        }

        // レンタル契約を新規登録する（日付は暫定値。管理者が後から正確な値に更新する）
        PcAcquisitionRental rental = new PcAcquisitionRental();
        rental.setPcAssetId(asset.getId());
        rental.setRentalVendorId(vendorId);
        rental.setRentalStartDate(LocalDate.now());             // 開始日: 本日
        rental.setRentalEndDate(LocalDate.now().plusYears(1));  // 終了日: 1年後（暫定）
        // contract_number, monthly_fee, contract_file_path は未設定（null）

        rentalMapper.insert(rental);
        log.info("レンタル機器自動登録完了: rentalId={}, pcAssetId={}, assetNumber={}",
            rental.getId(), asset.getId(), asset.getAssetNumber());
    }

    /**
     * エージェントが報告した使用者名で employees テーブルに存在しない場合、
     * ダミー社員コード（"dummyCode"）で社員レコードを新規登録する。
     *
     * <p>登録される社員レコードの内容:
     * <ul>
     *   <li>{@code employee_code}: {@value DUMMY_EMPLOYEE_CODE}（仮登録を示すプレースホルダー）</li>
     *   <li>{@code full_name}: エージェントが報告した使用者名</li>
     *   <li>{@code is_active}: {@code true}（在籍中として扱う）</li>
     *   <li>その他項目: {@code null}（未設定）</li>
     * </ul>
     * </p>
     *
     * <p>V5 マイグレーションにより {@code employee_code} の UNIQUE 制約を解除済みのため、
     * 複数の社員が {@value DUMMY_EMPLOYEE_CODE} を持つことができる。</p>
     *
     * @param fullName エージェントが報告した使用者名
     * @return 新規登録した社員の ID
     */
    private Long registerDummyEmployee(String fullName) {
        // ダミー社員エンティティを構築する
        Employee dummy = new Employee();
        dummy.setEmployeeCode(DUMMY_EMPLOYEE_CODE); // 仮登録コード（固定値）
        dummy.setFullName(fullName);                // エージェントが報告した使用者名
        dummy.setDepartment(null);                  // 部署未設定
        dummy.setPosition(null);                    // 役職未設定
        dummy.setEmail(null);                       // メール未設定
        dummy.setPhone(null);                       // 電話未設定
        dummy.setLocation(null);                    // 勤務地未設定
        dummy.setIsActive(true);                    // 在籍中として登録する

        // employees テーブルに INSERT（自動採番 ID が dummy.id にセットされる）
        employeeMapper.insert(dummy);

        log.info("ダミー社員自動登録: id={}, fullName={}, employeeCode={}",
            dummy.getId(), fullName, DUMMY_EMPLOYEE_CODE);

        return dummy.getId();
    }
}
