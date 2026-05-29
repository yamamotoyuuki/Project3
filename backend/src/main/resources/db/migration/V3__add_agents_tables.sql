-- ============================================================
-- V3: エージェント管理テーブルの追加
--
-- 1. agents        : エージェントマスタ（agent_number を PK として各テーブルを紐づける）
-- 2. agent_history : エージェント履歴（現時点ではテーブル作成のみ）
-- 3. pc_assets     : agent_number カラムを追加（agents テーブルとの紐付け）
-- ============================================================

-- =============================================
-- agents: エージェントマスタ
-- agent_number を PRIMARY KEY として各種テーブルを紐づける
-- =============================================
CREATE TABLE IF NOT EXISTS agents (
    agent_number VARCHAR(50)  NOT NULL COMMENT 'エージェント番号（例: AGT-A1B2C3D4）',
    hostname     VARCHAR(200)          COMMENT 'エージェントが動作するPCのホスト名',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '初回登録日時',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最終更新日時',
    PRIMARY KEY (agent_number)
);

-- =============================================
-- agent_history: エージェント履歴
-- 現時点ではテーブル定義のみ（データ登録処理は将来実装）
-- =============================================
CREATE TABLE IF NOT EXISTS agent_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '履歴ID（主キー）',
    agent_number VARCHAR(50)  NOT NULL                COMMENT '対象エージェント番号（agents テーブルの FK）',
    event_type   VARCHAR(50)           COMMENT 'イベント種別（例: REGISTERED, UPDATED, REPORT_SENT）',
    note         VARCHAR(2000)         COMMENT '補足情報',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '記録日時',
    PRIMARY KEY (id),
    CONSTRAINT fk_agent_history_agent FOREIGN KEY (agent_number) REFERENCES agents(agent_number)
);

-- =============================================
-- pc_assets: agent_number カラムを追加
-- agents テーブルの PK（agent_number）で紐づける
-- =============================================
ALTER TABLE pc_assets
    ADD COLUMN agent_number VARCHAR(50) NULL
        COMMENT 'エージェント番号（agents テーブルとの紐付けキー）'
        AFTER hostname;
