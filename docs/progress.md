# 開発進捗記録

最終更新: 2026-06-03

---

## 完了フェーズ

### ✅ Phase 1 - 環境構築・基盤整備
- commit: `1f64761`
- Docker Compose (MySQL + Backend + Frontend)
- Spring Boot: JWT認証、SecurityConfig、UserMapper、Flyway V1（全テーブル）
- Vue3: Pinia、Router、LoginView、DashboardView（スタブ）
- Tauri Agent: 骨格のみ

### ✅ Phase 2 前準備
- commit: `e9b5097`
- Employee / PcAsset / PcHardwareInfo エンティティ追加
- application-dev.yml 修正（localhost/project3 接続）

### ✅ Phase 2 - PC資産管理・社員管理
- commit: `c561e2d`
- **Backend**: PC資産 CRUD API、社員 CRUD API、ダッシュボード統計 API
  - PcAssetMapper/Service/Controller、EmployeeMapper/Service/Controller、DashboardController
  - PageResponse, AssetSearchRequest/CreateRequest/UpdateRequest/Response, Employee系 DTO
- **Frontend**: AppLayout, StatusBadge, AppPagination、AssetListView, AssetDetailView, EmployeeListView
- Router: /assets, /assets/:id, /employees 追加

### ✅ Phase 3 - 貸出・レンタル・ソフトウェア・ユーザー・エージェント管理
- commit: `a6810a8`
- **Backend**: 貸出/レンタル/ソフトウェア/ユーザー/エージェント管理 API
- **Frontend**: LoanListView, RentalListView, SoftwareListView, UserListView, Dashboard KPI更新
- コンパイル/ビルド確認: BUILD SUCCESSFUL (backend) / ✓ built in 1.00s (frontend)

---

## ✅ Phase 4 完了

### 4-1: 操作ログ機能（HandlerInterceptor）
- [x] `@Loggable` アノテーション作成（annotation/Loggable.java）
- [x] `OperationLog` エンティティ作成（domain/entity/OperationLog.java）
- [x] `OperationLogMapper` + XML 作成
- [x] `OperationLogService` 作成（@Async + REQUIRES_NEW で非同期書き込み）
- [x] `OperationLoggingInterceptor` 作成（HandlerInterceptor、新規依存不要）
- [x] `WebMvcConfig` でインターセプター登録
- [x] PcAssetController / LoanController に `@Loggable` 付与
- [x] `@EnableAsync` を PcMgmtApplication に追加

### 4-2: CSV/Excel エクスポート機能
- [x] `ExportService` 作成（Apache POI + 標準 PrintWriter）
  - exportAssetsCsv(): PC資産一覧 CSV（UTF-8 BOM付き）
  - exportAssetsExcel(): PC資産一覧 Excel (.xlsx)
  - exportLoansCsv(): 貸出一覧 CSV
- [x] `ExportController` 作成
  - GET /api/v1/export/assets.csv
  - GET /api/v1/export/assets.xlsx
  - GET /api/v1/export/loans.csv
- [x] AssetListView に CSV/Excelダウンロードボタン追加

### 4-3: Tauri エージェント完成
- [x] `lib.rs` 完成: ディスク情報（sysinfo::Disks）
- [x] ネットワーク IP（UDPソケットトリック）
- [x] ソフトウェア一覧（OS別コマンド: PowerShell/system_profiler/dpkg/rpm）
- [x] `App.vue` コメント追加・資産番号未設定警告・ディスク/ソフトウェア件数表示

### 4-4: GitHub Actions CI/CD
- [x] `.github/workflows/ci.yml` 作成
  - backend-build: Gradle build + test（MySQL サービスコンテナ付き）
  - frontend-build: npm ci + vue-tsc --noEmit + npm run build
  - docker-build: バックエンド/フロントエンドの Docker イメージビルド確認

### 4-5: テスト実装
- [x] `application-test.yml`（H2 インメモリ DB）
- [x] `PcAssetServiceTest`（Mockito 単体テスト: findAll/findById/create/update/delete）
- [x] `PcAssetControllerTest`（MockMvc WebMvcTest: 200/201/400/401/404 ステータス確認）

### ビルド確認
- Backend compileJava: BUILD SUCCESSFUL
- Backend compileTestJava: BUILD SUCCESSFUL
- Frontend: ✓ built in 1.25s

---

---

## ✅ codeChange01 完了（2026-05-26）

### 仕様変更内容: エージェント番号システムの実装

