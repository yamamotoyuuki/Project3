-- =============================================
-- V9: code_master - id カラムの削除
-- =============================================
-- 目的: code_master テーブルから surrogate key（id）を削除し、
--       本来の自然キーである (code_type, code_value) を主キーに昇格させる。
--
-- 変更手順:
--   1. id カラムの AUTO_INCREMENT を除去（MySQL は PK 削除前に必須）
--   2. 既存の PRIMARY KEY（id）を削除
--   3. UNIQUE 制約 uq_code_master_type_value を削除（PRIMARY KEY に置き換えるため）
--   4. idx_code_master_type インデックスを削除
--        └─ 複合主キー (code_type, code_value) の先頭カラムで代替されるため不要
--   5. id カラムを物理削除
--   6. (code_type, code_value) を新しい PRIMARY KEY として追加
-- =============================================

-- Step 1: AUTO_INCREMENT を除去する（PRIMARY KEY を削除するために必要）
ALTER TABLE code_master
    MODIFY COLUMN id BIGINT NOT NULL COMMENT 'surrogate key（削除予定）';

-- Step 2: 既存の PRIMARY KEY（id）を削除する
ALTER TABLE code_master DROP PRIMARY KEY;

-- Step 3: UNIQUE 制約を削除する（新しい PRIMARY KEY に置き換えるため）
ALTER TABLE code_master DROP INDEX uq_code_master_type_value;

-- Step 4: code_type 単独インデックスを削除する
--         （複合 PRIMARY KEY の先頭カラムでカバーされるため冗長）
ALTER TABLE code_master DROP INDEX idx_code_master_type;

-- Step 5: id カラムを物理削除する
ALTER TABLE code_master DROP COLUMN id;

-- Step 6: (code_type, code_value) を新しい PRIMARY KEY として追加する
--         code_type で絞り込む検索は複合主キーの先頭カラムとして索引が効く
ALTER TABLE code_master ADD PRIMARY KEY (code_type, code_value);
