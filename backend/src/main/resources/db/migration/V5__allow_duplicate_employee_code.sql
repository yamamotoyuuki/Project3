-- =============================================
-- V5: employees.employee_code のユニーク制約を通常インデックスに変更
-- =============================================
-- エージェントから未登録ユーザーが報告された場合、employee_code = 'dummyCode' で
-- 社員レコードを自動登録する仕様変更に対応する。
-- 複数の社員が employee_code = 'dummyCode' を持てるよう UNIQUE 制約を解除する。
-- ※ アプリ側（EmployeeService）でコード重複チェックは引き続き実施するため、
--   手動登録フローでの重複は防止される。
-- =============================================

-- 既存のユニーク制約を削除する
ALTER TABLE employees
    DROP INDEX uq_employees_code;

-- 検索用の通常インデックスとして再作成する（UNIQUE → 非ユニーク）
ALTER TABLE employees
    ADD INDEX idx_employees_code (employee_code);
