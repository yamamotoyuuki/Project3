# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

This repository is currently empty and in its initial setup phase. Add project-specific guidance here as the codebase evolves.

## Repository Configuration

A `.claude/settings.local.json` exists with the following allowed shell permission:

```
Bash(grep -E "\\.\\(json|md|ts|js|py|toml|yaml\\)$")
```

Update this file with build commands, architecture notes, and development workflows once the project is initialized.

## Design Documents

要件定義・基本設計書を参照してください。
@PC管理システム_要件定義_設計書_v2_9.md

## 開発ルール
- 各フェーズ完了後は必ず作業内容を報告し、次の指示を待つこと
- ファイルを新規作成・削除する前に確認を取ること
- エラーが発生したら自己解決を3回試み、それでも無理なら報告すること
- セッションが途中で途切れることを想定して、こまめに現在の進捗をdocs/progress.mdに記録してください。次のセッションで再開できるよう、どこまで完了したかを書いてください。
- 生成する成果物には、すべてコメントを記入すること（処理を説明するコメント、変数名の論理名など）

## 技術スタック
| **区分** | **技術** | **バージョン** | **備考** |
| --- | --- | --- | --- |
| フロントエンド（Web） | Vue.js + TypeScript | Vue 3 / Vite | Composition API、Pinia（状態管理）※採用理由は下表参照 |
| フロントエンド（エージェント） | Vue.js + TypeScript + Tauri | Tauri 2.x | Rustバックエンド、クロスプラットフォーム |
| バックエンド API | Java / Spring Boot | 3.x / Java 21 | REST API、Spring Security（JWT） |
| データベース | MySQL | 8.x | MyBatis、Flyway（マイグレーション） |
| エージェント収集ロジック | Rust（Tauri） | 1.7+ | sysinfo クレートで情報収集 |
| コンテナ | Docker / Docker Compose | 最新安定版 | 開発・本番環境共通 |
| ビルドツール | Gradle | 最新安定版 | backendビルド・依存関係管理 |
| CIツール | GitHub Actions | ? | ビルド・テスト・デプロイ自動化 |

## データベース接続情報
ホスト名：localhost
パスワード：pass
データベース：project3