#### バックエンド変更
- [x] `V3__add_agents_tables.sql`: agents テーブル（agent_number PK）・agent_history テーブル新設、pc_assets に agent_number カラム追加
- [x] `Agent.java`: エージェントエンティティ新規作成
- [x] `AgentRegisterRequest.java`: 初回登録リクエスト DTO 新規作成
- [x] `AgentMapper.java`: findAgentByNumber / insertAgent / updateAgent 追加、updateAgentLastSeen に agentNumber パラメータ追加
- [x] `AgentMapper.xml`: 上記3メソッドの SQL 追加、updateAgentLastSeen に agent_number 条件更新追加
- [x] `AgentService.java`: register() メソッド追加（AGT-XXXXXXXX 形式発行）、processReport() に isNew フラグパターン実装
- [x] `AgentController.java`: POST /api/v1/agent/register エンドポイント追加
- [x] `PcAsset.java`: agentNumber フィールド追加
- [x] `AssetResponse.java`: agentNumber フィールド追加
- [x] `PcAssetMapper.xml`: resultMap・findAll・findById に agent_number 追加

#### エージェントアプリ変更
- [x] `lib.rs`: AgentReport に agent_number: Option<String> 追加、register_agent Tauri コマンド追加（invoke_handler 登録済み）
- [x] `App.vue`: agentNumber ref・registerAgent()・onMounted の初期化シーケンス（PC情報収集→エージェント番号確認→未取得なら登録）、sendReport に agent_number 含める、設定タブにエージェント番号読み取り専用表示追加

#### フロントエンド管理画面変更
- [x] `types/index.ts`: PcAsset に agentNumber フィールド追加
- [x] `AssetListView.vue`: エージェント番号カラム追加（colspan も 9 に更新）

---

---

## ✅ コードマスタ追加（2026-06-03）

### 作業内容: code_master テーブル（汎用コードマスタ）の追加

#### 追加ファイル
- [x] `backend/src/main/resources/db/migration/V6__create_code_master_table.sql`
  - `code_master` テーブル CREATE
  - 初期データ INSERT: PC_STATUS（3件）、ACQUISITION_TYPE（2件）、USER_ROLE（3件）、AGENT_EVENT_TYPE（3件）、OPERATION_TYPE（4件）

#### 更新ドキュメント
- [x] `データベース設計.md`: テーブル数 16→17、テーブル一覧追加、ER図更新、4.17 詳細定義追加、マイグレーション履歴V6追記、設計上の注意点6.6追記、改訂履歴1.1追記

---

## ✅ code_master に DEVICE_TYPE（機器種別）追加（2026-06-03）

### 作業内容: code_master テーブルへの機器種別区分値の追加

#### 追加ファイル
- [x] `backend/src/main/resources/db/migration/V8__add_device_type_to_code_master.sql`
  - `DEVICE_TYPE`（機器種別）として9件の区分値を INSERT
  - LAPTOP / DESKTOP / DISPLAY / KEYBOARD / MOUSE / SMARTPHONE / TABLET / MOBILE_ROUTER / EXTERNAL_STORAGE

#### 更新ドキュメント
- [x] `データベース設計.md`: 4.17 に DEVICE_TYPE 区分値テーブル追加、マイグレーション履歴V8追記、改訂履歴1.3追記

---

---

## ✅ コードマスタ共通 API 追加（2026-06-03）

### 作業内容: code_master テーブルからコード値を取得する共通 API の実装

#### 追加ファイル
- [x] `api/dto/response/common/CodeValueResponse.java`
  - code_value（コード値）、code_label（表示ラベル）を持つレスポンス DTO
- [x] `domain/mapper/common/CodeMasterMapper.java`
  - MyBatis マッパーインターフェース（findActiveByCodeType）
- [x] `resources/.../mapper/common/CodeMasterMapper.xml`
  - SQL: code_type=#{codeType} AND is_active=1 ORDER BY sort_order ASC
- [x] `service/common/CodeMasterService.java`
  - findActiveByCodeType(String codeType): List<CodeValueResponse> を返す
- [x] `api/controller/common/CommonController.java`
  - GET /api/v1/common/codes/{codeType} エンドポイント
- [x] `constant/ApiConstants.java`: COMMON_PATH 定数追加

#### エンドポイント仕様
- URL: `GET /api/v1/common/codes/{codeType}`
- 認証: JWT 認証必須
- パスパラメータ: codeType（例: PC_STATUS, DEVICE_TYPE, ACQUISITION_TYPE）
- レスポンス: `[{codeValue: "...", codeLabel: "..."}, ...]`
- 条件: is_active=1 のみ / sort_order ASC

#### 副作用修正
- [x] 既存 Java ファイル 54件の UTF-8 BOM を一括除去（コンパイルエラー解消）
- ビルド確認: `BUILD SUCCESSFUL`

---

---

## ✅ code_master から id カラム削除（2026-06-03）

### 作業内容: code_master テーブルの id（surrogate key）削除・複合主キーへの変更

