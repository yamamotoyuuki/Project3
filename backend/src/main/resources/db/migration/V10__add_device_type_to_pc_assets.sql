-- =============================================
-- V10: pc_assets - device_type カラムの追加
-- =============================================
-- 目的: PC資産の機器種別を保持するカラムを追加する。
--       値は code_master テーブルの DEVICE_TYPE コード値（例: LAPTOP, DESKTOP）を格納する。
--       FK 制約は持たせず、コードマスタとの照合は画面表示時のみ行う設計とする。
--
-- 配置: device_name の直後に挿入し、機器の基本情報をまとめて管理する。
-- NULL 許容: 既存レコードへの影響を避けるため NULL を許可する（任意項目）。
-- =============================================

ALTER TABLE pc_assets
    ADD COLUMN device_type VARCHAR(50) NULL
        COMMENT '機器種別（code_master DEVICE_TYPE のコード値。例: LAPTOP, DESKTOP）'
    AFTER device_name;
