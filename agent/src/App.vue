<!--
  agent/src/App.vue
  -----------------------------------------------
  PC管理エージェント - メインコンポーネント（Tauri デスクトップアプリ）

  3画面（タブ）構成:
    - 📻 PC情報: 収集した PC のハードウェア・OS 情報を表示
    - 📤 送信: 収集情報をバックエンド API へ POST 送信
    - ⚙️ 設定: 資産番号・設置場所を設定して永続化

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
      <span>🖥️ PC管理エージェント</span>
      <span class="version">v1.0.0</span>
    </header>

    <!-- =====================
         タブ: PC情報表示（画面A）
         ===================== -->
    <div v-if="currentView === 'info'" class="view-panel">
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
        <div v-for="(nic, idx) in pcInfo.network" :key="idx" class="info-row">
          <span class="info-label">IPアドレス {{ idx > 0 ? idx + 1 : '' }}</span>
          <span>{{ nic.ip }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">収集日時</span>
          <span>{{ pcInfo.collected_at }}</span>
        </div>
        <!-- インストール済みソフトウェア件数 -->
        <!-- <div class="info-row">
          <span class="info-label">検出ソフトウェア</span>
          <span>{{ pcInfo.software.length }} 件</span>
        </div> -->
      </div>

      <!-- 情報収集ボタン（Tauri コマンドを呼び出す） -->
      <button class="btn-primary" @click="collectInfo">情報を取得</button>
    </div>

    <!-- =====================
         タブ: 送信（画面B）
         ===================== -->
    <div v-if="currentView === 'send'" class="view-panel">
      <h2>情報送信</h2>

      <!-- 送信前に資産番号の設定を促す警告 -->
      <div v-if="!settings.assetNumber" class="warning-alert">
        ⚠️ 設定画面で管理番号（資産番号）を入力してください
      </div>

      <!-- 送信ステータスメッセージ -->
      <p class="status-msg" :class="sendStatus.type">{{ sendStatus.message }}</p>

      <!-- 送信ボタン（PC情報未収集またはエージェント番号未取得の場合は disabled） -->
      <button class="btn-primary" @click="sendReport"
        :disabled="!pcInfo || !settings.assetNumber || !agentNumber">
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
      <div class="form-group">
        <label>API URL</label>
        <input :value="apiUrl" disabled class="input-readonly"
          title="application.yml で設定されたバックエンドAPIのURL（管理者のみ変更可）" />
        <span class="hint-text">※ application.yml で管理者が設定します</span>
      </div>

      <!-- 設置場所入力（pc_assetsのlocationカラムに登録される） -->
      <div class="form-group">
        <label>設置場所</label>
        <input v-model="settings.location" placeholder="3F-営業部" />
      </div>

      <!-- 使用者名入力（pc_assetsのassigned_employee_idに紐付けられる） -->
      <div class="form-group">
        <label>使用者名</label>
        <input v-model="settings.userName" placeholder="山田 太郎" />
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
        - エージェント番号取得済み: 「保存」→ そのまま saveSettings を実行
        - エージェント番号未取得 : 「新規登録」→ 確認モーダルを表示してから送信
      -->
      <button v-if="agentNumber" class="btn-primary" @click="saveSettings">保存</button>
      <button v-else class="btn-primary btn-register" @click="onNewRegisterClick">新規登録</button>
    </div>

    <!-- =====================
         新規登録 確認モーダル
         ===================== -->
    <div v-if="showNewRegisterWarning" class="modal-overlay" @click.self="closeRegisterModal">
      <div class="modal-content">
        <!-- 警告メッセージ -->
        <div class="modal-warning">
          <span class="modal-warning-icon">⚠️</span>
          <p>PC情報をサーバーに送信します。個人所有のPCでないことを確認してください。</p>
        </div>

        <!-- 個人所有でないことの確認チェックボックス -->
        <label class="checkbox-label">
          <input type="checkbox" v-model="confirmedNotPersonal" class="checkbox-input" />
          <span>個人所有のPCではありません</span>
        </label>

        <!-- チェックされた場合のみ「登録」ボタンを表示 -->
        <button v-if="confirmedNotPersonal" class="btn-primary" @click="onRegisterConfirm">登録</button>

        <!-- キャンセルボタン（常に表示） -->
        <button class="btn-cancel" @click="closeRegisterModal">キャンセル</button>
      </div>
    </div>

    <!-- =====================
         ボトムナビゲーション
         ===================== -->
    <nav class="bottom-nav">
      <!-- アクティブなタブは active クラスでハイライト -->
      <button :class="{ active: currentView === 'info' }" @click="currentView = 'info'">
        💻 PC情報
      </button>
      <button :class="{ active: currentView === 'send' }" @click="currentView = 'send'">
        📤 送信
      </button>
      <button :class="{ active: currentView === 'settings' }" @click="currentView = 'settings'">
        ⚙️ 設定
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
  }

  /** 現在表示中のタブ */
  const currentView = ref < View > ('info')

  /** 収集済み PC 情報（null = 未収集） */
  const pcInfo = ref < PcInfo | null > (null)

  /**
   * application.yml から読み込んだ API のベース URL
   * - 初期値は空文字（起動時に load_config() で設定される）
   * - 読み込みに失敗した場合はデフォルト値 "http://localhost:8080/api/v1" が設定される
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

  /** 送信ステータスメッセージ（type: 'success' | 'error' | 'loading' | ''） */
  const sendStatus = ref({ type: '', message: '送信ボタンを押してください' })

  /**
   * 新規登録確認モーダルの表示フラグ
   * true: モーダルを表示（エージェント番号未取得時に「新規登録」ボタン押下で true になる）
   */
  const showNewRegisterWarning = ref(false)

  /**
   * 「個人所有のPCではありませんか？」チェックボックスの状態
   * true: チェックされている → 「登録」ボタンを表示する
   */
  const confirmedNotPersonal = ref(false)

  /**
   * バックエンドから取得した取得区分
   * - 文字列（"PURCHASE" / "RENTAL"）: バックエンドに設定済み → 選択欄を読み取り専用にする
   * - null: 取得できなかった（資産未登録 / バックエンド未起動）→ 選択欄を活性化する
   */
  const acquisitionTypeFromBackend = ref < string | null > (null)

  /**
   * 設定値（localStorage に永続化）
   * assetNumber:     資産番号（例: "PC-00123"）
   * location:        設置場所（pc_assets.location に登録）
   * userName:        使用者名（社員名と照合して pc_assets.assigned_employee_id に登録）
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
   * application.yml から API 設定（ベース URL）を読み込む
   *
   * Rust の load_config コマンドを invoke() で呼び出す。
   * 成功時は apiUrl に設定されたURLをセット、失敗時はデフォルト値を使用する。
   * application.yml が見つからない場合も Rust 側でデフォルト値を返す。
   */
  async function loadApiConfig() {
    try {
      const config = await invoke<ApiConfig>('load_config')
      apiUrl.value = config.base_url
      console.info('application.yml からAPIのURLを読み込みました:', config.base_url)
    } catch (e) {
      // 読み込みに失敗した場合はデフォルト値を使用する
      apiUrl.value = 'http://localhost:8080/api/v1'
      console.warn('application.yml の読み込みに失敗しました。デフォルト値を使用します:', e)
    }
  }

  /**
   * PC 情報を収集する
   * Rust の collect_pc_info コマンドを invoke() で呼び出す。
   * 成功時は pcInfo に結果をセット、失敗時はコンソールにエラーを出力する。
   */
  async function collectInfo() {
    try {
      pcInfo.value = await invoke < PcInfo > ('collect_pc_info')
    } catch (e) {
      console.error('情報収集エラー:', e)
    }
  }

  /**
   * エージェントをバックエンドに初回登録してエージェント番号を取得し、ファイルに保存する
   *
   * ファイルにエージェント番号が存在しない場合のみ呼び出す。
   * 取得したエージェント番号は save_agent_number コマンドでローカルファイルに永続化する。
   * バックエンド未起動などでエラーが発生した場合は警告ログのみ出力し続行する。
   */
  async function registerAgent() {
    if (!pcInfo.value) return  // PC情報が収集できていない場合はスキップ
    try {
      // バックエンドにホスト名を送信してエージェント番号を発行してもらう
      const number = await invoke < string > ('register_agent', {
        apiUrl: apiUrl.value,
        hostname: pcInfo.value.hostname,
      })

      // 発行されたエージェント番号をローカルファイルに永続保存する
      // %LOCALAPPDATA%\{app-name}\.agent_id に保存（Windowsでは隠し属性付き）
      await invoke('save_agent_number', { agentNumber: number })

      agentNumber.value = number
      console.info('エージェント番号を取得・保存しました:', number)
    } catch (e) {
      // バックエンド未起動等でも起動は継続する（次回起動時に再試行される）
      console.warn('エージェント番号の取得に失敗しました（バックエンドが起動していない可能性）:', e)
    }
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
      const acqType = await invoke < string | null > ('fetch_asset_acquisition_type', {
        apiUrl:      apiUrl.value,
        agentNumber: agentNumber.value ?? '',       // 未取得時は空文字（バックエンド側でスキップ）
        hostname:    pcInfo.value?.hostname ?? '',  // フォールバック検索用ホスト名
      })
      if (acqType) {
        // バックエンドに設定済み → 読み取り専用にして表示する
        acquisitionTypeFromBackend.value = acqType
        // 設定欄の表示値もバックエンドの値に合わせる
        settings.value.acquisitionType = acqType
        console.info('取得区分をバックエンドから取得しました:', acqType)
      } else {
        // 未設定 → 選択欄を活性化（ユーザーが入力可能）
        acquisitionTypeFromBackend.value = null
        console.info('取得区分はバックエンドに未設定です。選択欄を活性化します。')
      }
    } catch (e) {
      // エラー時も活性化（バックエンド未起動等）
      acquisitionTypeFromBackend.value = null
      console.warn('取得区分の取得に失敗しました:', e)
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
      sendStatus.value = { type: 'loading', message: '送信中...' }

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

      // Rust コマンドでバックエンド API へ POST 送信
      await invoke('send_report', { apiUrl: apiUrl.value, report })

      // 送信成功後の処理:
      // 取得区分を送信した場合のみ、バックエンドから再取得して項目に反映する
      // 仕様: 「区分の取得および項目への値設定を行う」
      //       「すでに設定されている場合は区分の取得を行わない」
      //       → 送信していない場合（null）は再取得しない
      if (acquisitionTypeToSend !== null) {
        await fetchAcquisitionType()
        console.info('取得区分を送信後にバックエンドから再取得しました')
      }

      sendStatus.value = { type: 'success', message: '✅ 送信成功しました' }
    } catch (e: any) {
      sendStatus.value = { type: 'error', message: `❌ ${e}` }
    }
  }

  /**
   * 「新規登録」ボタン押下時のハンドラ
   *
   * エージェント番号が未取得の状態で呼び出される。
   * 確認モーダルを表示してチェックボックス＋登録ボタンの確認フローを開始する。
   */
  function onNewRegisterClick() {
    // モーダルを開く前にチェックボックスをリセットする
    confirmedNotPersonal.value = false
    showNewRegisterWarning.value = true
  }

  /**
   * 確認モーダルを閉じる（キャンセルまたはオーバーレイクリック時）
   *
   * チェックボックスもリセットして次回表示時にクリーンな状態にする。
   */
  function closeRegisterModal() {
    showNewRegisterWarning.value = false
    confirmedNotPersonal.value = false
  }

  /**
   * 「登録」ボタン押下時のハンドラ（確認モーダル内）
   *
   * チェックボックスで個人所有でないことを確認済みの場合のみ呼ばれる。
   * モーダルを閉じた後、エージェント番号を取得してから saveSettings() を実行する。
   *
   * 処理順序:
   *   1. モーダルを閉じる
   *   2. registerAgent() でエージェント番号をバックエンドから取得してファイルに保存する
   *   3. saveSettings() で設定を保存し、手動送信を実行する
   *      （この時点で agentNumber が取得済みのためレポートに agent_number が含まれる）
   */
  async function onRegisterConfirm() {
    closeRegisterModal()
    // エージェント番号を取得してから送信する（取得失敗時は agentNumber = null のまま送信）
    await registerAgent()
    await saveSettings()
  }

  /**
   * 設定値を localStorage に保存し、続けて手動送信を実行する
   * アプリ再起動後も設定が引き継がれるよう永続化する。
   * 保存後は送信タブへ切り替え、手動送信の結果を表示する。
   *
   * ※ API URL は application.yml で管理するため localStorage には保存しない
   */
  async function saveSettings() {
    // 設定値を localStorage に永続化（API URL は application.yml で管理するため含めない）
    localStorage.setItem('assetNumber', settings.value.assetNumber)
    localStorage.setItem('location', settings.value.location)
    localStorage.setItem('userName', settings.value.userName)
    // 取得区分はバックエンド未設定時のユーザー選択値として保存
    // （次回バックエンドが起動していない場合でも選択値を復元するため）
    localStorage.setItem('acquisitionType', settings.value.acquisitionType)

    // 送信タブへ切り替えて手動送信を実行（結果は送信タブの sendStatus で確認）
    currentView.value = 'send'
    await sendReport()
  }

  /**
   * アプリ起動時の初期化処理
   *
   * 1. application.yml から API URL を読み込む（最初に実行。後続処理で使用するため）
   * 2. PC情報を収集する（ホスト名はエージェント登録に必要なため先に実行）
   * 3. ローカルファイルからエージェント番号を読み込む
   *    - ファイルあり: agentNumber にセットして使用する
   *    - ファイルなし: 自動登録は行わない。設定タブの「新規登録」から手動で登録する。
   * 4. エージェント番号を使ってバックエンドから取得区分（購入/レンタル）を取得する
   *    - 取得できた場合: 設定欄を読み取り専用にする
   *    - 取得できなかった場合（エージェント番号未取得・資産未紐付け等）: 設定欄を活性化
   */
  onMounted(async () => {
    // ① application.yml から API URL を読み込む（後続の全 API 呼び出しで使用するため最初に実行）
    await loadApiConfig()

    // ② まず PC 情報を収集する（ホスト名取得のため最初に実行）
    await collectInfo()

    // ③ ローカルファイルからエージェント番号を読み込む
    //    保存先: %LOCALAPPDATA%\{app-name}\.agent_id（Windowsの場合）
    //    ※ ファイルが存在しない場合（初回起動）は自動登録を行わない。
    //       ユーザーが設定タブの「新規登録」ボタンを押して確認フローを経由した場合のみ登録する。
    try {
      const stored = await invoke < string | null > ('load_agent_number')
      if (stored) {
        // ファイルに保存済み → そのまま使用する
        agentNumber.value = stored
        console.info('エージェント番号をファイルから読み込みました:', stored)
      } else {
        // ファイルなし → 自動登録はしない。agentNumber = null のまま維持する。
        // 設定タブの「新規登録」ボタンから手動で登録してもらう。
        console.info('エージェント番号が未取得です。設定タブの「新規登録」から登録してください。')
      }
    } catch (e) {
      // 読み込みエラー時も自動登録は行わない
      console.warn('エージェント番号の読み込みに失敗しました:', e)
    }

    // ④ バックエンドから取得区分（購入/レンタル）を取得する（②③の後に実行）
    //    （エージェント番号取得処理と並行して実行可能だが、
    //     ホスト名が確定した後に実行する必要があるため collectInfo() の後に実行）
    await fetchAcquisitionType()
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
   ステータスメッセージ
   ============================== */

  .status-msg {
    padding: 10px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
  }

  .status-msg.success {
    background: #d1fae5;
    color: #065f46;
  }

  /* 緑: 送信成功 */
  .status-msg.error {
    background: #fee2e2;
    color: #dc2626;
  }

  /* 赤: エラー */
  .status-msg.loading {
    background: #ede9fe;
    color: #5b21b6;
  }

  /* 紫: 送信中 */

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

  /* 警告アイコン */
  .modal-warning-icon {
    font-size: 18px;
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
  }

  /* アクティブなタブ: インジゴ色・上部ボーダー */
  .bottom-nav button.active {
    color: #6366f1;
    font-weight: 600;
    border-top: 2px solid #6366f1;
  }
</style>