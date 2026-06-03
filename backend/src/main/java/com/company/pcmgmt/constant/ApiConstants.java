package com.company.pcmgmt.constant;

/**
 * API エンドポイントパス定数クラス
 *
 * <p>REST API のベースパスおよび各リソースのパスを一元管理する。
 * インスタンス化禁止のユーティリティクラス。</p>
 */
public final class ApiConstants {

    /** インスタンス化を禁止するプライベートコンストラクタ */
    private ApiConstants() {}

    /** API バージョンプレフィックス（例: /api/v1） */
    public static final String API_BASE = "/api/v1";

    /** 認証関連エンドポイント（ログイン・ログアウト） */
    public static final String AUTH_PATH = API_BASE + "/auth";

    /** PC資産管理エンドポイント */
    public static final String ASSETS_PATH = API_BASE + "/assets";

    /** エージェント情報収集エンドポイント（Tauri エージェントからの報告受信） */
    public static final String AGENT_PATH = API_BASE + "/agent";

    /** ソフトウェアライセンス管理エンドポイント */
    public static final String SOFTWARE_PATH = API_BASE + "/software";

    /** PC貸出管理エンドポイント */
    public static final String LOANS_PATH = API_BASE + "/loans";

    /** レンタルベンダー管理エンドポイント */
    public static final String RENTAL_VENDORS_PATH = API_BASE + "/rental-vendors";

    /** 社員管理エンドポイント */
    public static final String EMPLOYEES_PATH = API_BASE + "/employees";

    /** システムユーザー管理エンドポイント（管理者専用） */
    public static final String USERS_PATH = API_BASE + "/users";

    /**
     * Common utility endpoint (code master, shared lookups)
     * Usage: GET /api/v1/common/codes/{codeType}
     */
    public static final String COMMON_PATH = API_BASE + "/common";
}
