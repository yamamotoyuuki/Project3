-- =============================================
-- V12: pc_rental_history テーブルを再設計
-- =============================================
-- 変更内容:
--   旧: スナップショット型（変更後の全フィールドを1行保存）
--   新: フィールド差分記録型（変更前後の値を1フィールド1行で保存）
--
-- 新設計のカラム:
--   operation_id  : 同一保存操作をまとめる UUID（同時変更フィールドをグルーピング）
--   operation     : 操作種別（CREATE / UPDATE / RETURN）
--   field_name    : 変更フィールド名（UPDATE 時。CREATE は NULL）
--   field_label   : 画面表示用ラベル（例: "契約終了日"）
--   old_value     : 変更前の値（文字列）
--   new_value     : 変更後の値（文字列）
--   changed_by_name: 操作者名（ユーザー削除後も表示できるよう文字列キャッシュ）
-- =============================================

-- 旧テーブルを削除（FK はこのテーブルから外部テーブルへの参照のみのため安全に削除可）
DROP TABLE IF EXISTS pc_rental_history;

-- 新テーブルを作成
CREATE TABLE pc_rental_history (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'レコードID',
    rental_id        BIGINT       NOT NULL              COMMENT '対象レンタル契約ID',
    operation_id     CHAR(36)     NOT NULL              COMMENT '同一操作をまとめるUUID',
    operation        VARCHAR(20)  NOT NULL              COMMENT '操作種別（CREATE / UPDATE / RETURN）',
    field_name       VARCHAR(100)                       COMMENT '変更フィールド名（UPDATE 時）',
    field_label      VARCHAR(100)                       COMMENT '画面表示用ラベル（例: 契約終了日）',
    old_value        VARCHAR(500)                       COMMENT '変更前の値（文字列。NULL = 未設定または新規）',
    new_value        VARCHAR(500)                       COMMENT '変更後の値（文字列）',
    changed_by_name  VARCHAR(100)                       COMMENT '操作者名（表示用キャッシュ）',
    changed_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '変更日時',
    PRIMARY KEY (id),
    KEY idx_rental_history_rental_id (rental_id),     -- rental_id での検索を高速化
    KEY idx_rental_history_changed_at (changed_at),   -- 時系列ソートを高速化
    CONSTRAINT fk_rental_history_rental
        FOREIGN KEY (rental_id) REFERENCES pc_acquisition_rental(id) ON DELETE CASCADE
) COMMENT='レンタル契約 変更履歴（フィールド差分記録型）';
