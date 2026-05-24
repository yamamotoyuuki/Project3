# 開発進捗記録

最終更新: 2026-05-24

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

---

## 🔄 Phase 3 - 進行中

### 作業計画（完了したらチェック）

#### Backend
- [x] **3-1** エンティティ追加: PcLoan, RentalVendor, PcAcquisitionRental, SoftwareMaster
- [x] **3-2** 貸出管理 API: LoanMapper(+XML) / LoanService / LoanController
- [x] **3-3** レンタル管理 API: RentalMapper(+XML) / RentalService / RentalController (ベンダー含む)
- [x] **3-4** ソフトウェアライセンス API: SoftwareMapper(+XML) / SoftwareService / SoftwareController
- [x] **3-5** ユーザー管理 API: UserService / UserController (UserMapper 拡張)
- [x] **3-6** エージェント受信 API: AgentMapper(+XML) / AgentService / AgentController
- [x] **3-7** ダッシュボード更新: 全統計を実データ化 (LoanMapper/RentalMapper/SoftwareMapper 使用)
- コンパイル確認: BUILD SUCCESSFUL

#### Frontend
- [x] **3-8** 型定義追加: PcLoan, PcAcquisitionRental, RentalVendor, SoftwareLicense, SystemUser
- [x] **3-9** API モジュール: loans.ts, rentals.ts, software.ts, users.ts
- [x] **3-10** LoanListView: 貸出一覧・登録・返却（期限超過の赤ハイライト）
- [x] **3-11** RentalListView: 契約一覧・ベンダー管理・契約登録・返却（期限切れ行ハイライト）
- [x] **3-12** SoftwareListView: ライセンス一覧・超過フィルタ・CRUD
- [x] **3-13** UserListView: ユーザー一覧・追加・編集（管理者のみアクセス可）
- [x] **3-14** Router: /loans, /rentals, /software, /users 追加
- [x] **3-15** Dashboard: 貸出中KPIカード追加・クイックリンク有効化
- コンパイル/ビルド確認: BUILD SUCCESSFUL (backend) / ✓ built in 1.00s (frontend)

---

## ✅ Phase 3 完了

**コミット**: `a6810a8`

---

## 次セッションへの引き継ぎ

**最後に完了したステップ**: Phase 3 完全完了（コミット a6810a8）

### 残り課題（Phase 4 候補）
1. **Tauri エージェント実装**: Rust で sysinfo クレートを使いハードウェア情報収集 → POST /api/v1/agent/report
2. **CSV/Excel エクスポート**: Apache POI / OpenCSV はビルド依存関係に既に含まれている
3. **操作ログ機能**: operation_logs テーブルは作成済み、ロギングAOPを追加
4. **テスト**: Spring Boot Test + MyBatis Test でAPIテスト
5. **本番設定**: GitHub Actions CI/CD、Dockerマルチステージビルド最適化

### 中断時の注意点
- application-dev.yml は localhost:3306/project3 (root/pass) 接続
- JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
- frontend-web の型チェック: `node_modules\.bin\vue-tsc --noEmit`
- backend コンパイル: `$env:JAVA_HOME = "..."; .\gradlew.bat compileJava`
- ログインパスワード: admin / Admin@1234
