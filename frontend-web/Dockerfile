# ==============================================================
# frontend-web/Dockerfile
# マルチステージビルドで Vue SPA をビルドし、nginx で配信する
#
# [Stage 1] builder  : Node.js で npm install & npm run build を実行
# [Stage 2] runtime  : nginx に dist/ をコピーして軽量イメージを作成
# ==============================================================

# ==============================================================
# Stage 1: ビルドステージ
# Node.js 20（Alpine ベース）を使用して依存関係インストールとビルドを行う
# Alpine を選択することで中間イメージを軽量に保つ
# ==============================================================
FROM node:20-alpine AS builder

# 作業ディレクトリを設定
WORKDIR /app

# package.json を先にコピーして依存関係をキャッシュ活用でインストール
# (ソースコード変更のみの場合は npm install が再実行されない)
COPY package.json ./
RUN npm install

# ソースコード全体をコピー
COPY . .

# 本番用ビルドを実行（出力先: /app/dist）
RUN npm run build

# ==============================================================
# Stage 2: ランタイムステージ（nginx）
# ビルド成果物（dist/）のみをコピーし、軽量な配信用イメージを作成する
# Node.js は含まれないため、最終イメージサイズが大幅に削減される
# ==============================================================
FROM nginx:alpine

# ビルドステージで生成した dist/ を nginx の公開ディレクトリにコピー
COPY --from=builder /app/dist /usr/share/nginx/html

# カスタム nginx 設定をデフォルト設定と差し替え
# SPA のための history mode 対応と API プロキシ設定が含まれる
COPY nginx.conf /etc/nginx/conf.d/default.conf

# HTTP ポートを公開（docker-compose 側でホストポートにマッピング）
EXPOSE 80
