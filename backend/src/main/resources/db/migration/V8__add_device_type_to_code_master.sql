-- =============================================
-- V8: code_master - Add DEVICE_TYPE codes
-- =============================================
-- Purpose: Register device type (kiki shubetsu) classifications into code_master.
--          These values represent the physical category of managed IT assets.
--
-- code_type  : DEVICE_TYPE (fixed English key)
-- code_name  : Japanese display name for the category
-- code_value : English constant stored in asset records
-- code_label : Japanese display label shown on screen
-- sort_order : Display order in dropdown / list views (10, 20, 30, ...)
-- =============================================

-- =============================================
-- Initial data: DEVICE_TYPE (device category / kiki shubetsu)
-- =============================================
INSERT INTO code_master (code_type, code_name, code_value, code_label, description, sort_order) VALUES
    -- Laptop PC (note-gata PC)
    ('DEVICE_TYPE', '機器種別', 'LAPTOP',
     'ノートPC（Laptop）',
     'Portable notebook / laptop computer',
     10),

    -- Desktop PC
    ('DEVICE_TYPE', '機器種別', 'DESKTOP',
     'デスクトップPC（Desktop）',
     'Stationary desktop computer',
     20),

    -- Monitor / Display
    ('DEVICE_TYPE', '機器種別', 'DISPLAY',
     'モニター（Display）',
     'External monitor or display unit',
     30),

    -- Keyboard (kiibo-do)
    ('DEVICE_TYPE', '機器種別', 'KEYBOARD',
     'キーボード',
     'Physical keyboard (USB, Bluetooth, or built-in)',
     40),

    -- Mouse (mausu)
    ('DEVICE_TYPE', '機器種別', 'MOUSE',
     'マウス',
     'Pointing device (wired or wireless mouse)',
     50),

    -- Smartphone: iPhone or Android
    ('DEVICE_TYPE', '機器種別', 'SMARTPHONE',
     'スマートフォン（iPhone / Android）',
     'Mobile smartphone running iOS (iPhone) or Android',
     60),

    -- Tablet: iPad or Android tablet
    ('DEVICE_TYPE', '機器種別', 'TABLET',
     'タブレット（iPad / Android）',
     'Tablet device running iPadOS (iPad) or Android',
     70),

    -- Mobile Router (portable Wi-Fi router)
    ('DEVICE_TYPE', '機器種別', 'MOBILE_ROUTER',
     'モバイルルータ（Wi-Fiルータ）',
     'Portable Wi-Fi router or mobile broadband device',
     80),

    -- External Storage: HDD or SSD
    ('DEVICE_TYPE', '機器種別', 'EXTERNAL_STORAGE',
     '外付けストレージ（HDD/SSD）',
     'External hard disk drive (HDD) or solid-state drive (SSD)',
     90);
