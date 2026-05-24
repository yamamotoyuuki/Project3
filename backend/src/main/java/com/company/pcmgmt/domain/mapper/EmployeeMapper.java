package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.domain.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社員 MyBatis マッパーインターフェース
 *
 * <p>employees テーブルに対する CRUD クエリを定義する。
 * 実装は {@code resources/mapper/EmployeeMapper.xml} に記述する。</p>
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 検索条件・ページネーションを適用して社員一覧を取得する
     *
     * @param req 検索条件（キーワード・在籍フラグ・ページ情報）
     * @return EmployeeResponse のリスト
     */
    List<EmployeeResponse> findAll(@Param("req") EmployeeSearchRequest req);

    /**
     * 検索条件に一致する総件数を取得する（ページネーション用）
     *
     * @param req 検索条件
     * @return 総件数
     */
    long countAll(@Param("req") EmployeeSearchRequest req);

    /**
     * 指定IDの社員を取得する（レスポンス形式）
     *
     * @param id 社員ID
     * @return EmployeeResponse（存在しない場合は null）
     */
    EmployeeResponse findById(@Param("id") Long id);

    /**
     * 指定IDの社員をエンティティ形式で取得する（更新処理用）
     *
     * @param id 社員ID
     * @return Employee エンティティ（存在しない場合は null）
     */
    Employee findRawById(@Param("id") Long id);

    /**
     * 社員を新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code employee.id} にセットされる。</p>
     *
     * @param employee 登録する社員エンティティ
     * @return 挿入件数（通常 1）
     */
    int insert(Employee employee);

    /**
     * 社員情報を更新する
     *
     * @param employee 更新する社員エンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int update(Employee employee);

    /**
     * 社員コードの重複チェックを行う
     *
     * @param employeeCode チェック対象の社員コード
     * @param excludeId    更新時に自分自身を除外するためのID（新規登録時は null）
     * @return 重複している場合は true
     */
    boolean existsByEmployeeCode(@Param("employeeCode") String employeeCode,
                                 @Param("excludeId") Long excludeId);

    /**
     * 在籍中の社員リストを全件取得する（プルダウン選択用）
     *
     * <p>PC資産の担当者割り当てや貸出先選択のドロップダウンに使用する。
     * 在籍フラグ（isActive = true）の社員のみを返す。</p>
     *
     * @return 在籍中社員の EmployeeResponse リスト
     */
    List<EmployeeResponse> findActiveList();
}
