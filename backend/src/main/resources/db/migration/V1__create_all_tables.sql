-- =============================================
-- PC管理システム 初期スキーマ作成
-- V1: 全テーブル作成 (MySQL 9.x / H2 互換)
-- =============================================

-- =============================================
-- users: システムユーザー
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
    email         VARCHAR(200),
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    last_login_at DATETIME,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- =============================================
-- employees: 社員マスタ
-- =============================================
CREATE TABLE IF NOT EXISTS employees (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    employee_code VARCHAR(50)  NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    department    VARCHAR(100),
    position      VARCHAR(100),
    email         VARCHAR(200),
    phone         VARCHAR(30),
    location      VARCHAR(200),
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_employees_code UNIQUE (employee_code)
);

-- =============================================
-- pc_assets: PC資産マスタ
-- =============================================
CREATE TABLE IF NOT EXISTS pc_assets (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    asset_number         VARCHAR(50)  NOT NULL,
    device_name          VARCHAR(200) NOT NULL,
    acquisition_type     VARCHAR(20)  NOT NULL,
    maker                VARCHAR(100),
    model_number         VARCHAR(100),
    serial_number        VARCHAR(100),
    location             VARCHAR(200),
    status               VARCHAR(20)  NOT NULL DEFAULT 'IN_STORAGE',
    assigned_employee_id BIGINT,
    hostname             VARCHAR(200),
    agent_last_seen      DATETIME,
    note                 VARCHAR(2000),
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_pc_assets_number UNIQUE (asset_number)
);

-- =============================================
-- pc_acquisition_purchase: 購入PC情報
-- =============================================
CREATE TABLE IF NOT EXISTS pc_acquisition_purchase (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    pc_asset_id    BIGINT         NOT NULL,
    purchase_date  DATE,
    purchase_price DECIMAL(12, 2),
    warranty_expiry DATE,
    supplier       VARCHAR(200),
    note           VARCHAR(2000),
    PRIMARY KEY (id),
    CONSTRAINT uq_purchase_asset UNIQUE (pc_asset_id),
    CONSTRAINT fk_purchase_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE
);

-- =============================================
-- rental_vendors: レンタルベンダーマスタ
-- =============================================
CREATE TABLE IF NOT EXISTS rental_vendors (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(200) NOT NULL,
    contact_name VARCHAR(100),
    phone        VARCHAR(30),
    email        VARCHAR(200),
    address      VARCHAR(300),
    note         VARCHAR(2000),
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- =============================================
-- pc_acquisition_rental: レンタルPC契約情報
-- =============================================
CREATE TABLE IF NOT EXISTS pc_acquisition_rental (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    pc_asset_id        BIGINT         NOT NULL,
    rental_vendor_id   BIGINT         NOT NULL,
    contract_number    VARCHAR(100),
    rental_start_date  DATE           NOT NULL,
    rental_end_date    DATE           NOT NULL,
    monthly_fee        DECIMAL(10, 2),
    contract_file_path VARCHAR(500),
    return_date        DATE,
    returned_by        BIGINT,
    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_rental_asset UNIQUE (pc_asset_id),
    CONSTRAINT fk_rental_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_rental_vendor FOREIGN KEY (rental_vendor_id) REFERENCES rental_vendors(id)
);

-- =============================================
-- pc_rental_history: レンタル履歴
-- =============================================
CREATE TABLE IF NOT EXISTS pc_rental_history (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    pc_asset_id       BIGINT         NOT NULL,
    rental_vendor_id  BIGINT         NOT NULL,
    contract_number   VARCHAR(100),
    rental_start_date DATE           NOT NULL,
    rental_end_date   DATE           NOT NULL,
    monthly_fee       DECIMAL(10, 2),
    return_date       DATE,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rental_hist_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_rental_hist_vendor FOREIGN KEY (rental_vendor_id) REFERENCES rental_vendors(id)
);

-- =============================================
-- pc_hardware_info: ハードウェア情報
-- =============================================
CREATE TABLE IF NOT EXISTS pc_hardware_info (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    pc_asset_id  BIGINT         NOT NULL,
    cpu_model    VARCHAR(200),
    cpu_cores    INT,
    memory_gb    DECIMAL(6,1),
    disk_gb      DECIMAL(8,1),
    disk_free_gb DECIMAL(8,1),
    collected_at DATETIME,
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_hardware_asset UNIQUE (pc_asset_id),
    CONSTRAINT fk_hardware_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE
);

-- =============================================
-- pc_software_info: インストール済みソフトウェア
-- =============================================
CREATE TABLE IF NOT EXISTS pc_software_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    pc_asset_id   BIGINT       NOT NULL,
    software_name VARCHAR(300) NOT NULL,
    version       VARCHAR(100),
    publisher     VARCHAR(200),
    install_date  DATE,
    collected_at  DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_software_info_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE
);

-- =============================================
-- pc_network_info: ネットワーク情報
-- =============================================
CREATE TABLE IF NOT EXISTS pc_network_info (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    pc_asset_id  BIGINT       NOT NULL,
    nic_name     VARCHAR(100),
    ip_address   VARCHAR(45),
    mac_address  VARCHAR(17),
    collected_at DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_network_info_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE
);

-- =============================================
-- software_master: ソフトウェアライセンスマスタ
-- =============================================
CREATE TABLE IF NOT EXISTS software_master (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    software_name   VARCHAR(300) NOT NULL,
    publisher       VARCHAR(200),
    license_type    VARCHAR(100),
    purchased_count INT          NOT NULL DEFAULT 0,
    note            VARCHAR(2000),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- =============================================
-- pc_assignments: PC割当履歴
-- =============================================
CREATE TABLE IF NOT EXISTS pc_assignments (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    pc_asset_id   BIGINT   NOT NULL,
    employee_id   BIGINT   NOT NULL,
    assigned_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at DATETIME,
    note          VARCHAR(2000),
    created_by    BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_assignments_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- =============================================
-- pc_loans: PC貸出管理
-- =============================================
CREATE TABLE IF NOT EXISTS pc_loans (
    id                   BIGINT   NOT NULL AUTO_INCREMENT,
    pc_asset_id          BIGINT   NOT NULL,
    employee_id          BIGINT   NOT NULL,
    loan_date            DATE     NOT NULL,
    expected_return_date DATE,
    actual_return_date   DATE,
    purpose              VARCHAR(300),
    note                 VARCHAR(2000),
    created_by           BIGINT   NOT NULL,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_loans_asset FOREIGN KEY (pc_asset_id) REFERENCES pc_assets(id),
    CONSTRAINT fk_loans_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_loans_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- =============================================
-- operation_logs: 操作ログ
-- =============================================
CREATE TABLE IF NOT EXISTS operation_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT,
    username    VARCHAR(100),
    operation   VARCHAR(50)  NOT NULL,
    target_type VARCHAR(100),
    target_id   BIGINT,
    detail      VARCHAR(4000),
    ip_address  VARCHAR(45),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- =============================================
-- 初期データ: 管理者ユーザー（パスワード: Admin@1234）
-- bcrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =============================================
-- パスワード: Admin@1234
INSERT INTO users (username, password_hash, display_name, role, email, is_active)
VALUES ('admin', '$2b$10$uB1EBTQ7y0g.ZZUf2RxZUOQ8nE.0FxFps6CgoEPt9pmpZbCM0uzM2', 'システム管理者', 'ADMIN', 'admin@example.com', 1);