#### 追加ファイル
- [x] `backend/src/main/resources/db/migration/V9__drop_id_from_code_master.sql`
  - id の AUTO_INCREMENT 除去 → PRIMARY KEY 削除 → UNIQUE 制約削除 → idx_code_master_type 削除 → id カラム削除 → (code_type, code_value) を PRIMARY KEY に追加

#### 更新ドキュメント
- [x] `データベース設計.md`: 4.17 カラム定義・インデックス定義を更新、マイグレーション履歴 V9 追記、改訂履歴 1.4 追記

#### 変更なしファイル（id を参照していないため）
- `CodeMasterMapper.java` / `CodeMasterMapper.xml` / `CodeMasterService.java` / `CommonController.java`

---

---

## ✅ pc_assets に device_type カラム追加（2026-06-03）

### 作業内容: 機器種別（device_type）の DB 保持対応

#### バックエンド（7ファイル）
- [x] `V10__add_device_type_to_pc_assets.sql`: pc_assets に device_type カラム追加（device_name の直後、NULL 許容）
- [x] `PcAsset.java`: deviceType フィールド追加
- [x] `AssetCreateRequest.java`: deviceType フィールド追加（任意）
- [x] `AssetUpdateRequest.java`: deviceType フィールド追加（任意）
- [x] `AssetResponse.java`: deviceType フィールド追加
- [x] `PcAssetMapper.xml`: resultMap・findAll・findById の SELECT、INSERT、UPDATE に device_type を追加
- [x] `PcAssetService.java`: create() / update() で deviceType をセット（空文字は null 変換）

#### フロントエンド（3ファイル）
- [x] `types/index.ts`: PcAsset に deviceType 追加、AssetCreateRequest / AssetUpdateRequest に deviceType? 追加
- [x] `WEB_AST0101_AssetListView.vue`: saveAsset() で deviceType を送信
- [x] `WEB_AST0102_AssetDetailView.vue`: openEdit() で登録済み deviceType を初期表示、saveAsset() で送信

#### ビルド確認
- Backend compileJava: BUILD SUCCESSFUL
- Frontend 型チェック: エラーなし

---

---

## ✅ エージェント認証 フェーズ2 完了（2026-06-07）

### 作業内容: 登録トークン（Enrollment Token）+ デバイス固有APIキー方式の実装

#### バックエンド
- [x] `V13__add_enrollment_token_and_api_key.sql`: agent_enrollment_tokens テーブル新設、agents に api_key_hash カラム追加
- [x] `AgentEnrollmentToken.java`: エンティティ新規作成
- [x] `Agent.java`: apiKeyHash フィールド追加
- [x] `AgentEnrollmentTokenMapper.java` + `AgentEnrollmentTokenMapper.xml`: insert / findByToken / markAsUsed / findAll / deleteById
- [x] `AgentMapper.java` + `AgentMapper.xml`: updateApiKeyHash / findApiKeyHashByAgentNumber 追加
- [x] `AgentRegisterRequest.java`: enrollmentToken フィールド追加
- [x] `AgentRegisterResponse.java`: 新規作成（agentNumber + apiKey）
- [x] `EnrollmentTokenRequest.java` / `EnrollmentTokenResponse.java`: 新規作成
- [x] `EnrollmentTokenService.java`: issue / validateToken / findAll / deleteById / toResponse（状態判定）
- [x] `AgentService.java`: register() をトークン検証→AGT番号発行→APIキー生成→bcryptハッシュ保存→トークン消費に刷新、validateApiKey() 追加
- [x] `AgentController.java`: register / report / asset-info エンドポイントに APIキー検証（Bearer）追加
- [x] `EnrollmentTokenController.java`: POST/GET/DELETE /api/v1/agent-tokens エンドポイント新規作成
- [x] `JwtTokenProvider.java`: getUserId() メソッド追加
- [x] `SecurityConfig.java`: agent-tokens エンドポイントのコメント整理

#### フロントエンド（管理画面）
- [x] `icon-token.svg`: 鍵アイコン画像追加（imageフォルダ規約準拠）
- [x] `AppLayout.vue`: 登録トークン管理ナビアイテム追加（ADMIN/OPERATOR 表示）
- [x] `router/index.ts`: /agent-tokens ルート追加
- [x] `api/agentTokens.ts`: issue / findAll / delete API クライアント新規作成
- [x] `types/index.ts`: EnrollmentToken / EnrollmentTokenCreateRequest インターフェース追加
- [x] `WEB_AGT0101_AgentTokenView.vue`: トークン発行・一覧・削除画面新規作成

