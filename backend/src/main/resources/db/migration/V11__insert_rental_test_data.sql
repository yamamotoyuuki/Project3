-- =============================================
-- V11: レンタル機器テストデータ投入
-- =============================================
-- 目的: レンタル管理画面の動作確認用テストデータを5件登録する。
--       基準日: 2026-06-04
--       内訳:
--         1件 - 期限切れ（rental_end_date が過去）
--         1件 - 残30日以内（まもなく期限切れ）
--         1件 - 残90日以内
--         2件 - 余裕あり（残100日超）
--       全件 return_date = NULL（未返却）
-- =============================================

-- =============================================
-- 1. レンタルベンダー（2社）
-- =============================================
INSERT INTO rental_vendors (company_name, contact_name, phone, email, address, note) VALUES
    -- ベンダー1: PCレンタル専業
    (
        '株式会社レンタルプロ',
        '田中 一郎',
        '03-1234-5678',
        'tanaka@rentalpro.example.com',
        '東京都千代田区丸の内1-1-1',
        'PCレンタル専業。3年契約が基本。'
    ),
    -- ベンダー2: ITリース全般
    (
        'ITリース株式会社',
        '佐藤 花子',
        '06-9876-5432',
        'sato@it-lease.example.com',
        '大阪府大阪市北区梅田2-2-2',
        'PCからモニターまで幅広く取り扱い。'
    );

-- =============================================
-- 2. PC資産（取得区分: RENTAL、5件）
-- =============================================
INSERT INTO pc_assets (
    asset_number, device_name, device_type, acquisition_type,
    maker, model_number, serial_number,
    location, status, note
) VALUES
    -- RENT-001: ノートPC（期限切れ状態で確認用）
    (
        'RENT-001',
        'ThinkPad X1 Carbon 2024',
        'LAPTOP',
        'RENTAL',
        'Lenovo',
        'ThinkPad X1 Carbon Gen12',
        'SN-LNV-001-2024',
        '東京本社 3F 開発部',
        'IN_USE',
        'レンタル期限切れ確認用テストデータ'
    ),
    -- RENT-002: ノートPC（残30日以内で警告表示確認用）
    (
        'RENT-002',
        'Let''s note CF-SV4',
        'LAPTOP',
        'RENTAL',
        'Panasonic',
        'CF-SV4RDAVS',
        'SN-PAN-002-2024',
        '東京本社 2F 営業部',
        'IN_USE',
        'レンタル期限30日以内確認用テストデータ'
    ),
    -- RENT-003: デスクトップPC（残90日以内で注意表示確認用）
    (
        'RENT-003',
        'OptiPlex 7090 Desktop',
        'DESKTOP',
        'RENTAL',
        'Dell',
        'OptiPlex 7090',
        'SN-DEL-003-2024',
        '大阪支社 1F サーバー室',
        'IN_USE',
        'レンタル期限90日以内確認用テストデータ'
    ),
    -- RENT-004: モニター（余裕あり）
    (
        'RENT-004',
        'ProLite XUB2492HSN',
        'DISPLAY',
        'RENTAL',
        'IODATA',
        'EX-LDQ271DB',
        'SN-IOD-004-2025',
        '東京本社 3F 開発部',
        'IN_USE',
        'モニターレンタル確認用テストデータ'
    ),
    -- RENT-005: タブレット（余裕あり）
    (
        'RENT-005',
        'iPad Pro 13インチ M4',
        'TABLET',
        'RENTAL',
        'Apple',
        'MXZY3J/A',
        'SN-APL-005-2025',
        '東京本社 4F 役員室',
        'IN_USE',
        'タブレットレンタル確認用テストデータ'
    );

-- =============================================
-- 3. レンタル契約（5件、全件 return_date = NULL）
-- =============================================
-- pc_asset_id・rental_vendor_id は直前の INSERT を参照するため
-- LAST_INSERT_ID() ではなくサブクエリで asset_number / company_name から取得する。
INSERT INTO pc_acquisition_rental (
    pc_asset_id, rental_vendor_id,
    contract_number,
    rental_start_date, rental_end_date,
    monthly_fee,
    return_date
)
SELECT
    a.id,
    v.id,
    c.contract_number,
    c.rental_start_date,
    c.rental_end_date,
    c.monthly_fee,
    NULL  -- 未返却
FROM (
    -- 契約データを定義するインラインビュー
    SELECT 'RENT-001' AS asset_number, '株式会社レンタルプロ' AS vendor_name,
           'RPC-2023-0042'  AS contract_number,
           '2023-06-01'     AS rental_start_date,
           '2026-05-31'     AS rental_end_date,  -- 基準日(2026-06-04)より前 = 期限切れ
           15000.00         AS monthly_fee
    UNION ALL
    SELECT 'RENT-002', '株式会社レンタルプロ',
           'RPC-2024-0018',
           '2024-07-01', '2026-06-24',           -- 残20日 = 30日以内（警告）
           25000.00
    UNION ALL
    SELECT 'RENT-003', 'ITリース株式会社',
           'ITL-2023-1105',
           '2023-09-01', '2026-08-01',           -- 残58日 = 90日以内（注意）
           22000.00
    UNION ALL
    SELECT 'RENT-004', 'ITリース株式会社',
           'ITL-2024-2201',
           '2024-01-01', '2026-12-31',           -- 残210日 = 余裕あり
           8000.00
    UNION ALL
    SELECT 'RENT-005', '株式会社レンタルプロ',
           'RPC-2025-0099',
           '2025-04-01', '2027-03-31',           -- 残300日 = 余裕あり
           12000.00
) AS c
JOIN pc_assets     a ON a.asset_number  = c.asset_number
JOIN rental_vendors v ON v.company_name = c.vendor_name;
