-- =============================================
-- V6: code_master - General-purpose code master table
-- =============================================
-- Purpose: Centrally manage all code values used throughout the system.
-- Instead of hardcoding enum-like values in the application, this table
-- provides a single place to add/modify code values and their display labels.
--
-- Usage:
--   - code_type  : Category identifier (e.g., PC_STATUS, ACQUISITION_TYPE)
--   - code_value : Actual code string stored in other tables (e.g., IN_USE)
--   - code_label : Human-readable display label (e.g., 使用中)
-- =============================================

-- =============================================
-- code_master: General code master (汎用コードマスタ)
-- =============================================
CREATE TABLE IF NOT EXISTS code_master (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Internal ID',
    code_type   VARCHAR(50)   NOT NULL               COMMENT 'Code category key (e.g., PC_STATUS, ACQUISITION_TYPE, USER_ROLE)',
    code_value  VARCHAR(50)   NOT NULL               COMMENT 'Code value stored in application tables (e.g., IN_USE, PURCHASE)',
    code_label  VARCHAR(200)  NOT NULL               COMMENT 'Display label shown on screen (e.g., 使用中, 購入)',
    description VARCHAR(500)                         COMMENT 'Supplemental description of this code value',
    sort_order  INT           NOT NULL DEFAULT 0     COMMENT 'Display sequence number within the same code_type (e.g., 1, 2, 3 ... shown in this order on screen)',
    is_active   TINYINT(1)    NOT NULL DEFAULT 1     COMMENT 'Active flag: 1=active, 0=inactive (hidden from UI)',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT 'Record creation datetime',
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update datetime',
    PRIMARY KEY (id),
    -- Composite unique: one code_value per code_type
    CONSTRAINT uq_code_master_type_value UNIQUE (code_type, code_value),
    -- Index for frequent lookup by code_type
    INDEX idx_code_master_type (code_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='General-purpose code master. Manages all system-wide code values and their display labels.';

-- =============================================
-- Initial data: PC_STATUS (PC resource status)
-- Corresponds to pc_assets.status column values
-- =============================================
INSERT INTO code_master (code_type, code_value, code_label, description, sort_order) VALUES
    ('PC_STATUS', 'IN_STORAGE', '保管中',   'PC is in storage and not currently assigned to any employee', 10),
    ('PC_STATUS', 'IN_USE',     '使用中',   'PC is assigned to an employee and currently in active use',   20),
    ('PC_STATUS', 'DISPOSED',   '廃棄済み', 'PC has been disposed of and is no longer in service',         30);

-- =============================================
-- Initial data: ACQUISITION_TYPE (PC acquisition type)
-- Corresponds to pc_assets.acquisition_type column values
-- =============================================
INSERT INTO code_master (code_type, code_value, code_label, description, sort_order) VALUES
    ('ACQUISITION_TYPE', 'PURCHASE', '購入',     'PC was purchased outright; details in pc_acquisition_purchase', 10),
    ('ACQUISITION_TYPE', 'RENTAL',   'レンタル', 'PC is rented from a vendor; details in pc_acquisition_rental', 20);

-- =============================================
-- Initial data: USER_ROLE (system user role)
-- Corresponds to users.role column values
-- =============================================
INSERT INTO code_master (code_type, code_value, code_label, description, sort_order) VALUES
    ('USER_ROLE', 'ADMIN',    '管理者',         'Full access including user management',                  10),
    ('USER_ROLE', 'OPERATOR', 'オペレーター',   'Can register and edit assets, employees, and loans',    20),
    ('USER_ROLE', 'VIEWER',   '閲覧者',         'Read-only access; cannot create or modify records',     30);

-- =============================================
-- Initial data: AGENT_EVENT_TYPE (agent event types)
-- Corresponds to agent_history.event_type column values
-- =============================================
INSERT INTO code_master (code_type, code_value, code_label, description, sort_order) VALUES
    ('AGENT_EVENT_TYPE', 'REGISTERED',   '登録',           'Agent was newly registered to the system',        10),
    ('AGENT_EVENT_TYPE', 'UPDATED',      '更新',           'Agent information was updated',                   20),
    ('AGENT_EVENT_TYPE', 'REPORT_SENT',  'レポート送信',   'Agent sent a hardware/software collection report', 30);

-- =============================================
-- Initial data: OPERATION_TYPE (operation log types)
-- Corresponds to operation_logs.operation column values
-- =============================================
INSERT INTO code_master (code_type, code_value, code_label, description, sort_order) VALUES
    ('OPERATION_TYPE', 'CREATE', '登録', 'New record was created',          10),
    ('OPERATION_TYPE', 'UPDATE', '更新', 'Existing record was updated',     20),
    ('OPERATION_TYPE', 'DELETE', '削除', 'Existing record was deleted',     30),
    ('OPERATION_TYPE', 'EXPORT', 'エクスポート', 'Data was exported to file', 40);
