package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.RentalCreateRequest;
import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.request.RentalVendorCreateRequest;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.RentalResponse;
import com.company.pcmgmt.api.dto.response.RentalVendorResponse;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.RentalVendor;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import com.company.pcmgmt.domain.mapper.RentalMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * レンタル管理サービス
 *
 * <p>レンタル契約とレンタルベンダーの CRUD 操作を担当するビジネスロジック層。
 * データアクセスは {@link RentalMapper} および {@link PcAssetMapper} に委譲する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    /** レンタル契約・ベンダーの DB アクセスを担うマッパー */
    private final RentalMapper rentalMapper;

    /** PC資産の存在確認に使用するマッパー */
    private final PcAssetMapper pcAssetMapper;

    // ======= レンタル契約 =======

    /**
     * レンタル契約一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・期限フィルター・返却状況・ページ情報）
     * @return ページネーション付きレンタル契約レスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> findAll(RentalSearchRequest req) {
        // 検索条件に一致するレンタル契約リストを取得
        List<RentalResponse> content = rentalMapper.findAll(req);
        // 総件数を取得（ページネーション計算用）
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
        // 存在しない場合は 404 例外をスロー
        if (r == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        return r;
    }

    /**
     * レンタル契約を新規登録する
     *
     * <p>PC資産とベンダーの存在確認を行ってから登録する。</p>
     *
     * @param req レンタル契約登録リクエスト
     * @return 登録後のレンタル契約レスポンス
     * @throws ResourceNotFoundException PC資産またはベンダーが存在しない場合
     */
    @Transactional
    public RentalResponse create(RentalCreateRequest req) {
        // 対象PC資産の存在確認
        if (pcAssetMapper.findRawById(req.getPcAssetId()) == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + req.getPcAssetId());
        }
        // 対象ベンダーの存在確認
        if (rentalMapper.findRawVendorById(req.getRentalVendorId()) == null) {
            throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + req.getRentalVendorId());
        }

        // リクエストからエンティティを組み立て
        PcAcquisitionRental rental = new PcAcquisitionRental();
        rental.setPcAssetId(req.getPcAssetId());                  // 対象PC資産ID
        rental.setRentalVendorId(req.getRentalVendorId());        // ベンダーID
        rental.setContractNumber(req.getContractNumber());        // 契約番号
        rental.setRentalStartDate(req.getRentalStartDate());      // レンタル開始日
        rental.setRentalEndDate(req.getRentalEndDate());          // レンタル終了日
        rental.setMonthlyFee(req.getMonthlyFee());               // 月額料金
        rental.setContractFilePath(req.getContractFilePath());   // 契約書ファイルパス

        // DBに保存（INSERT 後、rental.id に自動採番IDがセットされる）
        rentalMapper.insert(rental);
        log.info("レンタル契約登録: id={}", rental.getId());

        // 保存後のデータ（JOIN情報を含む）を返す
        return rentalMapper.findById(rental.getId());
    }

    /**
     * レンタル品の返却を登録する
     *
     * <p>本日の日付を返却日としてセットする。
     * 既に返却済みの場合はエラーとなる。</p>
     *
     * @param id 返却対象のレンタル契約ID
     * @return 更新後のレンタル契約レスポンス
     * @throws ResourceNotFoundException 指定IDのレンタル契約が存在しない場合
     * @throws IllegalStateException     既に返却済みの場合
     */
    @Transactional
    public RentalResponse returnRental(Long id) {
        // 返却対象のレンタル契約を取得
        PcAcquisitionRental rental = rentalMapper.findRawById(id);
        if (rental == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        // 既に返却済みの場合はエラー
        if (rental.getReturnDate() != null) throw new IllegalStateException("この契約はすでに返却済みです");

        // 本日の日付を返却日としてセット
        rental.setReturnDate(LocalDate.now());
        rentalMapper.update(rental);
        log.info("レンタル返却登録: id={}", id);

        // 更新後のデータを返す
        return rentalMapper.findById(id);
    }

    // ======= ベンダー =======

    /**
     * 全レンタルベンダーを取得する
     *
     * @return ベンダーレスポンスのリスト
     */
    @Transactional(readOnly = true)
    public List<RentalVendorResponse> findAllVendors() {
        return rentalMapper.findAllVendors();
    }

    /**
     * 指定IDのベンダー詳細を取得する
     *
     * @param id ベンダーID
     * @return RentalVendorResponse
     * @throws ResourceNotFoundException 指定IDのベンダーが存在しない場合
     */
    @Transactional(readOnly = true)
    public RentalVendorResponse findVendorById(Long id) {
        RentalVendorResponse v = rentalMapper.findVendorById(id);
        // 存在しない場合は 404 例外をスロー
        if (v == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);
        return v;
    }

    /**
     * レンタルベンダーを新規登録する
     *
     * @param req ベンダー登録リクエスト
     * @return 登録後のベンダーレスポンス
     */
    @Transactional
    public RentalVendorResponse createVendor(RentalVendorCreateRequest req) {
        // リクエストからエンティティを組み立て
        RentalVendor vendor = new RentalVendor();
        vendor.setCompanyName(req.getCompanyName()); // 会社名
        vendor.setContactName(req.getContactName()); // 担当者名
        vendor.setPhone(req.getPhone());             // 電話番号
        vendor.setEmail(req.getEmail());             // メールアドレス
        vendor.setAddress(req.getAddress());         // 住所
        vendor.setNote(req.getNote());               // 備考

        // DBに保存（INSERT 後、vendor.id に自動採番IDがセットされる）
        rentalMapper.insertVendor(vendor);
        log.info("ベンダー登録: id={}", vendor.getId());

        // 保存後のデータを返す
        return rentalMapper.findVendorById(vendor.getId());
    }

    /**
     * レンタルベンダー情報を更新する
     *
     * @param id  更新対象のベンダーID
     * @param req 更新リクエスト
     * @return 更新後のベンダーレスポンス
     * @throws ResourceNotFoundException 指定IDのベンダーが存在しない場合
     */
    @Transactional
    public RentalVendorResponse updateVendor(Long id, RentalVendorCreateRequest req) {
        // 更新対象の存在確認（存在しない場合は 404 例外をスロー）
        RentalVendor vendor = rentalMapper.findRawVendorById(id);
        if (vendor == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);

        // 既存エンティティに更新値をセット
        vendor.setCompanyName(req.getCompanyName()); // 会社名
        vendor.setContactName(req.getContactName()); // 担当者名
        vendor.setPhone(req.getPhone());             // 電話番号
        vendor.setEmail(req.getEmail());             // メールアドレス
        vendor.setAddress(req.getAddress());         // 住所
        vendor.setNote(req.getNote());               // 備考

        rentalMapper.updateVendor(vendor);

        // 更新後のデータを返す
        return rentalMapper.findVendorById(id);
    }
}
