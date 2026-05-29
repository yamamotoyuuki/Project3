# 開発進捗記録

最終更新: 2026-05-26

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

## 次セッションへの引き継ぎ

**最後に完了したステップ**: codeChange01 完全完了

### 残り課題（オプション）
1. **操作ログ閲覧 UI**: 管理者向けに /admin/logs 画面追加
2. **テスト拡充**: LoanService / RentalService の単体テスト
3. **E2E テスト**: Playwright などでブラウザテスト
4. **本番設定**: 環境変数の整理、Docker Compose 本番設定

### 中断時の注意点
- application-dev.yml は localhost:3306/project3 (root/pass) 接続
- JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
- frontend-web の型チェック: `node_modules\.bin\vue-tsc --noEmit`
- backend コンパイル: `$env:JAVA_HOME = "..."; .\gradlew.bat compileJava`
- ログインパスワード: admin / Admin@1234
- エクスポート URL: /api/v1/export/assets.csv, /api/v1/export/assets.xlsx, /api/v1/export/loans.csv
- Flyway マイグレーション: V2（user_name カラム）、V3（agents/agent_history テーブル、pc_assets.agent_number カラム）が適用済みであること
