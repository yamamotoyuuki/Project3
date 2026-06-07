/**
 * PostToolUse フック: .ts ファイル編集後に ESLint を実行する
 *
 * Claude Code が Edit / Write ツールで .ts ファイルを変更した直後に呼び出される。
 * stdin から JSON ペイロードを受け取り、ファイルパスを抽出して ESLint を実行する。
 *
 * 終了コード:
 *   0 - ESLint エラーなし（対象外ファイルのスキップ含む）
 *   1 - ESLint がエラーを検出（Claude に修正を促す）
 */
'use strict';

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

// stdin から JSON ペイロードを収集する
let rawData = '';
process.stdin.on('data', (chunk) => { rawData += chunk; });
process.stdin.on('end', () => {
  // JSON を解析してファイルパスを取得する
  let filePath;
  try {
    const payload = JSON.parse(rawData);
    filePath = payload.tool_input && payload.tool_input.file_path;
  } catch (_) {
    // JSON 解析エラーは無視してスキップする
    return;
  }

  // .ts ファイル以外または node_modules 配下はスキップする
  const normalizedPath = (filePath || '').replace(/\\/g, '/');
  if (
    !filePath ||
    path.extname(filePath) !== '.ts' ||
    normalizedPath.includes('/node_modules/')
  ) {
    return;
  }

  // 対象ファイルが属するプロジェクトルート（package.json が存在するディレクトリ）を探す
  let projectRoot = path.dirname(filePath);
  while (projectRoot !== path.parse(projectRoot).root) {
    if (fs.existsSync(path.join(projectRoot, 'package.json'))) {
      break;
    }
    projectRoot = path.dirname(projectRoot);
  }

  // ローカルの ESLint バイナリを優先して使用する（Windows は .cmd 形式）
  const isWindows = process.platform === 'win32';
  const localEslint = path.join(
    projectRoot, 'node_modules', '.bin',
    isWindows ? 'eslint.cmd' : 'eslint'
  );

  // ローカルに ESLint がインストールされていない場合はスキップして案内を表示する
  if (!fs.existsSync(localEslint)) {
    console.warn('[ESLint] ESLint がローカルにインストールされていません。');
    console.warn('[ESLint] 以下のコマンドを実行してください:');
    console.warn('[ESLint]   npm install -D eslint @typescript-eslint/parser @typescript-eslint/eslint-plugin');
    return;
  }

  console.log(`[ESLint] チェック中: ${path.relative(projectRoot, filePath)}`);

  try {
    // ESLint を実行する（stylish 形式で出力）
    const result = execSync(
      `"${localEslint}" ${JSON.stringify(filePath)} --format=stylish`,
      { cwd: projectRoot, encoding: 'utf8' }
    );
    // 出力があれば表示する（警告など）
    if (result && result.trim()) {
      console.log(result);
    } else {
      console.log('[ESLint] 問題なし');
    }
  } catch (e) {
    // ESLint がエラーを検出した場合（終了コード != 0）、Claude に修正を促す
    const output = [e.stdout, e.stderr].filter(Boolean).join('\n').trim();
    if (output) {
      console.error('[ESLint] 問題を検出しました:\n' + output);
    }
    process.exit(1);
  }
});
