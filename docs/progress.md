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
- [ ] **3-8** 型定義追加: PcLoan, PcAcquisitionRental, RentalVendor, SoftwareMaster
- [ ] **3-9** API モジュール: loans.ts, rentals.ts, software.ts, users.ts
- [ ] **3-10** LoanListView: 貸出一覧・登録・返却
- [ ] **3-11** RentalListView: レンタル契約一覧・登録・期限アラート
- [ ] **3-12** SoftwareListView: ライセンス一覧・登録・使用数確認
- [ ] **3-13** UserListView: ユーザー一覧・登録（管理者のみ）
- [ ] **3-14** Router: /loans, /rentals, /software, /users 追加
- [ ] **3-15** Dashboard: 実データ反映

---

## 次セッションへの引き継ぎ

> このセクションは各ステップ完了後に更新する。

**最後に完了したステップ**: Phase 2 完了（コミット c561e2d）
**次にやること**: Phase 3-1 から順番に着手

### 中断時の注意点
- application-dev.yml は localhost:3306/project3 (root/pass) 接続
- JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
- frontend-web の型チェック: `node_modules\.bin\vue-tsc --noEmit`
- backend コンパイル: `$env:JAVA_HOME = "..."; .\gradlew.bat compileJava`
