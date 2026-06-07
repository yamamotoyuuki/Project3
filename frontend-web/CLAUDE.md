# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 開発コマンド

```bash
# 開発サーバー起動（ポート 3000）
npm run dev

# 本番ビルド（vue-tsc による型チェック + Vite バンドル）
npm run build

# 型チェックのみ（ビルドなし）
npm run type-check

# ビルドプレビュー（本番ビルドをローカルで確認）
npm run preview
```

> 開発サーバーは `http://localhost:3000` で起動。`/api/*` へのリクエストは Vite プロキシが `http://localhost:8080`（Spring Boot バックエンド）に転送する。

## アーキテクチャ概要

### レイヤー構成

```
src/
├── api/          API クライアント（ドメインごとに 1 ファイル）
├── components/
│   ├── common/   全画面共通コンポーネント
│   └── RNT/      レンタル機能専用コンポーネント
├── image/        SVG アイコン画像（絵文字・CSS アイコン禁止）
├── router/       Vue Router 設定・ナビゲーションガード
├── stores/       Pinia ストア（現時点: auth のみ）
├── types/        TypeScript 型定義（単一ファイル types/index.ts に集約）
└── views/        ページコンポーネント（画面 ID に対応）
```

### 画面 ID と命名規則

ビュー名は `WEB_{モジュールコード}{画面番号}_{説明}View.vue` 形式。

| モジュールコード | 機能         |
|----------------|-------------|
| AUT            | 認証          |
| DSH            | ダッシュボード  |
| AST            | PC 資産管理   |
| EMP            | 社員管理      |
| LON            | 貸出管理      |
| RNT            | レンタル管理   |
| SFW            | ソフトウェア管理|
| USR            | ユーザー管理   |
| AGT            | エージェントトークン管理 |

### 認証フロー

- `src/stores/auth.ts` が JWT トークンと `CurrentUser` を Pinia で管理し、`localStorage` に永続化する。
- `src/api/axios.ts` の Axios インターセプタがリクエストに `Authorization: Bearer <token>` を自動付与する。401 レスポンスは `authStore.logout()` + `/login` リダイレクトで自動処理される。
- Vue Router の `beforeEach` ガードが `meta.requiresAuth` / `meta.requiresAdmin` を評価して未認証・権限不足をはじく。

### ロール権限

| ロール   | アクセス範囲                                   |
|---------|---------------------------------------------|
| ADMIN   | 全画面（ユーザー管理含む）                       |
| IT_STAFF| ユーザー管理以外の全画面（登録トークン管理含む）   |
| VIEWER  | 閲覧のみ（編集・削除系操作は UI 側で非表示 / 無効化）|

ストアのゲッター: `isAdmin`（ADMIN のみ）、`isItStaff`（ADMIN または IT_STAFF）。

### API クライアントパターン

全 API モジュールは `src/api/axios.ts` の `apiClient` を使用し、ドメインごとに名前付き `const` オブジェクトでエクスポートする。

```typescript
// 例: src/api/assets.ts
export const assetsApi = {
  findAll(params: AssetSearchParams): Promise<ApiResponse<PageResponse<PcAsset>>> { ... },
  findById(id: number): Promise<ApiResponse<PcAsset>> { ... },
}
```

すべてのレスポンスは `ApiResponse<T>` ラッパー（`code` / `message` / `data` / `timestamp`）で返る。一覧は `PageResponse<T>`（**0 始まりのページ番号**）。

### 型定義

`src/types/index.ts` がアプリ全体の型を一元管理する。新しい API エンティティや検索パラメータを追加するときは、このファイルに追記する。

### 共通コンポーネント

| コンポーネント          | 用途                                  |
|-----------------------|--------------------------------------|
| `AppLayout`           | ヘッダー + サイドバー + `<slot>` のシェル。全認証済み画面で使用。|
| `AppPagination`       | `v-model` でページ番号（0 始まり）を双方向バインド。|
| `StatusBadge`         | PC ステータス（`PcStatus`）をバッジ表示。      |
| `MultiSelectFilter`   | チェックボックス複数選択フィルタ。             |
| `AppConfirmDialog`    | 削除・返却などの確認ダイアログ。              |

### パスエイリアス

`@/` は `src/` に解決される（`tsconfig.json` と `vite.config.ts` 両方で設定済み）。

## コーディング規約（このディレクトリ固有）

- **アイコン**: `src/image/` に SVG で格納し、`import` してテンプレートで `<img :src="icon" />` として使う。絵文字・CSS アイコンフォント禁止。
- **画面遷移**: テンプレートの `@click` に直接 `router.push(...)` を書かず、`<script setup>` に遷移関数を定義して呼び出す。
- **型安全**: `any` 禁止。不明な型は `unknown` + 型ガード。
- **リストキー**: `index` を `:key` にしない。エンティティの `id` を使う。
- **ファイルサイズ**: 1 ファイル 300 行以内を目安。超える場合はコンポーネント分割。
- **TypeScript strict**: `noUnusedLocals` / `noUnusedParameters` が有効。未使用変数はビルドエラーになる。
