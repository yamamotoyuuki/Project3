-- =============================================
-- V13: エージェント登録トークン テーブルの追加 + agents テーブルへのAPIキーハッシュ追加
-- =============================================
-- 目的: エージェントアプリの初回登録をセキュア化する。
--       管理者がWebコンソールで発行した登録トークン（24時間有効・1回限り）を
--       エージェントのインストーラーに設定し、初回登録時にバックエンドで検証する。
--       登録成功後にデバイス固有のAPIキー（ハッシュ値）を発行して以降の通信を認証する。
-- =============================================

-- =============================================
-- 1. agent_enrollment_tokens テーブル（登録トークン管理）
-- =============================================
CREATE TABLE agent_enrollment_tokens (
    id                  BIGINT       NOT NULL AUTO_INCREMENT                   COMMENT 'レコードID',
    token               VARCHAR(64)  NOT NULL                                  COMMENT '登録トークン（UUID形式、1回限り有効）',
    expires_at          DATETIME     NOT NULL                                  COMMENT 'トークン有効期限（発行から24時間後）',
    used_at             DATETIME     NULL                                      COMMENT 'トークン使用日時（使用済みの場合のみ設定）',
    used_by_agent_number VARCHAR(20) NULL                                      COMMENT '使用したエージェント番号（使用済みの場合のみ設定）',
    note                VARCHAR(200) NULL                                      COMMENT '発行メモ（対象PC名など任意メモ）',
    created_by_user_id  BIGINT       NOT NULL                                  COMMENT 'トークン発行者のユーザーID（usersテーブルのID）',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP        COMMENT '発行日時',
    PRIMARY KEY (id),
    UNIQUE KEY uq_token (token),                    -- トークン文字列の重複を防ぐ
    KEY idx_token_expires (token, expires_at),      -- トークン検証時の複合インデックス
    CONSTRAINT fk_enrollment_token_user
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
) COMMENT='エージェント登録トークン管理テーブル（1回限り・24時間有効）';

-- =============================================
-- 2. agents テーブルへ api_key_hash カラムを追加
-- =============================================
-- 目的: エージェントごとに固有のAPIキーのbcryptハッシュを保持する。
--       平文のAPIキーはDBに保存せず、登録時に一度だけエージェントへ返却する。
--       以降の通信でエージェントがAPIキーをAuthorizationヘッダーで送信し、
--       バックエンドがbcryptで照合して認証する。
ALTER TABLE agents
    ADD COLUMN api_key_hash VARCHAR(100) NULL
        COMMENT 'APIキーのbcryptハッシュ（平文は保存しない。登録時に一度だけエージェントへ返却する）'
    AFTER hostname;
