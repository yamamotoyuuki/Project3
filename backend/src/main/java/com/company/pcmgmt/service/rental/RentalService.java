package com.company.pcmgmt.service.rental;

import com.company.pcmgmt.api.dto.request.rental.RentalCreateRequest;
import com.company.pcmgmt.api.dto.request.rental.RentalReturnRequest;
import com.company.pcmgmt.api.dto.request.rental.RentalSearchRequest;
import com.company.pcmgmt.api.dto.request.rental.RentalUpdateRequest;
import com.company.pcmgmt.api.dto.request.rental.RentalVendorCreateRequest;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.rental.RentalHistoryResponse;
import com.company.pcmgmt.api.dto.response.rental.RentalResponse;
import com.company.pcmgmt.api.dto.response.rental.RentalVendorResponse;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.RentalHistory;
import com.company.pcmgmt.domain.entity.RentalVendor;
import com.company.pcmgmt.domain.mapper.asset.PcAssetMapper;
import com.company.pcmgmt.domain.mapper.rental.RentalHistoryMapper;
import com.company.pcmgmt.domain.mapper.rental.RentalMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * レンタル管理サービス
 *
 * <p>レンタル契約とレンタルベンダーの CRUD 操作を担当するビジネスロジック層。
 * CREATE / UPDATE / RETURN の各操作時に {@code pc_rental_history} へ変更履歴を記録する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    /** レンタル契約・ベンダーの DB アクセスを担うマッパー */
    private final RentalMapper rentalMapper;

    /** PC資産の存在確認に使用するマッパー */
    private final PcAssetMapper pcAssetMapper;

    /** レンタル契約変更履歴の DB アクセスを担うマッパー */
    private final RentalHistoryMapper rentalHistoryMapper;

    /** 日付フォーマット（履歴の old_value / new_value に使用） */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ======= レンタル契約 =======

    /**
     * レンタル契約一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・期限フィルター・返却状況・ページ情報）
     * @return ページネーション付きレンタル契約レスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> findAll(RentalSearchRequest req) {
        List<RentalResponse> content = rentalMapper.findAll(req);
        long total = rentalMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 指定IDのレンタル契約詳細を取得する
     *
     * @param id レンタル契約ID
     * @return RentalResponse
     * @throws ResourceNotFoundException 指定IDのレンタル契約が存在しない場合
     */
    @Transactional(readOnly = true)
    public RentalResponse findById(Long id) {
        RentalResponse r = rentalMapper.findById(id);
        if (r == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        return r;
    }

    /**
     * 指定レンタル契約の変更履歴一覧を取得する
     *
     * @param id レンタル契約ID
     * @return 変更履歴レスポンスのリスト（新しい順）
     * @throws ResourceNotFoundException 指定IDのレンタル契約が存在しない場合
     */
    @Transactional(readOnly = true)
    public List<RentalHistoryResponse> getHistories(Long id) {
        // 契約の存在確認
        if (rentalMapper.findById(id) == null) {
            throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        }
        return rentalHistoryMapper.findByRentalId(id);
    }

    /**
     * レンタル契約を新規登録する
     *
     * <p>登録後に operation=CREATE の履歴を1件記録する。</p>
     *
     * @param req レンタル契約登録リクエスト
     * @return 登録後のレンタル契約レスポンス
     * @throws ResourceNotFoundException PC資産またはベンダーが存在しない場合
     */
    @Transactional
    public RentalResponse create(RentalCreateRequest req) {
        // 対象PC資産・ベンダーの存在確認
        if (pcAssetMapper.findRawById(req.getPcAssetId()) == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + req.getPcAssetId());
        }
        if (rentalMapper.findRawVendorById(req.getRentalVendorId()) == null) {
            throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + req.getRentalVendorId());
        }

        // エンティティ組み立て
        PcAcquisitionRental rental = new PcAcquisitionRental();
        rental.setPcAssetId(req.getPcAssetId());
        rental.setRentalVendorId(req.getRentalVendorId());
        rental.setContractNumber(req.getContractNumber());
        rental.setRentalStartDate(req.getRentalStartDate());
        rental.setRentalEndDate(req.getRentalEndDate());
        rental.setMonthlyFee(req.getMonthlyFee());
        rental.setContractFilePath(req.getContractFilePath());

        rentalMapper.insert(rental);
        log.info("レンタル契約登録: id={}", rental.getId());

        // 登録履歴を1件記録する（フィールド差分なし）
        RentalHistory hist = buildHistory(rental.getId(), UUID.randomUUID().toString(), "CREATE");
        rentalHistoryMapper.insert(hist);

        return rentalMapper.findById(rental.getId());
    }

    /**
     * レンタル契約情報を更新する（ベンダー・契約番号・期間・月額）
     *
     * <p>変更されたフィールドのみ履歴を記録する（未変更フィールドは記録しない）。</p>
     *
     * @param id  更新対象のレンタル契約ID
     * @param req 更新リクエスト
     * @return 更新後のレンタル契約レスポンス
     * @throws ResourceNotFoundException 指定IDのレンタル契約が存在しない場合
     */
    @Transactional
    public RentalResponse update(Long id, RentalUpdateRequest req) {
        // 更新対象の取得
        PcAcquisitionRental rental = rentalMapper.findRawById(id);
        if (rental == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);

        // 変更前のベンダー名を取得（フィールド差分記録に使用）
        RentalVendor oldVendor = rentalMapper.findRawVendorById(rental.getRentalVendorId());
        String oldVendorName = oldVendor != null ? oldVendor.getCompanyName() : String.valueOf(rental.getRentalVendorId());

        // 変更後のベンダー名を取得（ベンダーIDが変わった場合のみ）
        String newVendorName = oldVendorName;
        if (!Objects.equals(rental.getRentalVendorId(), req.getRentalVendorId())) {
            RentalVendor newVendor = rentalMapper.findRawVendorById(req.getRentalVendorId());
            newVendorName = newVendor != null ? newVendor.getCompanyName() : String.valueOf(req.getRentalVendorId());
        }

        // 変更フィールドのリストを構築する
        String operationId = UUID.randomUUID().toString();
        String username = getCurrentUsername();
        List<RentalHistory> histories = new ArrayList<>();

        addIfChanged(histories, id, operationId, username,
            "rentalVendorId", "ベンダー",    oldVendorName,                   newVendorName);
        addIfChanged(histories, id, operationId, username,
            "contractNumber", "契約番号",    nullToEmpty(rental.getContractNumber()), nullToEmpty(req.getContractNumber()));
        addIfChanged(histories, id, operationId, username,
            "rentalStartDate", "契約開始日", formatDate(rental.getRentalStartDate()),   formatDate(req.getRentalStartDate()));
        addIfChanged(histories, id, operationId, username,
            "rentalEndDate",   "契約終了日", formatDate(rental.getRentalEndDate()),     formatDate(req.getRentalEndDate()));
        addIfChanged(histories, id, operationId, username,
            "monthlyFee",      "月額",       formatFee(rental.getMonthlyFee()),         formatFee(req.getMonthlyFee()));

        // エンティティ更新
        rental.setRentalVendorId(req.getRentalVendorId());
        rental.setContractNumber(req.getContractNumber());
        rental.setRentalStartDate(req.getRentalStartDate());
        rental.setRentalEndDate(req.getRentalEndDate());
        rental.setMonthlyFee(req.getMonthlyFee());
        rentalMapper.update(rental);
        log.info("レンタル契約更新: id={}", id);

        // 変更フィールドが1件以上あれば履歴を登録する
        for (RentalHistory h : histories) {
            rentalHistoryMapper.insert(h);
        }
        if (histories.isEmpty()) {
            log.debug("レンタル契約更新: 変更フィールドなし id={}", id);
        }

        return rentalMapper.findById(id);
    }

    /**
     * レンタル品の返却を登録する
     *
     * <p>返却後に operation=RETURN の履歴を1件記録する。</p>
     *
     * @param id  返却対象のレンタル契約ID
     * @param req 返却リクエスト（返却日を含む）
     * @return 更新後のレンタル契約レスポンス
     * @throws ResourceNotFoundException 指定IDのレンタル契約が存在しない場合
     * @throws IllegalStateException     既に返却済みの場合
     */
    @Transactional
    public RentalResponse returnRental(Long id, RentalReturnRequest req) {
        PcAcquisitionRental rental = rentalMapper.findRawById(id);
        if (rental == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        if (rental.getReturnDate() != null) throw new IllegalStateException("この契約はすでに返却済みです");

        rental.setReturnDate(req.getReturnDate());
        rentalMapper.update(rental);
        log.info("レンタル返却登録: id={}, returnDate={}", id, req.getReturnDate());

        // 返却履歴を記録する
        RentalHistory hist = buildHistory(id, UUID.randomUUID().toString(), "RETURN");
        hist.setFieldName("returnDate");
        hist.setFieldLabel("返却日");
        hist.setOldValue(null);
        hist.setNewValue(formatDate(req.getReturnDate()));
        rentalHistoryMapper.insert(hist);

        return rentalMapper.findById(id);
    }

    // ======= ベンダー =======

    @Transactional(readOnly = true)
    public List<RentalVendorResponse> findAllVendors() {
        return rentalMapper.findAllVendors();
    }

    @Transactional(readOnly = true)
    public RentalVendorResponse findVendorById(Long id) {
        RentalVendorResponse v = rentalMapper.findVendorById(id);
        if (v == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);
        return v;
    }

    @Transactional
    public RentalVendorResponse createVendor(RentalVendorCreateRequest req) {
        RentalVendor vendor = new RentalVendor();
        vendor.setCompanyName(req.getCompanyName());
        vendor.setContactName(req.getContactName());
        vendor.setPhone(req.getPhone());
        vendor.setEmail(req.getEmail());
        vendor.setAddress(req.getAddress());
        vendor.setNote(req.getNote());
        rentalMapper.insertVendor(vendor);
        log.info("ベンダー登録: id={}", vendor.getId());
        return rentalMapper.findVendorById(vendor.getId());
    }

    @Transactional
    public RentalVendorResponse updateVendor(Long id, RentalVendorCreateRequest req) {
        RentalVendor vendor = rentalMapper.findRawVendorById(id);
        if (vendor == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);
        vendor.setCompanyName(req.getCompanyName());
        vendor.setContactName(req.getContactName());
        vendor.setPhone(req.getPhone());
        vendor.setEmail(req.getEmail());
        vendor.setAddress(req.getAddress());
        vendor.setNote(req.getNote());
        rentalMapper.updateVendor(vendor);
        return rentalMapper.findVendorById(id);
    }

    // ======= プライベートヘルパー =======

    /**
     * 基本フィールド設定済みの RentalHistory を生成する。
     * rentalId / operationId / operation / changedByName をセットして返す。
     */
    private RentalHistory buildHistory(Long rentalId, String operationId, String operation) {
        RentalHistory h = new RentalHistory();
        h.setRentalId(rentalId);
        h.setOperationId(operationId);
        h.setOperation(operation);
        h.setChangedByName(getCurrentUsername());
        return h;
    }

    /**
     * 変更前後の値が異なる場合のみ履歴リストに追加する。
     * 同じ値なら何もしない。
     */
    private void addIfChanged(
            List<RentalHistory> list, Long rentalId, String operationId, String username,
            String fieldName, String fieldLabel, String oldVal, String newVal) {
        if (Objects.equals(oldVal, newVal)) return;
        RentalHistory h = new RentalHistory();
        h.setRentalId(rentalId);
        h.setOperationId(operationId);
        h.setOperation("UPDATE");
        h.setFieldName(fieldName);
        h.setFieldLabel(fieldLabel);
        h.setOldValue(oldVal);
        h.setNewValue(newVal);
        h.setChangedByName(username);
        list.add(h);
    }

    /** Spring Security の認証コンテキストからログインユーザー名を取得する */
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.isAuthenticated()) ? auth.getName() : "不明";
        } catch (Exception e) {
            return "不明";
        }
    }

    /** LocalDate を YYYY-MM-DD 形式の文字列に変換する（null は "未設定"） */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "未設定";
    }

    /** BigDecimal の月額を "¥XX,XXX" 形式に変換する（null は "未設定"） */
    private String formatFee(BigDecimal fee) {
        if (fee == null) return "未設定";
        return String.format("¥%,d", fee.longValue());
    }

    /** null を空文字列に変換する（契約番号など任意テキストフィールド用） */
    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