#### エージェント（Tauri）
- [x] `lib.rs`: ApiSection に enrollment_token フィールド追加、AgentRegisterResponse 構造体追加、AGENT_API_KEY_FILE 定数追加、load_api_key / save_api_key コマンド追加、register_agent を enrollment_token 対応に変更、send_report / fetch_asset_acquisition_type に api_key パラメータ追加
- [x] `App.vue`: ApiConfig に enrollment_token フィールド追加、apiKey ref 追加、registerAgent() をトークン送信・APIキー保存に刷新、fetchAcquisitionType() に apiKey 追加、sendReport() に apiKey 追加、onMounted に load_api_key 追加、onRegisterConfirm() にエラーハンドリング追加

#### セキュリティ設計
- トークン: UUID（ハイフン除去）、24時間有効、1回限り使用
- APIキー: 48バイト SecureRandom → Base64URL（パディングなし）= 64文字
- APIキー保存: bcryptハッシュをDB保存（平文は返却時のみ）
- エージェント端末: .agent_key 隠しファイルに平文保存

---

---

## ✅ エージェント バグ修正群（2026-06-07）

### 修正1: application.yml の enrollment_token → UI 入力方式に変更
- `agent/src-tauri/src/lib.rs`: ApiSection に enrollment_token フィールド追加（`#[serde(default)]`）、フォールバック時に enrollment_token: None を設定
- `agent/src/App.vue`: モーダルに登録トークン入力欄追加、enrollmentTokenInput ref・registerAgent() 引数化

### 修正2: 登録モーダルでトークンが空と判定されるバグ
- 原因: closeRegisterModal() が enrollmentTokenInput をリセットした後に registerAgent() が値を読んでいた
- 修正: const token = enrollmentTokenInput.value.trim() でモーダルを閉じる前に退避

### 修正3: 登録結果を中央ポップアップ表示に変更
- registerPopup ref・showRegisterPopup()・closeRegisterPopup() 追加
- `.register-popup-overlay` / `.register-popup-content` スタイル追加

### 修正4: エージェントID未取得時のUI制限
- PC情報タブ・送信タブを v-if="agentNumber" で非表示に変更
- WindowsUpdate ボタンを :disabled="!agentNumber" に変更
- currentView 初期値を 'settings' に変更
- onMounted: agentNumber 確認後のみ collectInfo() 実行

### 修正5: 未登録 PC のlocalStorage 残存値をクリア
- clearSettings() 関数追加（localStorage + settings ref 両方クリア）
- onMounted で agentNumber 未取得時に clearSettings() 呼び出し

### 修正6: TypeScript/Rust serde キー不一致（camelCase 修正）
- Rust AgentRegisterResponse は `#[serde(rename = "agentNumber")]` / `#[serde(rename = "apiKey")]`
- TS 型を `{ agentNumber: string; apiKey: string }` に修正
- result.agent_number → result.agentNumber、result.api_key → result.apiKey

### 修正7: 未登録PC での send_report 400 エラー（根本原因修正）
- 原因: 未登録状態では onMounted の collectInfo() がスキップされ pcInfo.value = null。
         registerAgent() 内で `if (!pcInfo.value) return` がサイレントリターンし、
         agentNumber/apiKey が設定されないまま sendReport() が呼ばれ Bearer トークンが空で 400。
- 修正:
  - `App.vue` の onRegisterConfirm(): registerAgent() 呼び出し前に `if (!pcInfo.value)` 時は
    `pcInfo.value = await invoke<PcInfo>('collect_pc_info')` でホスト名を先取得
  - `App.vue` の registerAgent(): サイレントリターンを `throw new Error(...)` に変更
    （呼び出し元で成功と誤判定されないようにする）

---

## 次セッションへの引き継ぎ

**最後に完了したステップ**: エージェント send_report 400 エラーの根本原因修正完了

### 残り課題（オプション）
1. **操作ログ閲覧 UI**: 管理者向けに /admin/logs 画面追加
2. **テスト拡充**: LoanService / RentalService / EnrollmentTokenService の単体テスト
3. **E2E テスト**: Playwright などでブラウザテスト
4. **本番設定**: 環境変数の整理、Docker Compose 本番設定
5. **要件定義書更新**: 13.1.2 に登録トークン + APIキー方式を追記（任意）

### 中断時の注意点
- application-dev.yml は localhost:3306/project3 (root/pass) 接続
- JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
- frontend-web の型チェック: `node_modules\.bin\vue-tsc --noEmit`
- backend コンパイル: `$env:JAVA_HOME = "..."; .\gradlew.bat compileJava`
- ログインパスワード: admin / Admin@1234
- エクスポート URL: /api/v1/export/assets.csv, /api/v1/export/assets.xlsx, /api/v1/export/loans.csv
- Flyway マイグレーション: V13 が追加済み（agent_enrollment_tokens テーブル + agents.api_key_hash カラム）
- エージェント登録フロー: 管理者が Web 画面でトークン発行 → エージェントアプリの「新規登録」モーダルに直接入力 → 登録ボタン
