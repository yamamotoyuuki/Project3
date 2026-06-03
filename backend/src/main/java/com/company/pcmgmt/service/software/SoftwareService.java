package com.company.pcmgmt.service.software;

import com.company.pcmgmt.api.dto.request.software.SoftwareCreateRequest;
import com.company.pcmgmt.api.dto.request.software.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.software.SoftwareResponse;
import com.company.pcmgmt.domain.entity.SoftwareMaster;
import com.company.pcmgmt.domain.mapper.software.SoftwareMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ソフトウェアライセンス管理サービス
 *
 * <p>ソフトウェアマスタの CRUD 操作を担当するビジネスロジック層。
 * データアクセスは {@link SoftwareMapper} に委譲する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareService {

    /** ソフトウェアの DB アクセスを担うマッパー */
    private final SoftwareMapper softwareMapper;

    /**
     * ソフトウェア一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・超過フィルター・ページ情報）
     * @return ページネーション付きソフトウェアレスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<SoftwareResponse> findAll(SoftwareSearchRequest req) {
        // 検索条件に一致するソフトウェアリストを取得
        List<SoftwareResponse> content = softwareMapper.findAll(req);
        // 総件数を取得（ページネーション計算用）
        long total = softwareMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 指定IDのソフトウェア詳細を取得する
     *
     * @param id ソフトウェアマスタID
     * @return SoftwareResponse
     * @throws ResourceNotFoundException 指定IDのソフトウェアが存在しない場合
     */
    @Transactional(readOnly = true)
    public SoftwareResponse findById(Long id) {
        SoftwareResponse sw = softwareMapper.findById(id);
        // 存在しない場合は 404 例外をスロー
        if (sw == null) throw new ResourceNotFoundException("ソフトウェアが見つかりません: id=" + id);
        return sw;
    }

    /**
     * ソフトウェアを新規登録する
     *
     * @param req 登録リクエスト
     * @return 登録後のソフトウェアレスポンス
     */
    @Transactional
    public SoftwareResponse create(SoftwareCreateRequest req) {
        // リクエストからエンティティを組み立て
        SoftwareMaster sw = new SoftwareMaster();
        sw.setSoftwareName(req.getSoftwareName()); // ソフトウェア名
        sw.setPublisher(req.getPublisher());       // 発行元
        sw.setLicenseType(req.getLicenseType());   // ライセンス種別
        // 購入数が null の場合は 0（ライセンス管理なし）として登録
        sw.setPurchasedCount(req.getPurchasedCount() != null ? req.getPurchasedCount() : 0);
        sw.setNote(req.getNote());                 // 備考

        // DBに保存（INSERT 後、sw.id に自動採番IDがセットされる）
        softwareMapper.insert(sw);
        log.info("ソフトウェア登録: id={}", sw.getId());

        // 保存後のデータ（インストール数含む）を返す
        return softwareMapper.findById(sw.getId());
    }

    /**
     * ソフトウェア情報を更新する
     *
     * @param id  更新対象のソフトウェアマスタID
     * @param req 更新リクエスト
     * @return 更新後のソフトウェアレスポンス
     * @throws ResourceNotFoundException 指定IDのソフトウェアが存在しない場合
     */
    @Transactional
    public SoftwareResponse update(Long id, SoftwareCreateRequest req) {
        // 更新対象の存在確認（存在しない場合は 404 例外をスロー）
        SoftwareMaster sw = softwareMapper.findRawById(id);
        if (sw == null) throw new ResourceNotFoundException("ソフトウェアが見つかりません: id=" + id);

        // 既存エンティティに更新値をセット
        sw.setSoftwareName(req.getSoftwareName()); // ソフトウェア名
        sw.setPublisher(req.getPublisher());       // 発行元
        sw.setLicenseType(req.getLicenseType());   // ライセンス種別
        // 購入数が null の場合は 0（ライセンス管理なし）として更新
        sw.setPurchasedCount(req.getPurchasedCount() != null ? req.getPurchasedCount() : 0);
        sw.setNote(req.getNote());                 // 備考

        softwareMapper.update(sw);

        // 更新後のデータを返す
        return softwareMapper.findById(id);
    }
}
