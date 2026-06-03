-- =============================================
-- V7: code_master - Add code_name column
-- =============================================
-- Purpose: Add code_name column to code_master table.
--          This column holds the Japanese display name for each code category (code_type).
--          For example: code_type='PC_STATUS' -> code_name='PCステータス'
--          Position: inserted after code_type, before code_value.
-- =============================================

-- Add code_name column after code_type
-- DEFAULT '' is specified temporarily to avoid errors on existing rows (NOT NULL constraint).
ALTER TABLE code_master
    ADD COLUMN code_name VARCHAR(100) NOT NULL DEFAULT ''
        COMMENT 'Japanese name of the code category (e.g., PCステータス, 取得区分)'
    AFTER code_type;

-- =============================================
-- Populate code_name for existing V6 records
-- =============================================
UPDATE code_master SET code_name = 'PCステータス'           WHERE code_type = 'PC_STATUS';
UPDATE code_master SET code_name = '取得区分'               WHERE code_type = 'ACQUISITION_TYPE';
UPDATE code_master SET code_name = 'ユーザーロール'         WHERE code_type = 'USER_ROLE';
UPDATE code_master SET code_name = 'エージェントイベント種別' WHERE code_type = 'AGENT_EVENT_TYPE';
UPDATE code_master SET code_name = '操作種別'               WHERE code_type = 'OPERATION_TYPE';
