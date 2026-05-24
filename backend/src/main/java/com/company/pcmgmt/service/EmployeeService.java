package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.EmployeeCreateRequest;
import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.request.EmployeeUpdateRequest;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.Employee;
import com.company.pcmgmt.domain.mapper.EmployeeMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 社員管理サービス
 *
 * <p>社員情報の CRUD 操作を担当するビジネスロジック層。
 * データアクセスは {@link EmployeeMapper} に委譲する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    /** 社員情報の DB アクセスを担うマッパー */
    private final EmployeeMapper employeeMapper;

    /**
     * 社員一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・在籍フラグ・ページ情報）
     * @return ページネーション付き社員レスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findAll(EmployeeSearchRequest req) {
        // 検索条件に一致する社員リストを取得
        List<EmployeeResponse> content = employeeMapper.findAll(req);
        // 総件数を取得（ページネーション計算用）
        long total = employeeMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 在籍中の社員リストを取得する（プルダウン選択用）
     *
     * <p>PC資産の担当者割り当てや貸出先選択に使用するドロップダウン用データ。
     * isActive = true の社員のみを返す。</p>
     *
     * @return 在籍中社員のリスト
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findActiveList() {
        return employeeMapper.findActiveList();
    }

    /**
     * 指定IDの社員詳細を取得する
     *
     * @param id 社員ID
     * @return EmployeeResponse
     * @throws ResourceNotFoundException 指定IDの社員が存在しない場合
     */
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        EmployeeResponse emp = employeeMapper.findById(id);
        // 存在しない場合は 404 例外をスロー
        if (emp == null) {
            throw new ResourceNotFoundException("社員が見つかりません: id=" + id);
        }
        return emp;
    }

    /**
     * 社員を新規登録する
     *
     * @param req 登録リクエスト
     * @return 登録後の社員レスポンス
     * @throws IllegalArgumentException 社員コードが既に使用されている場合
     */
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest req) {
        // 社員コードの重複チェック（excludeId=null は新規登録を意味する）
        if (employeeMapper.existsByEmployeeCode(req.getEmployeeCode(), null)) {
            throw new IllegalArgumentException("社員コード [" + req.getEmployeeCode() + "] は既に使用されています");
        }

        // リクエストからエンティティを組み立て
        Employee emp = new Employee();
        emp.setEmployeeCode(req.getEmployeeCode()); // 社員コード
        emp.setFullName(req.getFullName());          // 氏名
        emp.setDepartment(req.getDepartment());      // 部署
        emp.setPosition(req.getPosition());          // 役職
        emp.setEmail(req.getEmail());                // メールアドレス
        emp.setPhone(req.getPhone());                // 電話番号
        emp.setLocation(req.getLocation());          // 勤務場所
        // 新規登録は必ず在籍中（true）で開始
        emp.setIsActive(true);

        // DBに保存（INSERT 後、emp.id に自動採番IDがセットされる）
        employeeMapper.insert(emp);
        log.info("社員登録: employeeCode={}", emp.getEmployeeCode());

        // 保存後のデータを返す
        return employeeMapper.findById(emp.getId());
    }

    /**
     * 社員情報を更新する
     *
     * <p>退職処理は {@code req.isActive = false} を設定することで行う。</p>
     *
     * @param id  更新対象の社員ID
     * @param req 更新リクエスト
     * @return 更新後の社員レスポンス
     * @throws ResourceNotFoundException 指定IDの社員が存在しない場合
     */
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest req) {
        // 更新対象の存在確認（存在しない場合は 404 例外をスロー）
        Employee existing = employeeMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("社員が見つかりません: id=" + id);
        }

        // 既存エンティティに更新値をセット
        existing.setFullName(req.getFullName());      // 氏名
        existing.setDepartment(req.getDepartment());  // 部署
        existing.setPosition(req.getPosition());      // 役職
        existing.setEmail(req.getEmail());            // メールアドレス
        existing.setPhone(req.getPhone());            // 電話番号
        existing.setLocation(req.getLocation());      // 勤務場所
        // isActive が指定された場合のみ更新（null の場合は変更しない）
        if (req.getIsActive() != null) {
            existing.setIsActive(req.getIsActive());  // 在籍フラグ
        }

        employeeMapper.update(existing);
        log.info("社員更新: id={}", id);

        // 更新後のデータを返す
        return employeeMapper.findById(id);
    }
}
