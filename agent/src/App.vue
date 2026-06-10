<!--
  機能ID:
    AGT_PCI0101 : PC情報表示タブ
    AGT_PCI0102 : PC情報送信タブ
    AGT_CFG0101 : 設定タブ
    AGT_CFG0102 : 新規登録確認モーダル
    AGT_WUP0101 : WindowsUpdate適用判定
  agent/src/App.vue
  -----------------------------------------------
  PC管理エージェント - メインコンポーネント（Tauri デスクトップアプリ）

  3画面（タブ）構成:
    - PC情報 (AGT_PCI0101): 収集した PC のハードウェア・OS 情報を表示
    - 送信   (AGT_PCI0102): 収集情報をバックエンド API へ POST 送信
    - 設定   (AGT_CFG0101): 資産番号・設置場所を設定して永続化

  Tauri コマンド（Rust 側 lib.rs で定義）:
    - load_config(): application.yml から API 設定を読み込む
    - collect_pc_info(): ハードウェア情報の収集
    - send_report(apiUrl, report): バックエンドへのデータ送信

  API URL 管理:
    - application.yml（src-tauri/application.yml）で一元管理
    - 管理者がバンドル済み設定ファイルを編集して配布する
    - localStorage には保存しない
  -----------------------------------------------
-->
<template>
  <!-- アプリ全体ラッパー（縦方向レイアウト） -->
  <div class="agent-app">

    <!-- ヘッダー（アプリ名・バージョン表示） -->
    <header class="agent-header">
      <!-- imageフォルダのアイコン画像を読み込む（規約: アイコンは画像化してimageフォルダに格納） -->
      <img src="./image/icon-pc.svg" class="header-icon" alt="PC管理エージェント" />
      <span>PC管理エージェント</span>
      <span class="version">v1.0.0</span>
    </header>

    <!-- =====================
         タブ: PC情報表示（画面A）
         エージェント番号未取得時は非表示（表示・操作ともに不可）
         ===================== -->
    <div v-if="currentView === 'info' && agentNumber" class="view-panel">
      <h2>PC情報</h2>
      <!-- 収集済みの場合のみ情報グリッドを表示 -->
      <div v-if="pcInfo" class="info-grid">
        <div class="info-row">
          <span class="info-label">ホスト名</span>
          <span>{{ pcInfo.hostname }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">CPU</span>
          <span>{{ pcInfo.hardware.cpu_model }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">コア数</span>
          <span>{{ pcInfo.hardware.cpu_cores }} コア</span>
        </div>
        <div class="info-row">
          <span class="info-label">メモリ</span>
          <span>{{ pcInfo.hardware.memory_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">ディスク（総容量）</span>
          <span>{{ pcInfo.hardware.disk_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">ディスク（空き）</span>
          <span>{{ pcInfo.hardware.disk_free_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">OS</span>
          <span>{{ pcInfo.os.name }} {{ pcInfo.os.version }}</span>
        </div>
        <!-- ネットワーク IP アドレス（複数 NIC 対応） -->
        <!-- :key に IP アドレスを使用する。MAC は現バージョンで "N/A" 固定のため使用不可。 -->
        <!-- IP は NIC ごとに一意であり、追加・削除時も正しく差分更新される。          -->
        <div v-for="(nic, idx) in pcInfo.network" :key="nic.ip" class="info-row">
          <span class="info-label">IPアドレス {{ idx > 0 ? idx + 1 : '' }}</span>
          <span>{{ nic.ip }}</span>
        </div>
        <!-- <div class="info-row">
          <span class="info-label">収集日時</span>
          <span>{{ pcInfo.collected_at }}</span>
        </div> -->
        <!-- インストール済みソフトウェア件数 -->
        <!-- <div class="info-row">
          <span class="info-label">検出ソフトウェア</span>
          <span>{{ pcInfo.software.length }} 件</span>
        </div> -->
      </div>

      <!-- 情報収集ボタン（Tauri コマンドを呼び出す） -->
      <button class="btn-primary" @click="onCollectInfoClick">情報を取得</button>
    </div>

    <!-- =====================
         タブ: 送信（画面B）
         エージェント番号未取得時は非表示（表示・操作ともに不可）
         ===================== -->
    <div v-if="currentView === 'send' && agentNumber" class="view-panel">
      <h2>情報送信</h2>

      <!-- 送信ステータスメッセージ（共通部品・画面中央フロート） -->
      <StatusMessage
        :message="sendStatus.message"
        :messageType="sendStatus.type"
        :isFading="isSendStatusFading"
        :enableFadeOut="true"
      />


      <!-- 送信ボタン（PC情報未収集またはエージェント番号未取得の場合は disabled） -->
      <button class="btn-primary" @click="onSendReportClick" :disabled="!pcInfo || !agentNumber">
        手動送信
      </button>
    </div>

    <!-- =====================
         タブ: 設定（画面C）
         ===================== -->
    <div v-if="currentView === 'settings'" class="view-panel">
      <h2>設定</h2>

      <!-- エージェント番号（読み取り専用・バックエンドが発行） -->
      <div class="form-group">
        <label>エージェント番号</label>
        <input :value="agentNumber || '（未取得）'" disabled class="input-readonly" title="バックエンドに自動登録された端末固有の識別子" />
      </div>

      <!-- バックエンド API URL（application.yml から読み込み・読み取り専用） -->
      <!-- <div class="form-group">
        <label>API URL</label>
        <input :value="apiUrl" disabled class="input-readonly"
          title="application.yml で設定されたバックエンドAPIのURL（管理者のみ変更可）" />
        <span class="hint-text">※ application.yml で管理者が設定します</span>
      </div> -->

      <!-- 設置場所入力（pc_assetsのlocationカラムに登録される）必須 -->
      <div class="form-group">
        <label>設置場所 <span class="required-mark">*必須</span></label>
        <input
          v-model="settings.location"
          placeholder="3F-営業部"
          :class="{ 'input-error': validationErrors.location }"
          @input="validationErrors.location = false"
        />
        <!-- バリデーションエラーメッセージ（保存ボタン押下後に表示） -->
        <span v-if="validationErrors.location" class="error-text">設置場所を入力してください</span>
      </div>

      <!-- 使用者名入力（pc_assetsのassigned_employee_idに紐付けられる）必須 -->
      <div class="form-group">
        <label>使用者名 <span class="required-mark">*必須</span></label>
        <input
          v-model="settings.userName"
          placeholder="山田 太郎"
          :class="{ 'input-error': validationErrors.userName }"
          @input="validationErrors.userName = false"
        />
        <!-- バリデーションエラーメッセージ（保存ボタン押下後に表示） -->
        <span v-if="validationErrors.userName" class="error-text">使用者名を入力してください</span>
      </div>

      <!--
        購入／レンタル区分
        - 起動時にバックエンドから取得できた場合: 読み取り専用（disabled）
        - 取得できなかった場合（資産未登録・バックエンド未起動等）: 編集可能（active）
        仕様: エージェントから設定できるのはレンタルへの変更のみ（購入への変更は管理者操作）
      -->
      <div class="form-group">
        <label>購入／レンタル区分</label>
        <select v-model="settings.acquisitionType" :disabled="acquisitionTypeFromBackend !== null" class="select"
          :class="{ 'input-readonly': acquisitionTypeFromBackend !== null }">
          <option value="">選択してください</option>
          <option value="PURCHASE">購入</option>
          <option value="RENTAL">レンタル</option>
        </select>
        <!-- バックエンドから取得済みの場合は変更不可である旨を表示 -->
        <span v-if="acquisitionTypeFromBackend !== null" class="hint-text">
          ※ バックエンドに設定済みのため変更できません
        </span>
        <!-- 未設定でレンタルが選択可能な場合の説明 -->
        <span v-else class="hint-text hint-text--active">
          ※ 保存時にバックエンドへ反映されます
        </span>
      </div>

      <!--
        保存 / 新規登録ボタン
        - エージェント番号取得済み: 「保存」→ そのまま saveSettings を実行（画面遷移なし）
        - エージェント番号未取得 : 「新規登録」→ 確認モーダルを表示してから送信
      -->
      <button v-if="agentNumber" class="btn-primary" @click="onSaveSettingsClick">保存</button>
      <button v-else class="btn-primary btn-register" @click="onNewRegisterClick">新規登録</button>

      <!-- 送信結果メッセージ（保存ボタン押下後に表示。共通部品・画面中央フロート） -->
      <StatusMessage
        :message="sendStatus.message"
        :messageType="sendStatus.type"
        :isFading="isSendStatusFading"
        :enableFadeOut="true"
      />

      <!-- ======================================================
           WindowsUpdate 適用判定
           ====================================================== -->
      <div class="form-group win-update-section">
        <!-- ボタン＋件数メッセージを横並びで表示 -->
        <div class="win-update-row">
          <!-- エージェント番号未取得時または処理中は非活性 -->
          <button class="btn-primary btn-win-update" :disabled="isWinUpdateLoading || !agentNumber" @click="runWindowsUpdateJudgment">
            WindowsUpdate 適用判定
          </button>
        </div>

      </div>
    </div>

    <!-- =====================
         PC情報取得 完了トースト
         PC情報タブでのみ表示。画面中央（メモリとディスク総容量の高さあたり）に重ねて表示する。
         ===================== -->
    <!-- PC情報取得完了トースト（共通部品） -->
    <StatusMessage
      v-if="currentView === 'info'"
      :message="collectStatus.message"
      :messageType="collectStatus.type"
      :isFading="isCollectStatusFading"
      :enableFadeOut="true"
    />

    <!-- =====================
         WindowsUpdate 未適用一覧 ポップアップ
         未適用の更新がある場合のみ表示。画面中央に重ねて表示し、×またはオーバーレイクリックで閉じる。
         ===================== -->
    <div v-if="isWinUpdatePopupVisible" class="modal-overlay" @click.self="isWinUpdatePopupVisible = false">
      <div class="modal-content win-update-popup">
        <!-- タイトル行 -->
        <div class="win-update-popup-header">
          <span class="win-update-popup-title">
            <!-- imageフォルダの警告アイコン画像を読み込む -->
            <img src="./image/icon-warning.svg" class="popup-title-icon" alt="warning" />
            未適用の Windows Update
          </span>
          <button class="win-update-popup-close" @click="isWinUpdatePopupVisible = false">×</button>
        </div>
        <!-- 件数メッセージ -->
        <p class="win-update-popup-count">{{ winUpdateMessage }}</p>
        <!-- KB ID 一覧 -->
        <ul class="win-update-popup-list">
          <li v-for="kb in winUpdateKbIds" :key="kb" class="win-update-popup-item">{{ kb }}</li>
        </ul>
      </div>
    </div>

    <!-- =====================
         登録結果 ポップアップ（画面中央表示・共通部品）
         成功時: フェードアウト可否=true → 3秒後に自動フェードアウト
         失敗時: フェードアウト可否=false → 「閉じる」ボタンを押すまで表示
         ===================== -->
    <StatusMessage
      variant="popup"
      :message="registerPopup.message"
      :messageType="registerPopup.type"
      :isFading="isRegisterPopupFading"
      :enableFadeOut="registerPopup.type === 'success'"
      @close="closeRegisterPopup"
    />

    <!-- =====================
         新規登録 確認モーダル
         ===================== -->
    <div v-if="showNewRegisterWarning" class="modal-overlay" @click.self="closeRegisterModal">
      <div class="modal-content">
        <!-- 警告メッセージ -->
        <div class="modal-warning">
          <!-- imageフォルダに格納した警告アイコン画像を読み込む（規約: アイコンは画像化してimageフォルダに格納） -->
          <img src="./image/icon-warning.svg" class="modal-warning-icon" alt="warning" />
          <p>PC情報をサーバーに送信します。個人所有のPCでないことを確認してください。</p>
        </div>

        <!-- 個人所有でないことの確認チェックボックス -->
        <label class="checkbox-label">
          <input type="checkbox" v-model="confirmedNotPersonal" class="checkbox-input" />
          <span>個人所有のPCではありません</span>
        </label>

        <!-- 登録トークン入力（管理者から発行されたトークンを画面から入力する） -->
        <div class="modal-token-group">
          <label class="modal-token-label">登録トークン</label>
          <input
            v-model="enrollmentTokenInput"
            type="text"
            placeholder="管理者から発行されたトークンを入力"
            class="modal-token-input"
          />
          <span class="hint-text">管理者から発行された登録トークンを入力してください</span>
        </div>

        <!-- チェック済み かつ トークン入力済みの場合のみ「登録」ボタンを表示 -->
        <button v-if="confirmedNotPersonal && enrollmentTokenInput.trim()" class="btn-primary" @click="onRegisterConfirmClick">登録</button>

        <!-- キャンセルボタン（常に表示） -->
        <button class="btn-cancel" @click="closeRegisterModal">キャンセル</button>
      </div>
    </div>

    <!-- =====================
         グローバルローディングオーバーレイ
         ボタン押下からAPI処理完了までの間、画面全体を薄暗くして処理中を示す
         ===================== -->
    <div v-if="isGlobalLoading" class="global-loading-overlay">
      <div class="global-loading-content">
        <div class="global-loading-spinner"></div>
        <span class="global-loading-text">処理中...</span>
      </div>
    </div>

    <!-- =====================
         ボトムナビゲーション
         ===================== -->
    <nav class="bottom-nav">
      <!-- アクティブなタブは active クラスでハイライト -->
      <!-- 各タブのアイコンはimageフォルダの画像ファイルを使用（規約準拠） -->
      <!-- PC情報・送信タブはエージェント番号取得済みの場合のみ表示する -->
      <button v-if="agentNumber" :class="{ active: currentView === 'info' }" @click="navigateToInfo">
        <img src="./image/icon-pc.svg" class="nav-tab-icon" alt="PC情報" />
        PC情報
      </button>
      <button v-if="agentNumber" :class="{ active: currentView === 'send' }" @click="navigateToSend">
        <img src="./image/icon-send.svg" class="nav-tab-icon" alt="送信" />
        送信
      </button>
      <button :class="{ active: currentView === 'settings' }" @click="navigateToSettings">
        <img src="./image/icon-settings.svg" class="nav-tab-icon" alt="設定" />
        設定
      </button>
    </nav>

  </div>
</template>

<script setup lang="ts">
  /**
   * エージェントアプリのロジック
   *
   * - Tauri の invoke() で Rust コマンドを呼び出す
   * - API URL は application.yml（src-tauri/application.yml）から読み込む
   * - その他の設定値（資産番号・設置場所・使用者名）は localStorage で永続化する
   * - 起動時に自動でPC情報を収集し、エージェント番号・取得区分を取得する
   *
   * 起動時シーケンス:
   *   1. load_config() で application.yml から API URL を読み込む
   *   2. collect_pc_info() でハードウェア情報を収集
   *   3. ローカルファイルからエージェント番号を読み込む（load_agent_number コマンド）
   *      - 存在しない場合: 設定タブの「新規登録」ボタンから手動登録
   *      - 存在する場合:  そのまま使用
   *   4. fetch_asset_acquisition_type() でバックエンドから取得区分を取得
   *      - 取得できた場合: acquisitionTypeFromBackend に設定し、選択欄を読み取り専用にする
   *      - 取得できなかった場合: 選択欄を活性化してユーザーが入力可能にする
   */
  import { ref, onMounted } from 'vue'
  import { invoke } from '@tauri-apps/api/core'
  import StatusMessage from './components/StatusMessage.vue'

  /** タブ識別型 */
  type View = 'info' | 'send' | 'settings'

  /**
   * Tauri コマンド collect_pc_info() の戻り値型（Rust AgentReport に対応）
   */
  interface PcInfo {
    hostname: string
    hardware: {
      cpu_model: string
      cpu_cores: number
      memory_gb: number
      disk_gb: number
      disk_free_gb: number
    }
    os: { name: string; version: string }
    network: { ip: string; mac: string }[]
    software: { name: string; version: string }[]
    collected_at: string
  }

  /**
   * load_config() コマンドの戻り値型（Rust ApiSection に対応）
   */
  interface ApiConfig {
    /** バックエンドAPIのベースURL（例: "http://localhost:8080/api/v1"） */
    base_url: string
    /**
     * エージェント初回登録トークン（application.yml の agent.api.enrollment-token）
     * 管理者が発行したトークンを設定する。初回登録後は不要（APIキーに切り替わる）。
     */
    enrollment_token?: string
  }

  /**
   * fetch_asset_acquisition_type コマンドの戻り値型（Rust AssetInfo に対応）
   * - acquisitionType: 取得区分（"PURCHASE" / "RENTAL" / null）
   * - isReturned     : 返却済みフラグ（RENTAL かつ返却済みの場合に true）
   */
  interface AssetInfo {
    /** 取得区分（"PURCHASE" / "RENTAL" / null = 未設定） */
    acquisitionType: string | null
    /** 返却済みフラグ（Rust の rename_all="camelCase" により isReturned で受け取る） */
    isReturned: boolean
  }

  /**
   * 現在表示中のタブ
   * 初期値は 'settings'（エージェント番号未取得時は設定タブのみ使用可能）。
   * 起動時にエージェント番号が確認できた場合は onMounted 内で 'info' に切り替える。
   */
  const currentView = ref<View>('settings')

  /** 収集済み PC 情報（null = 未収集） */
  const pcInfo = ref < PcInfo | null > (null)

  /**
   * application.yml から読み込んだ API のベース URL
   * - 初期値は空文字（起動時に load_config() で設定される）
   * - 読み込みに失敗した場合は .env の VITE_API_BASE_URL フォールバック値が設定される
   * - localStorage は使用しない（管理者が application.yml で一元管理する）
   */
  const apiUrl = ref('')

  /**
   * エージェント番号（初回登録時にバックエンドが発行。アプリのローカルデータディレクトリに永続化）
   * - Windows: %LOCALAPPDATA%\{app-name}\.agent_id（隠しファイル属性付き）
   * - macOS:   ~/Library/Application Support/{app-name}/.agent_id
   * - Linux:   ~/.local/share/{app-name}/.agent_id
   * 例: "AGT-A1B2C3D4" / null = 未取得（初回起動またはバックエンド未起動時）
   */
  const agentNumber = ref < string | null > (null) // 起動時に load_agent_number で読み込む

  /**
   * APIキー（初回登録時にバックエンドが発行。アプリのローカルデータディレクトリに永続化）
   * - Windows: %LOCALAPPDATA%\{app-name}\.agent_key（隠しファイル属性付き）
   * - report / asset-info API呼び出し時に Authorization: Bearer {apiKey} として付与する
   * - null = 未取得（初回起動時。登録後に設定される）
   */
  const apiKey = ref < string | null > (null) // 起動時に load_api_key で読み込む

  /**
   * PC情報取得ステータスメッセージ（type: 'success' | 'error' | 'warning' | ''）
   * PC情報タブの「情報を取得」ボタン押下後にメモリとディスク総容量の間に表示する
   */
  const collectStatus = ref<{ type: 'success' | 'error' | 'warning' | ''; message: string }>({ type: '', message: '' })

  /** PC情報取得メッセージのフェードアウト中フラグ */
  const isCollectStatusFading = ref(false)

  /** PC情報取得メッセージのフェードアウトタイマー参照 */
  let collectStatusTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * PC情報取得ステータスメッセージを 3 秒後にフェードアウトして消去するタイマーを開始する
   * 連続して「情報を取得」を押した場合は既存タイマーをキャンセルしてリセットする
   */
  function startCollectStatusFadeTimer() {
    if (collectStatusTimer !== null) {
      clearTimeout(collectStatusTimer)
      collectStatusTimer = null
    }
    isCollectStatusFading.value = false
    // 2.5 秒後にフェードアウト開始（CSS transition 0.5 秒と合わせて合計 3 秒で消える）
    collectStatusTimer = setTimeout(() => {
      isCollectStatusFading.value = true
      collectStatusTimer = setTimeout(() => {
        collectStatus.value = { type: '', message: '' }
        isCollectStatusFading.value = false
        collectStatusTimer = null
      }, 500)
    }, 2500)
  }

  /** 送信ステータスメッセージ（type: 'success' | 'error' | ''） */
  const sendStatus = ref<{ type: 'success' | 'error' | ''; message: string }>({ type: '', message: '送信ボタンを押してください' })

  /**
   * フェードアウト中フラグ
   * true になると CSS の opacity: 0 トランジションが発動する
   */
  const isSendStatusFading = ref(false)

  /**
   * フェードアウトタイマーの参照
   * 連続して保存ボタンを押した場合に前のタイマーをキャンセルするために保持する
   */
  let sendStatusTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * 送信ステータスメッセージを 3 秒後にフェードアウトして消去するタイマーを開始する
   *
   * - 既存タイマーがある場合はキャンセルして新たに開始する（連続押下対応）
   * - 2.5 秒後に fading フラグを true にして CSS フェードアウト（0.5 秒）を開始する
   * - フェードアウト完了後にメッセージをリセットする
   * - 成功・エラー確定後のみ呼ぶ
   */
  function startSendStatusFadeTimer() {
    // 既存タイマーをキャンセルして二重起動を防ぐ
    if (sendStatusTimer !== null) {
      clearTimeout(sendStatusTimer)
      sendStatusTimer = null
    }
    isSendStatusFading.value = false
    // 2.5 秒後にフェードアウト開始（CSS transition 0.5 秒と合わせて合計 3 秒で消える）
    sendStatusTimer = setTimeout(() => {
      isSendStatusFading.value = true
      // CSS フェードアウト完了（0.5 秒）後にメッセージをリセットする
      sendStatusTimer = setTimeout(() => {
        sendStatus.value = { type: '', message: '送信ボタンを押してください' }
        isSendStatusFading.value = false
        sendStatusTimer = null
      }, 500)
    }, 2500)
  }

  /**
   * 新規登録確認モーダルの表示フラグ
   * true: モーダルを表示（エージェント番号未取得時に「新規登録」ボタン押下で true になる）
   */
  const showNewRegisterWarning = ref(false)

  // -------------------------------------------------------
  // WindowsUpdate 適用判定
  // -------------------------------------------------------

  /** 判定処理中フラグ（true の間はボタンを disabled にする） */
  const isWinUpdateLoading = ref(false)

  /** モーダルに表示するステータスメッセージ（結果件数 / エラー） */
  const winUpdateMessage = ref('')

  /**
   * 未適用更新の KB ID 一覧
   * 未適用あり: ['KB5034441', ...] / 最新 or エラー: []
   */
  const winUpdateKbIds = ref < string[] > ([])

  /**
   * 未適用更新ポップアップの表示フラグ
   * 未適用更新が 1 件以上ある場合、またはエラー発生時に true になる
   */
  const isWinUpdatePopupVisible = ref(false)

  /** ボタン押下中の全画面ローディングオーバーレイ表示フラグ（true: オーバーレイ表示） */
  const isGlobalLoading = ref(false)

  /**
   * WindowsUpdate 適用判定を実行する
   *
   * Windows Update Agent COM API（Microsoft.Update.Session）で
   * Microsoft が公開済みの未適用更新プログラムを検索し、
   * windowsUpdateProgram.txt に保存する。
   * ネットワーク通信を伴うため最大 120 秒かかる場合がある。
   */
  async function runWindowsUpdateJudgment() {
    // ボタン押下時: メッセージ・KB一覧・ポップアップをリセットする
    // グローバルローディングオーバーレイで処理中を表示するため、ここでは何も表示しない
    isWinUpdateLoading.value = true
    isGlobalLoading.value = true
    winUpdateMessage.value = ''
    winUpdateKbIds.value = []
    isWinUpdatePopupVisible.value = false
    try {
      const result = await invoke < string > ('collect_windows_update')

      // PowerShell の出力から件数を抽出して件数に応じたメッセージを設定する
      const countMatch = result.match(/Available update\(s\) from Microsoft:\s*(\d+)/)
      if (countMatch) {
        const count = parseInt(countMatch[1], 10)
        if (count === 0) {
          // 未適用なし: ポップアップは表示しない（最新状態のため通知不要）
          winUpdateKbIds.value = []
        } else {
          // テーブルの KB 列（行頭に並ぶ 4〜8 桁の数値）を抽出して "KBXXXXXXX" 形式に変換する
          // 出力例: "5034441   Critical  2024-01-15  ..." → "KB5034441"
          winUpdateKbIds.value = [...result.matchAll(/^\s*(\d{4,8})\b/gm)]
            .map(m => `KB${m[1]}`)
          // 未適用あり: 件数メッセージをセットして画面中央のポップアップを表示する
          winUpdateMessage.value = `${count} 件の未適用更新があります`
          isWinUpdatePopupVisible.value = true
        }
      }
      // countMatch が null の場合（出力フォーマット変化等）はポップアップを表示しない
    } catch (e: unknown) {
      // エラー発生時: エラーメッセージをセットしてポップアップを表示する（フェードアウトしない）
      const errorMessage = e instanceof Error ? e.message : String(e)
      winUpdateMessage.value = `エラー: ${errorMessage}`
      winUpdateKbIds.value = []
      isWinUpdatePopupVisible.value = true
    } finally {
      isWinUpdateLoading.value = false
      isGlobalLoading.value = false
    }
  }

  /**
   * 「個人所有のPCではありませんか？」チェックボックスの状態
   * true: チェックされている → 「登録」ボタンを表示する
   */
  const confirmedNotPersonal = ref(false)

  /**
   * 登録トークン入力値（モーダル内の入力欄に利用者が直接入力する）
   * application.yml から読み込む方式を廃止し、画面入力方式に変更。
   * モーダルを開くたびに空文字にリセットする。
   */
  const enrollmentTokenInput = ref('')

  /**
   * 登録結果ポップアップの表示状態
   * type: 'success' = 登録成功（緑）、'error' = 登録失敗（赤）、'' = 非表示
   * 成功時は 3 秒後に自動フェードアウト、失敗時は手動で閉じるまで表示する。
   */
  const registerPopup = ref<{ type: 'success' | 'error' | ''; message: string }>({
    type: '',
    message: '',
  })

  /** 登録結果ポップアップのフェードアウト中フラグ */
  const isRegisterPopupFading = ref(false)

  /** 登録結果ポップアップのフェードアウトタイマー参照（連続呼び出し時のキャンセルに使用） */
  let registerPopupTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * 登録結果ポップアップを表示する
   * - 成功時: 2.5 秒後にフェードアウト開始（CSS 0.5 秒で合計 3 秒）
   * - 失敗時: 「閉じる」ボタンまたはオーバーレイクリックまで表示し続ける
   *
   * @param type    - 'success' または 'error'
   * @param message - 表示するメッセージ文字列
   */
  function showRegisterPopup(type: 'success' | 'error', message: string) {
    // 既存タイマーをキャンセルして状態をリセットする
    if (registerPopupTimer !== null) {
      clearTimeout(registerPopupTimer)
      registerPopupTimer = null
    }
    isRegisterPopupFading.value = false
    registerPopup.value = { type, message }

    // 成功時のみ自動フェードアウトタイマーを開始する
    if (type === 'success') {
      registerPopupTimer = setTimeout(() => {
        isRegisterPopupFading.value = true
        registerPopupTimer = setTimeout(() => {
          registerPopup.value = { type: '', message: '' }
          isRegisterPopupFading.value = false
          registerPopupTimer = null
        }, 500)
      }, 2500)
    }
  }

  /**
   * 登録結果ポップアップを閉じる（「閉じる」ボタンまたはオーバーレイクリック時）
   * タイマーもキャンセルして完全にリセットする。
   */
  function closeRegisterPopup() {
    if (registerPopupTimer !== null) {
      clearTimeout(registerPopupTimer)
      registerPopupTimer = null
    }
    registerPopup.value = { type: '', message: '' }
    isRegisterPopupFading.value = false
  }

  /**
   * バックエンドから取得した取得区分
   * - 文字列（"PURCHASE" / "RENTAL"）: バックエンドに設定済み → 選択欄を読み取り専用にする
   * - null: 取得できなかった（資産未登録 / バックエンド未起動）→ 選択欄を活性化する
   */
  const acquisitionTypeFromBackend = ref < string | null > (null)

  /**
   * レンタル返却済みフラグ
   * - true : 取得区分が RENTAL かつ返却済み（pc_acquisition_rental.return_date が設定済み）
   * - false: 購入品 / 未返却 / 資産未登録
   * 「情報を取得」ボタン押下後に fetchAcquisitionType() で更新される。
   */
  const isRentalReturned = ref(false)

  /**
   * 設定値（localStorage に永続化）
   * assetNumber:     資産番号（例: "PC-00123"）
   * location:        設置場所（pc_assets.location に登録）必須
   * userName:        使用者名（社員名と照合して pc_assets.assigned_employee_id に登録）必須
   * acquisitionType: 購入/レンタル区分（バックエンドから取得できなかった場合のユーザー選択値）
   *
   * ※ API URL は application.yml で管理するため settings には含めない
   */
  const settings = ref({
    assetNumber: localStorage.getItem('assetNumber') || '',
    location: localStorage.getItem('location') || '',
    userName: localStorage.getItem('userName') || '',
    acquisitionType: localStorage.getItem('acquisitionType') || '',
  })

  /**
   * フィールドごとのバリデーションエラーフラグ
   * - 保存ボタン押下時に必須チェックが失敗した場合に true になる
   * - ユーザーが入力を開始した時点で false にリセットする（input イベント）
   */
  const validationErrors = ref({
    location: false, // 設置場所が未入力の場合に true
    userName: false, // 使用者名が未入力の場合に true
  })

  /**
   * application.yml から API 設定（ベース URL）を読み込む
   *
   * Rust の load_config コマンドを invoke() で呼び出す。
   * 成功時は apiUrl に設定されたURLをセット、失敗時はデフォルト値を使用する。
   * application.yml が見つからない場合も Rust 側でデフォルト値を返す。
   */
  /**
   * フォールバック API URL
   * application.yml の読み込みに失敗した場合に使用する。
   * .env の VITE_API_BASE_URL で設定する（env変数ハードコード禁止規約に従い定数化）。
   */
  // ?? を先に評価してから string にアサーションする（as string ?? '' だと ?? が dead code になるため）
  const fallbackApiUrl = (import.meta.env.VITE_API_BASE_URL ?? '') as string

  async function loadApiConfig() {
    try {
      const config = await invoke < ApiConfig > ('load_config')
      // base_url が undefined になる場合（Rust の serde rename ずれ等）に備えて .env のフォールバックを使用する
      apiUrl.value = config.base_url || fallbackApiUrl
      console.info('application.yml からAPIのURLを読み込みました:', apiUrl.value)
    } catch (e) {
      // 読み込みに失敗した場合は .env の VITE_API_BASE_URL を使用する
      apiUrl.value = fallbackApiUrl
      console.warn('application.yml の読み込みに失敗しました。フォールバック URL を使用します:', e)
    }
  }

  /**
   * PC 情報を収集する
   * Rust の collect_pc_info コマンドを invoke() で呼び出す。
   * 成功時は pcInfo に結果をセット、失敗時はコンソールにエラーを出力する。
   *
   * 収集完了後にバックエンドへ取得区分（購入/レンタル）を問い合わせる。
   * これにより「情報を取得」ボタン1回で設定タブの購入/レンタル区分も最新化される。
   */
  async function collectInfo() {
    // 連続押下時は前のフェードタイマーをキャンセルしてメッセージを即時リセットする
    if (collectStatusTimer !== null) {
      clearTimeout(collectStatusTimer)
      collectStatusTimer = null
    }
    isCollectStatusFading.value = false
    try {
      pcInfo.value = await invoke < PcInfo > ('collect_pc_info')
      // 取得成功メッセージを設定して 3 秒後フェードアウトタイマーを開始する
      // アイコンは imageフォルダの icon-success.svg を template 側で表示（規約準拠）
      collectStatus.value = { type: 'success', message: '取得しました' }
      startCollectStatusFadeTimer()
    } catch (e) {
      console.error('情報収集エラー:', e)
      // アイコンは imageフォルダの icon-error.svg を template 側で表示（規約準拠）
      collectStatus.value = { type: 'error', message: '取得に失敗しました' }
      startCollectStatusFadeTimer()
    }
    // PC情報収集後にバックエンドから取得区分・返却状況を取得する（ホスト名が確定してから実行）
    await fetchAcquisitionType()

    // 返却済みの場合は「取得しました」を返却済み警告メッセージに差し替える
    // 既存のフェードタイマーをキャンセルして warning メッセージで再スタートする
    if (isRentalReturned.value) {
      if (collectStatusTimer !== null) {
        clearTimeout(collectStatusTimer)
        collectStatusTimer = null
      }
      isCollectStatusFading.value = false
      // アイコンは imageフォルダの icon-warning.svg を template 側で表示（規約準拠）
      collectStatus.value = { type: 'warning', message: 'このPCは使用を停止してます。\n管理者に連絡してください。' }
      startCollectStatusFadeTimer()
    }
  }

  /**
   * エージェントをバックエンドに初回登録してエージェント番号とAPIキーを取得し、ファイルに保存する
   *
   * ファイルにエージェント番号が存在しない場合のみ呼び出す。
   * 引数で受け取った登録トークン（onRegisterConfirm がモーダルを閉じる前に退避した値）を
   * バックエンドに送信し、返却されたエージェント番号とAPIキーをローカルファイルに永続化する。
   *
   * ※ closeRegisterModal() が enrollmentTokenInput をリセットするため、
   *   ref を直接読まずに引数で受け取る方式にしている。
   * ※ onRegisterConfirm() 側で事前に pcInfo を収集してからこの関数を呼び出すこと。
   *
   * @param enrollmentToken - 利用者がモーダル入力欄に入力した登録トークン（呼び出し元で退避済み）
   * @throws PC情報未収集・トークン未入力・不正・期限切れ・バックエンド未起動の場合にエラーをスローする
   */
  async function registerAgent(enrollmentToken: string) {
    // PC情報が収集できていない場合はエラーをスローする
    // （サイレントリターンすると呼び出し元で成功と誤判定されるため、必ず例外で通知する）
    if (!pcInfo.value) {
      throw new Error('PC情報の収集に失敗しました。再度お試しください。')
    }

    if (!enrollmentToken) {
      throw new Error('登録トークンを正しく入力してください。')
    }

    // バックエンドに登録トークンとホスト名を送信してエージェント番号とAPIキーを発行してもらう
    // Rust の AgentRegisterResponse は #[serde(rename = "agentNumber")] / #[serde(rename = "apiKey")] で
    // キャメルケースにリネームして JSON 出力するため、TypeScript 型もキャメルケースで定義する。
    const result = await invoke<{ agentNumber: string; apiKey: string }>('register_agent', {
      apiUrl: apiUrl.value,
      hostname: pcInfo.value.hostname,
      enrollmentToken,
    })

    // 発行されたエージェント番号をローカルファイルに永続保存する
    await invoke('save_agent_number', { agentNumber: result.agentNumber })
    // 発行されたAPIキーをローカルファイルに永続保存する（隠しファイル属性付き）
    await invoke('save_api_key', { apiKey: result.apiKey })

    agentNumber.value = result.agentNumber
    apiKey.value      = result.apiKey
    console.info('エージェント番号・APIキーを取得・保存しました:', result.agentNumber)
  }

  /**
   * バックエンドからPC資産の取得区分（購入/レンタル）を取得する
   *
   * 起動時に呼び出し、取得できた場合は acquisitionTypeFromBackend に設定して
   * 設定欄を読み取り専用にする。取得できない場合（資産未登録・バックエンド未起動等）は
   * null のまま維持して設定欄を活性化する。
   *
   * 検索順序（バックエンド側）:
   *   1. エージェント番号で検索（pc_assets.agent_number が設定済みの場合）
   *   2. ホスト名でフォールバック検索（管理者が先に資産を作成した場合など）
   *
   * エージェント番号・ホスト名のどちらも未取得の場合はスキップする。
   */
  async function fetchAcquisitionType() {
    // エージェント番号もホスト名も未取得の場合はスキップする
    if (!agentNumber.value && !pcInfo.value?.hostname) return
    try {
      // Rust コマンドが AssetInfo | null を返す（取得区分 + 返却済みフラグ）
      const assetInfo = await invoke < AssetInfo | null > ('fetch_asset_acquisition_type', {
        apiUrl: apiUrl.value,
        apiKey: apiKey.value ?? '',             // APIキー（Authorization: Bearer ヘッダーに付与）
        agentNumber: agentNumber.value ?? '',   // 未取得時は空文字（バックエンド側でスキップ）
        hostname: pcInfo.value?.hostname ?? '', // フォールバック検索用ホスト名
      })

      if (assetInfo?.acquisitionType) {
        // バックエンドに取得区分が設定済み → 読み取り専用にして表示する
        acquisitionTypeFromBackend.value = assetInfo.acquisitionType
        settings.value.acquisitionType  = assetInfo.acquisitionType
        console.info('取得区分をバックエンドから取得しました:', assetInfo.acquisitionType)
      } else {
        // 未設定 → 選択欄を活性化（ユーザーが入力可能）
        acquisitionTypeFromBackend.value = null
        console.info('取得区分はバックエンドに未設定です。選択欄を活性化します。')
      }

      // 返却済みフラグを更新する（RENTAL かつ返却済みの場合に true）
      // PC情報タブに警告メッセージを表示するために使用する
      isRentalReturned.value = assetInfo?.isReturned ?? false
      if (isRentalReturned.value) {
        console.warn('このPCはレンタル返却済みです。')
      }
    } catch (e) {
      // エラー時は選択欄を活性化し、返却済みフラグをリセットする（バックエンド未起動等）
      acquisitionTypeFromBackend.value = null
      isRentalReturned.value = false
      console.warn('資産情報の取得に失敗しました:', e)
    }
  }

  /**
   * 収集した PC 情報をバックエンドへ送信する
   *
   * - 設定画面の assetNumber・location・userName をレポートにマージして送信する
   * - agentNumber が取得済みであれば agent_number に含める
   * - 取得区分（acquisition_type）の送信ルール:
   *     ① バックエンドから取得済みの場合（acquisitionTypeFromBackend !== null）: 送信しない・再取得しない
   *     ② エージェントが購入／レンタルいずれかを選択した場合: 送信し、送信後にバックエンドから
   *        再取得して項目に反映する（仕様: 「区分の取得および項目への値設定」）
   *     ③ 未選択（空文字）の場合: 送信しない
   * - V4 変更: fetchAcquisitionType() はエージェント番号ベースになっているため、
   *            エージェント番号が取得済みの場合のみ再取得が動作する
   */
  async function sendReport() {
    if (!pcInfo.value) return
    try {
      // 送信開始時は既存のフェードタイマーをキャンセルする（グローバルローディングで処理中を表示するため loading メッセージは不要）
      if (sendStatusTimer !== null) {
        clearTimeout(sendStatusTimer)
        sendStatusTimer = null
      }
      isSendStatusFading.value = false

      // 取得区分の送信判定:
      // - バックエンドから取得済み（選択欄がdisabled）→ null（送信しない・再取得しない）
      // - エージェントが購入/レンタルいずれかを選択した場合 → その値を送信
      // - 未選択（空文字）                               → null（送信しない）
      const acquisitionTypeToSend =
        acquisitionTypeFromBackend.value === null && settings.value.acquisitionType !== ''
          ? settings.value.acquisitionType  // 'PURCHASE' または 'RENTAL'
          : null

      // 収集情報にユーザー入力値・エージェント番号を追加してレポートを構築
      const report = {
        ...pcInfo.value,
        agent_number: agentNumber.value || null,       // エージェント番号（未取得時はnull）
        asset_number: settings.value.assetNumber,
        location: settings.value.location,
        user_name: settings.value.userName || null,    // 未入力はnull
        acquisition_type: acquisitionTypeToSend,       // 選択値を送信、未選択・設定済みはnull
      }

      // Rust コマンドでバックエンド API へ POST 送信（APIキーを Authorization: Bearer として送信）
      await invoke('send_report', { apiUrl: apiUrl.value, apiKey: apiKey.value ?? '', report })

      // 送信成功後の処理:
      // 取得区分を送信した場合のみ、バックエンドから再取得して項目に反映する
      // 仕様: 「区分の取得および項目への値設定を行う」
      //       「すでに設定されている場合は区分の取得を行わない」
      //       → 送信していない場合（null）は再取得しない
      if (acquisitionTypeToSend !== null) {
        await fetchAcquisitionType()
        console.info('取得区分を送信後にバックエンドから再取得しました')
      }

      // アイコンは imageフォルダの icon-success.svg を template 側で表示（規約準拠）
      sendStatus.value = { type: 'success', message: '送信成功しました' }
      startSendStatusFadeTimer() // 3 秒後にフェードアウトして消去する
    } catch (e: unknown) {
      // unknown 型で受けて型ガードで安全にメッセージを取り出す（any型禁止規約に従う）
      // アイコンは imageフォルダの icon-error.svg を template 側で表示（規約準拠）
      const errorMessage = e instanceof Error ? e.message : String(e)
      sendStatus.value = { type: 'error', message: errorMessage }
      startSendStatusFadeTimer() // 3 秒後にフェードアウトして消去する
    }
  }

  // =====================================================
  // タブナビゲーション関数
  // 規約: 画面遷移は必ず関数に切り出す（@click に直接代入しない）
  // =====================================================

  /**
   * PC情報タブへ遷移する（エージェント番号取得済み時のみ BottomNav に表示される）
   */
  function navigateToInfo() {
    currentView.value = 'info'
  }

  /**
   * 送信タブへ遷移する（エージェント番号取得済み時のみ BottomNav に表示される）
   */
  function navigateToSend() {
    currentView.value = 'send'
  }

  /**
   * 設定タブへ遷移する（常に表示される）
   */
  function navigateToSettings() {
    currentView.value = 'settings'
  }

  /**
   * 「情報を取得」ボタン押下ハンドラ
   * 処理中は全画面ローディングオーバーレイを表示する
   */
  async function onCollectInfoClick() {
    isGlobalLoading.value = true
    try {
      await collectInfo()
    } finally {
      isGlobalLoading.value = false
    }
  }

  /**
   * 「手動送信」ボタン押下ハンドラ
   * 処理中は全画面ローディングオーバーレイを表示する
   */
  async function onSendReportClick() {
    isGlobalLoading.value = true
    try {
      await sendReport()
    } finally {
      isGlobalLoading.value = false
    }
  }

  /**
   * 「保存」ボタン押下ハンドラ
   * 処理中は全画面ローディングオーバーレイを表示する
   */
  async function onSaveSettingsClick() {
    isGlobalLoading.value = true
    try {
      await saveSettings()
    } finally {
      isGlobalLoading.value = false
    }
  }

  /**
   * 確認モーダルの「登録」ボタン押下ハンドラ
   * 処理中は全画面ローディングオーバーレイを表示する
   */
  async function onRegisterConfirmClick() {
    isGlobalLoading.value = true
    try {
      await onRegisterConfirm()
    } finally {
      isGlobalLoading.value = false
    }
  }

  /**
   * 設定値（資産番号・設置場所・使用者名・購入区分）を localStorage とリアクティブ変数から消去する。
   *
   * 呼び出しタイミング:
   *   - 起動時にエージェント番号が未取得だった場合
   *   - 読み込みエラーが発生した場合
   *
   * localStorage と settings ref を両方クリアすることで、
   * 前回登録時の残存値が設定フォームに表示されるのを防ぐ。
   */
  function clearSettings() {
    // localStorage から設定値を削除する
    localStorage.removeItem('assetNumber')
    localStorage.removeItem('location')
    localStorage.removeItem('userName')
    localStorage.removeItem('acquisitionType')

    // リアクティブな settings ref も空文字にリセットする（画面の入力欄に反映される）
    settings.value.assetNumber    = ''
    settings.value.location       = ''
    settings.value.userName       = ''
    settings.value.acquisitionType = ''

    console.info('設定値をクリアしました（エージェント番号未取得）')
  }

  /**
   * 「新規登録」ボタン押下時のハンドラ
   *
   * エージェント番号が未取得の状態で呼び出される。
   * 設置場所・使用者名の必須チェックを行い、未入力の場合はモーダルを開かずにエラーを表示する。
   * 必須チェック通過後に確認モーダルを表示して
   * チェックボックス＋トークン入力＋登録ボタンの確認フローを開始する。
   */
  function onNewRegisterClick() {
    // 必須項目チェック: 設置場所・使用者名が未入力の場合はモーダルを開かずにエラーを表示する
    const locationEmpty = !settings.value.location.trim()
    const userNameEmpty = !settings.value.userName.trim()

    if (locationEmpty || userNameEmpty) {
      // エラーフラグをセットしてフィールドを赤枠表示にする
      validationErrors.value.location = locationEmpty
      validationErrors.value.userName = userNameEmpty

      // 最初に未入力だったフィールドのエラーメッセージを表示する
      const errorMsg = locationEmpty
        ? '設置場所は必須です。入力してからもう一度「新規登録」を押してください。'
        : '使用者名は必須です。入力してからもう一度「新規登録」を押してください。'
      sendStatus.value = { type: 'error', message: errorMsg }
      startSendStatusFadeTimer()
      return // 必須チェック失敗: モーダルを開かずに中断する
    }

    // 必須チェック通過: モーダルを開く前にチェックボックスとトークン入力をリセットする
    confirmedNotPersonal.value = false
    enrollmentTokenInput.value = ''
    showNewRegisterWarning.value = true
  }

  /**
   * 確認モーダルを閉じる（キャンセルまたはオーバーレイクリック時）
   *
   * チェックボックスとトークン入力もリセットして次回表示時にクリーンな状態にする。
   */
  function closeRegisterModal() {
    showNewRegisterWarning.value = false
    confirmedNotPersonal.value = false
    enrollmentTokenInput.value = ''
  }

  /**
   * 「登録」ボタン押下時のハンドラ（確認モーダル内）
   *
   * チェックボックスで個人所有でないことを確認済みの場合のみ呼ばれる。
   * モーダルを閉じた後、エージェント番号・APIキーを取得してから saveSettings() を実行する。
   *
   * 処理順序:
   *   1. enrollmentTokenInput の値を退避する（closeRegisterModal がリセットするため必須）
   *   2. モーダルを閉じる（enrollmentTokenInput が '' にリセットされる）
   *   3. pcInfo が未収集の場合は collect_pc_info でホスト名を先に取得する
   *      （エージェント未登録状態では onMounted での PC情報収集がスキップされているため必須）
   *   4. registerAgent(token) でエージェント番号・APIキーをバックエンドから取得してファイルに保存する
   *      - 登録トークン不正・期限切れ・バックエンド未起動の場合はエラーメッセージを表示して中断する
   *   5. collectInfo() で PC 情報収集と取得区分取得を実行する（agentNumber 確定後）
   *   6. saveSettings() で設定を保存し、手動送信を実行する
   *      （この時点で agentNumber・apiKey が取得済みのためレポートに含まれる）
   */
  async function onRegisterConfirm() {
    // モーダルを閉じる前にトークンを退避する
    // closeRegisterModal() 内で enrollmentTokenInput.value = '' にリセットされるため、
    // 先に値を取り出しておかないと registerAgent() に渡すトークンが空になる。
    const token = enrollmentTokenInput.value.trim()
    closeRegisterModal()
    try {
      // エージェント未登録状態では onMounted での PC情報収集がスキップされているため、
      // registerAgent() に渡すホスト名を取得するために先に PC情報を収集する。
      // pcInfo.value が既に設定済みの場合（再登録など）はスキップする。
      if (!pcInfo.value) {
        pcInfo.value = await invoke<PcInfo>('collect_pc_info')
      }
      // 退避したトークンを引数で渡してエージェント番号・APIキーを取得する
      await registerAgent(token)
      // 登録成功: 画面中央のポップアップで通知する（3 秒後に自動消去）
      showRegisterPopup('success', 'エージェントの登録が完了しました')
      // 登録後にPC情報収集と取得区分取得を実行する（agentNumber 確定後に正確な取得区分を取得）
      await collectInfo()
    } catch (e: unknown) {
      // トークン不正・期限切れ・バックエンド未起動・PC情報収集失敗などの場合は
      // 画面中央のポップアップでエラーを表示する（手動で閉じるまで残す）
      const errorMessage = e instanceof Error ? e.message : String(e)
      showRegisterPopup('error', `登録失敗:\n${errorMessage}`)
      return
    }
    // 設置場所・使用者名は onNewRegisterClick で事前検証済みのため、
    // saveSettings のバリデーションも通過する。
    await saveSettings()
  }

  /**
   * 設定値を localStorage に保存し、続けて手動送信を実行する
   * アプリ再起動後も設定が引き継がれるよう永続化する。
   * 画面遷移は行わず、送信結果は設定タブ内の sendStatus で表示する。
   *
   * 設置場所・使用者名は必須。未入力の場合はエラーを表示して送信を中断する。
   * ※ 新規登録フローでは onNewRegisterClick で事前検証済みのため、このチェックも通過する。
   *
   * ※ API URL は application.yml で管理するため localStorage には保存しない
   */
  async function saveSettings(): Promise<void> {
    // 必須項目チェック: 設置場所・使用者名の入力状態を確認する
    const locationEmpty = !settings.value.location.trim()
    const userNameEmpty = !settings.value.userName.trim()

    // いずれかが未入力の場合はエラーを表示して送信を中断する
    if (locationEmpty || userNameEmpty) {
      validationErrors.value.location = locationEmpty
      validationErrors.value.userName = userNameEmpty

      // 最初に未入力だったフィールドのエラーメッセージを sendStatus に表示する
      const errorMsg = locationEmpty
        ? '設置場所は必須です。入力してください。'
        : '使用者名は必須です。入力してください。'
      sendStatus.value = { type: 'error', message: errorMsg }
      startSendStatusFadeTimer()
      return
    }

    // バリデーション通過時はエラーフラグをリセットする
    validationErrors.value.location = false
    validationErrors.value.userName = false

    // 設定値を localStorage に永続化（API URL は application.yml で管理するため含めない）
    localStorage.setItem('assetNumber', settings.value.assetNumber)
    localStorage.setItem('location', settings.value.location)
    localStorage.setItem('userName', settings.value.userName)
    // 取得区分はバックエンド未設定時のユーザー選択値として保存
    // （次回バックエンドが起動していない場合でも選択値を復元するため）
    localStorage.setItem('acquisitionType', settings.value.acquisitionType)

    // 画面遷移せずにそのまま手動送信を実行する（結果は設定タブ内の sendStatus で確認）
    await sendReport()
  }

  /**
   * アプリ起動時の初期化処理
   *
   * 1. application.yml から API URL を読み込む（最初に実行。後続処理で使用するため）
   * 2. PC情報を収集する（ホスト名取得のため最初に実行）
   *    ※ collectInfo() 内で fetchAcquisitionType() が呼ばれるが、
   *      この時点では agentNumber = null のためホスト名のみで検索する。
   * 3. ローカルファイルからエージェント番号を読み込む
   *    - ファイルあり: agentNumber にセットして使用する
   *    - ファイルなし: 自動登録は行わない。設定タブの「新規登録」から手動で登録する。
   * 4. エージェント番号が確定した後、取得区分を再取得する
   *    エージェント番号＋ホスト名の両方で検索し、正確な値を取得する。
   *    - 取得できた場合: 設定欄を読み取り専用にする
   *    - 取得できなかった場合（エージェント番号未取得・資産未紐付け等）: 設定欄を活性化
   */
  onMounted(async () => {
    // ① application.yml から API URL を読み込む（後続の全 API 呼び出しで使用するため最初に実行）
    await loadApiConfig()

    // ② ローカルファイルからエージェント番号・APIキーを読み込む
    //    ※ エージェント番号の有無によって後続処理（PC情報収集など）の実行可否を決めるため、
    //      PC情報収集より先に実行する。
    //    保存先: %LOCALAPPDATA%\{app-name}\.agent_id / .agent_key（Windowsの場合）
    //    ファイルが存在しない場合（初回起動）は自動登録を行わない。
    //    ユーザーが設定タブの「新規登録」ボタンを押して確認フローを経由した場合のみ登録する。
    try {
      const stored = await invoke<string | null>('load_agent_number')
      if (stored) {
        agentNumber.value = stored
        console.info('エージェント番号をファイルから読み込みました:', stored)
      } else {
        // エージェント番号なし → 設定タブのみ使用可能。PC情報収集などは行わない。
        // localStorage に前回の設定値が残っている場合はクリアして初期状態に戻す。
        // （別PCでの再登録時や .agent_id 削除後の起動で古い値が表示されるのを防ぐ）
        clearSettings()
        console.info('エージェント番号が未取得です。設定タブの「新規登録」から登録してください。')
      }
    } catch (e) {
      // 読み込みエラー時も設定値をクリアして安全な初期状態にする
      clearSettings()
      console.warn('エージェント番号の読み込みに失敗しました:', e)
    }

    try {
      const storedKey = await invoke<string | null>('load_api_key')
      if (storedKey) {
        apiKey.value = storedKey
        console.info('APIキーをファイルから読み込みました')
      } else {
        console.info('APIキーが未設定です。新規登録後に保存されます。')
      }
    } catch (e) {
      console.warn('APIキーの読み込みに失敗しました:', e)
    }

    // ③ エージェント番号が取得済みの場合のみ PC 情報収集・取得区分取得を実行する
    //    エージェント番号未取得時はこれらの処理を一切行わず、設定タブで「新規登録」を促す。
    if (agentNumber.value) {
      // エージェント番号あり → PC情報タブへ遷移してから情報収集を開始する
      navigateToInfo()
      // PC情報収集（内部で fetchAcquisitionType() も実行）
      await collectInfo()
      // エージェント番号確定後に取得区分を再取得する（エージェント番号ベースで正確に検索）
      await fetchAcquisitionType()
    }
    // エージェント番号なし → currentView は 'settings' のまま（初期値）。新規登録を待つ。
  })
</script>

<style scoped>
  /* ==============================
   アプリ全体レイアウト
   ============================== */

  /* 縦方向に並べる全画面レイアウト */
  .agent-app {
    display: flex;
    flex-direction: column;
    height: 100vh;
    font-family: 'Segoe UI', sans-serif;
    background: #f3f4f6;
  }

  /* ヘッダー（濃紺背景・白テキスト） */
  .agent-header {
    background: #1a1a2e;
    color: white;
    padding: 12px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  /* ヘッダータイトル横のアイコン画像（imageフォルダより読み込み） */
  .header-icon {
    width: 20px;
    height: 20px;
    vertical-align: middle;
    margin-right: 6px;
    /* 白色テキストに合わせてアイコンを白く反転する */
    filter: brightness(0) invert(1);
  }

  /* バージョン表示（小・半透明） */
  .version {
    font-size: 12px;
    opacity: 0.6;
  }

  /* ==============================
   タブコンテンツ
   ============================== */

  /* 各タブの共通パネル */
  .view-panel {
    flex: 1;
    /* 残りのスペースを全て使用 */
    padding: 20px;
    overflow-y: auto;
    /* コンテンツが多い場合はスクロール */
  }

  .view-panel h2 {
    font-size: 18px;
    margin-bottom: 16px;
    color: #1a1a2e;
  }

  /* ==============================
   PC 情報グリッド
   ============================== */

  /* 情報行を縦に並べるグリッド */
  .info-grid {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 20px;
  }

  /* 1行: ラベル左・値右 */
  .info-row {
    display: flex;
    justify-content: space-between;
    background: white;
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 14px;
  }

  /* 項目ラベル（グレー） */
  .info-label {
    color: #6b7280;
    font-weight: 500;
  }

  /* ==============================
   フォームグループ（設定画面）
   ============================== */

  .form-group {
    margin-bottom: 16px;
  }

  .form-group label {
    display: block;
    font-size: 13px;
    color: #374151;
    margin-bottom: 4px;
  }

  .form-group input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 14px;
    box-sizing: border-box;
  }

  /* 読み取り専用フィールド（グレー背景でユーザー入力不可を明示） */
  .input-readonly {
    background: #f3f4f6;
    color: #6b7280;
    cursor: default;
  }

  /* セレクトボックス（購入/レンタル区分など） */
  .select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 14px;
    box-sizing: border-box;
    background: white;
    appearance: auto;
  }

  /* セレクトボックス disabled 状態（読み取り専用） */
  .select:disabled {
    background: #f3f4f6;
    color: #6b7280;
    cursor: default;
  }

  /* 補足説明テキスト（フィールド下に表示） */
  .hint-text {
    display: block;
    font-size: 12px;
    color: #9ca3af;
    margin-top: 4px;
  }

  /* 活性化時の補足テキスト（選択可能であることを示す） */
  .hint-text--active {
    color: #6366f1;
  }

  /* 必須マーク（ラベル右横に赤文字で「*必須」を表示） */
  .required-mark {
    font-size: 11px;
    color: #dc2626;
    font-weight: 600;
    margin-left: 4px;
    vertical-align: middle;
  }

  /* バリデーションエラー時の入力フィールド（赤ボーダー） */
  .input-error {
    border-color: #dc2626 !important;
    background: #fff5f5;
    box-shadow: 0 0 0 2px rgba(220, 38, 38, 0.15);
  }

  /* バリデーションエラーメッセージテキスト（フィールド下に赤文字で表示） */
  .error-text {
    display: block;
    font-size: 12px;
    color: #dc2626;
    margin-top: 4px;
    font-weight: 500;
  }

  /* ==============================
   ボタン
   ============================== */

  /* メインアクションボタン */
  .btn-primary {
    background: #6366f1;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    width: 100%;
  }

  /* disabled 状態: 半透明でカーソル変更 */
  .btn-primary:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  /* ==============================
   WindowsUpdate 適用判定
   ============================== */

  /* セクション全体：上に区切り線を入れて視覚的に分離する */
  .win-update-section {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #e5e7eb;
  }

  /* ボタンとステータスメッセージを横並びにするコンテナ */
  .win-update-row {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-wrap: wrap;
    /* 画面が狭い場合は折り返す */
  }

  /* 適用判定ボタン（btn-primary の 100% 幅を上書きしてコンパクトにする） */
  .btn-win-update {
    width: auto;
    white-space: nowrap;
    padding: 10px 20px;
  }

  /* ==============================
   WindowsUpdate 未適用一覧 ポップアップ
   ============================== */

  /* ポップアップ内コンテンツ（modal-content を上書き） */
  .win-update-popup {
    width: 340px;
  }

  /* ヘッダー行：タイトルと閉じるボタンを横並び */
  .win-update-popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .win-update-popup-title {
    font-size: 15px;
    font-weight: 700;
    color: #92400e;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  /* ポップアップタイトル横のアイコン画像 */
  .popup-title-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }

  /* 閉じるボタン（×） */
  .win-update-popup-close {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #6b7280;
    padding: 0 4px;
    line-height: 1;
  }

  .win-update-popup-close:hover {
    color: #111827;
  }

  /* 件数メッセージ */
  .win-update-popup-count {
    font-size: 13px;
    font-weight: 600;
    color: #92400e;
    margin-bottom: 10px;
  }

  /* KB ID リスト */
  .win-update-popup-list {
    list-style: none;
    padding: 0;
    margin: 0 0 12px;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .win-update-popup-item {
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 13px;
    background: #fff7ed;
    border: 1px solid #fed7aa;
    border-radius: 6px;
    padding: 4px 10px;
    color: #7c2d12;
  }

  /* 注記テキスト */
  .win-update-popup-note {
    font-size: 11px;
    color: #6b7280;
    margin: 0;
  }

  /* 新規登録ボタン（オレンジ系でアクション意図を強調） */
  .btn-register {
    background: #f59e0b;
  }

  .btn-register:hover {
    background: #d97706;
  }

  /* ==============================
   新規登録 確認モーダル
   ============================== */

  /* 半透明オーバーレイ（アプリ全体を覆う） */
  .modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 20px;
  }

  /* モーダル本体（白カード） */
  .modal-content {
    background: white;
    border-radius: 12px;
    padding: 24px 20px;
    width: 100%;
    max-width: 360px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  }

  /* モーダル内警告エリア */
  .modal-warning {
    background: #fef9c3;
    border: 1px solid #fde047;
    border-radius: 8px;
    padding: 14px;
    display: flex;
    gap: 10px;
    align-items: flex-start;
  }

  /* 警告アイコン画像（imageフォルダの icon-warning.svg を img 要素で表示） */
  .modal-warning-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    margin-top: 1px;
  }

  /* 警告テキスト */
  .modal-warning p {
    margin: 0;
    font-size: 14px;
    color: #713f12;
    line-height: 1.5;
  }

  /* チェックボックスラベル（クリック領域を広く） */
  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #374151;
    cursor: pointer;
    user-select: none;
    padding: 10px 12px;
    background: #f9fafb;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }

  /* チェックボックス本体（サイズ拡大） */
  .checkbox-input {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    cursor: pointer;
    accent-color: #6366f1;
  }

  /* 登録トークン入力グループ（モーダル内） */
  .modal-token-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  /* 登録トークン入力ラベル */
  .modal-token-label {
    font-size: 13px;
    color: #374151;
    font-weight: 500;
  }

  /* 登録トークン入力フィールド */
  .modal-token-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 14px;
    box-sizing: border-box;
    font-family: 'Consolas', 'Courier New', monospace; /* トークン文字列は等幅フォントで表示 */
  }

  /* 入力フォーカス時: インジゴ色のボーダーとグロー */
  .modal-token-input:focus {
    outline: none;
    border-color: #6366f1;
    box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
  }

  /* キャンセルボタン（グレー系・サブアクション） */
  .btn-cancel {
    background: #f3f4f6;
    color: #374151;
    border: 1px solid #d1d5db;
    padding: 10px 24px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    width: 100%;
  }

  .btn-cancel:hover {
    background: #e5e7eb;
  }

  /* 警告メッセージ（資産番号未設定時） */
  .warning-alert {
    background: #fef9c3;
    border: 1px solid #fde047;
    color: #713f12;
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 14px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  /* 警告アラート内のインラインアイコン画像 */
  .inline-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    vertical-align: middle;
  }

  /* ==============================
   ボトムナビゲーション
   ============================== */

  /* 下部固定のタブバー */
  .bottom-nav {
    display: flex;
    border-top: 1px solid #e5e7eb;
    background: white;
  }

  /* タブボタン共通 */
  .bottom-nav button {
    flex: 1;
    padding: 14px 8px;
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 13px;
    color: #6b7280;
    transition: color 0.15s;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 3px;
  }

  /* タブボタン内のアイコン画像（imageフォルダより読み込み） */
  .nav-tab-icon {
    width: 20px;
    height: 20px;
    /* SVG の stroke="currentColor" でボタンのテキスト色を継承させる */
    filter: invert(44%) sepia(8%) saturate(479%) hue-rotate(182deg) brightness(95%) contrast(90%);
  }

  /* アクティブタブのアイコンをインジゴ色に合わせる */
  .bottom-nav button.active .nav-tab-icon {
    filter: invert(38%) sepia(62%) saturate(648%) hue-rotate(205deg) brightness(97%) contrast(101%);
  }

  /* アクティブなタブ: インジゴ色・上部ボーダー */
  .bottom-nav button.active {
    color: #6366f1;
    font-weight: 600;
    border-top: 2px solid #6366f1;
  }

  /* ==============================
   グローバルローディングオーバーレイ
   ============================== */

  /* 画面全体を覆う半透明オーバーレイ（全モーダルより手前に表示） */
  .global-loading-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 400;
  }

  /* スピナーとテキストを縦に並べる */
  .global-loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 14px;
  }

  /* 回転スピナー */
  .global-loading-spinner {
    width: 40px;
    height: 40px;
    border: 4px solid rgba(255, 255, 255, 0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  /* スピナー回転アニメーション */
  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  /* 「処理中...」テキスト */
  .global-loading-text {
    color: white;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
</style>