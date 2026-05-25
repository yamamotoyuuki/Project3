<!--
  App.vue
  -----------------------------------------------
  ルートコンポーネント

  Vue Router が管理する各ページビューをここへ描画する。
  将来的にヘッダー・サイドバー・フッター等の共通レイアウトを
  追加する場合はこのファイルを拡張する。
  -----------------------------------------------
-->
<template>
  <!--
    RouterView: 現在の URL に対応するページコンポーネントを動的に描画する
    Vue Router 組み込みコンポーネント。
  -->
  <RouterView />
</template>

<script setup lang="ts">
/**
 * RouterView をインポート。
 * Composition API (script setup) では明示的にインポートして使用する。
 */
import { RouterView } from 'vue-router'
</script>
