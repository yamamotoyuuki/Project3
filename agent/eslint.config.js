/**
 * ESLint フラット設定ファイル（ESLint v9+ 形式）
 * 対象: agent の TypeScript ファイル
 */
'use strict';

const tsParser = require('@typescript-eslint/parser');
const tsPlugin = require('@typescript-eslint/eslint-plugin');

module.exports = [
  {
    // node_modules とビルド成果物は対象外とする
    ignores: ['node_modules/**', 'dist/**', 'src-tauri/**'],
  },
  {
    // TypeScript ファイルにのみルールを適用する
    files: ['**/*.ts'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
      globals: {
        // ブラウザ環境のグローバル変数を許容する
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        Promise: 'readonly',
        URL: 'readonly',
        URLSearchParams: 'readonly',
        Blob: 'readonly',
        File: 'readonly',
        FormData: 'readonly',
        fetch: 'readonly',
        localStorage: 'readonly',
        sessionStorage: 'readonly',
        location: 'readonly',
        history: 'readonly',
        navigator: 'readonly',
        Event: 'readonly',
        MouseEvent: 'readonly',
        KeyboardEvent: 'readonly',
        HTMLElement: 'readonly',
        HTMLInputElement: 'readonly',
        HTMLButtonElement: 'readonly',
        Element: 'readonly',
        Node: 'readonly',
      },
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
    },
    rules: {
      // @typescript-eslint 推奨ルールをベースとして適用する
      ...tsPlugin.configs['recommended'].rules,
      // any 型の使用を禁止する（コーディング規約: any 型を使わない）
      '@typescript-eslint/no-explicit-any': 'error',
      // 未使用変数をエラーとする（_プレフィックスの変数は許容）
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      // console 出力を警告とする（本番コードへの混入を防ぐ）
      'no-console': 'warn',
    },
  },
];
