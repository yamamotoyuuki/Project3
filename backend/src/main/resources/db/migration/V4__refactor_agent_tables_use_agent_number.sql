-- ============================================================
-- V4: エージェント関連テーブルのリレーションを agent_number ベースに変更
--
-- 変更前: pc_hardware_info / pc_software_info / pc_network_info が
--         pc_asset_id（pc_assets.id の FK）で紐づいていた
-- 変更後: agent_number（agents.agent_number の FK）で紐づける
--
-- 理由: エージェント番号を主識別子として使用することで、
--       ホスト名やID変更に依存しない安定したリレーションを実現する
-- ============================================================

-- =============================================
-- pc_hardware_info: pc_asset_id → agent_number
-- =============================================

-- FK 制約と一意制約を削除する
ALTER TABLE pc_hardware_info DROP FOREIGN KEY fk_hardware_asset;
ALTER TABLE pc_hardware_info DROP INDEX uq_hardware_asset;

-- agent_number カラムを追加する
ALTER TABLE pc_hardware_info
    ADD COLUMN agent_number VARCHAR(50) NULL
        COMMENT 'エージェント番号（agents テーブルとの紐付けキー）'
        AFTER id;

-- 既存データを移行する（pc_assets.agent_number が設定済みの場合のみ）
UPDATE pc_hardware_info h
    JOIN pc_assets a ON h.pc_asset_id = a.id
    SET h.agent_number = a.agent_number
    WHERE a.agent_number IS NOT NULL;

-- エージェント未紐付けのレコードを削除する（agent_number が NULL = エージェント未導入）
DELETE FROM pc_hardware_info WHERE agent_number IS NULL;

-- 旧カラム（pc_asset_id）を削除する
ALTER TABLE pc_hardware_info DROP COLUMN pc_asset_id;

-- 新しい一意制約と FK 制約を追加する
ALTER TABLE pc_hardware_info
    ADD CONSTRAINT uq_hardware_agent UNIQUE (agent_number),
    ADD CONSTRAINT fk_hardware_agent
        FOREIGN KEY (agent_number) REFERENCES agents(agent_number)
        ON DELETE CASCADE;

-- =============================================
-- pc_software_info: pc_asset_id → agent_number
-- =============================================

-- FK 制約を削除する
ALTER TABLE pc_software_info DROP FOREIGN KEY fk_software_info_asset;

-- agent_number カラムを追加する
ALTER TABLE pc_software_info
    ADD COLUMN agent_number VARCHAR(50) NULL
        COMMENT 'エージェント番号（agents テーブルとの紐付けキー）'
        AFTER id;

-- 既存データを移行する
UPDATE pc_software_info s
    JOIN pc_assets a ON s.pc_asset_id = a.id
    SET s.agent_number = a.agent_number
    WHERE a.agent_number IS NOT NULL;

-- エージェント未紐付けのレコードを削除する
DELETE FROM pc_software_info WHERE agent_number IS NULL;

-- 旧カラムを削除する
ALTER TABLE pc_software_info DROP COLUMN pc_asset_id;

-- 新しい FK 制約を追加する
ALTER TABLE pc_software_info
    ADD CONSTRAINT fk_software_agent
        FOREIGN KEY (agent_number) REFERENCES agents(agent_number)
        ON DELETE CASCADE;

-- =============================================
-- pc_network_info: pc_asset_id → agent_number
-- =============================================

-- FK 制約を削除する
ALTER TABLE pc_network_info DROP FOREIGN KEY fk_network_info_asset;

-- agent_number カラムを追加する
ALTER TABLE pc_network_info
    ADD COLUMN agent_number VARCHAR(50) NULL
        COMMENT 'エージェント番号（agents テーブルとの紐付けキー）'
        AFTER id;

-- 既存データを移行する
UPDATE pc_network_info n
    JOIN pc_assets a ON n.pc_asset_id = a.id
    SET n.agent_number = a.agent_number
    WHERE a.agent_number IS NOT NULL;

-- エージェント未紐付けのレコードを削除する
DELETE FROM pc_network_info WHERE agent_number IS NULL;

-- 旧カラムを削除する
ALTER TABLE pc_network_info DROP COLUMN pc_asset_id;

-- 新しい FK 制約を追加する
ALTER TABLE pc_network_info
    ADD CONSTRAINT fk_network_agent
        FOREIGN KEY (agent_number) REFERENCES agents(agent_number)
        ON DELETE CASCADE;
