package com.company.pcmgmt.service.agent;

import com.company.pcmgmt.api.dto.request.agent.EnrollmentTokenRequest;
import com.company.pcmgmt.api.dto.response.agent.EnrollmentTokenResponse;
import com.company.pcmgmt.domain.entity.AgentEnrollmentToken;
import com.company.pcmgmt.domain.mapper.agent.AgentEnrollmentTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * エージェント登録トークン管理サービス
 *
 * <p>管理者・IT担当者がエージェントの初回登録に使用するトークンを発行・管理する。
 * トークンは発行から24時間有効で、1回限り使用できる。</p>
 *
 * <p>トークンのライフサイクル:
 * <ol>
 *   <li>管理者がWebコンソールでトークンを発行（{@link #issue}）</li>
 *   <li>IT担当者がエージェントの application.yml にトークンを設定してインストール</li>
 *   <li>エージェント初回起動時に POST /api/v1/agent/register へトークンを送信</li>
 *   <li>{@link AgentService#register} がトークンを検証し、使用済みにマークする</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentTokenService {

    /** 登録トークンの有効期間（時間） */
    private static final int TOKEN_VALID_HOURS = 24;

    /** 登録トークンの DB アクセスを担うマッパー */
    private final AgentEnrollmentTokenMapper tokenMapper;

    // =========================================================
    // トークン発行
    // =========================================================

    /**
     * 新しい登録トークンを発行する
     *
     * <p>UUID を生成してトークンとし、有効期限（24時間後）を設定して DB に保存する。</p>
     *
     * @param req            発行リクエスト（任意メモを含む）
     * @param createdByUserId トークン発行者のユーザーID（ログイン中ユーザーのID）
     * @return 発行したトークン情報のレスポンス
     */
    @Transactional
    public EnrollmentTokenResponse issue(EnrollmentTokenRequest req, Long createdByUserId) {
        // UUID でトークン文字列を生成する（ハイフンを除去して可読性を上げる）
        String tokenStr = UUID.randomUUID().toString().replace("-", "");

        // 有効期限: 現在日時 + 24時間
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_VALID_HOURS);

        // エンティティを構築して INSERT
        AgentEnrollmentToken token = new AgentEnrollmentToken();
        token.setToken(tokenStr);
        token.setExpiresAt(expiresAt);
        token.setNote(req != null ? req.getNote() : null);
        token.setCreatedByUserId(createdByUserId);

        tokenMapper.insert(token);

        log.info("登録トークンを発行しました: id={}, expiresAt={}, createdByUserId={}",
            token.getId(), expiresAt, createdByUserId);

        return toResponse(token);
    }

    // =========================================================
    // トークン一覧取得
    // =========================================================

    /**
     * 全登録トークンを発行日時の降順で返す
     *
     * @return トークン一覧（ステータスを付与済み）
     */
    public List<EnrollmentTokenResponse> findAll() {
        return tokenMapper.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // =========================================================
    // トークン削除（強制無効化）
    // =========================================================

    /**
     * 指定IDの登録トークンを削除する
     *
     * <p>未使用トークンをキャンセルする場合、または使用済みレコードを整理する場合に使用する。</p>
     *
     * @param id 削除するトークンのID
     */
    @Transactional
    public void delete(Long id) {
        int deleted = tokenMapper.deleteById(id);
        if (deleted == 0) {
            throw new IllegalArgumentException("指定されたトークンが見つかりません: id=" + id);
        }
        log.info("登録トークンを削除しました: id={}", id);
    }

    // =========================================================
    // トークン検証（AgentService から呼び出す）
    // =========================================================

    /**
     * 登録トークンを検証して有効な場合は AgentEnrollmentToken を返す
     *
     * <p>以下をすべて満たす場合に有効と判定する:
     * <ol>
     *   <li>トークン文字列が DB に存在する</li>
     *   <li>未使用（used_at IS NULL）</li>
     *   <li>有効期限内（expires_at &gt; 現在日時）</li>
     * </ol>
     * </p>
     *
     * @param tokenStr 検証するトークン文字列
     * @return 有効なトークンエンティティ
     * @throws IllegalArgumentException トークンが無効・存在しない・使用済み・期限切れの場合
     */
    public AgentEnrollmentToken validateToken(String tokenStr) {
        // トークンが存在するか確認
        AgentEnrollmentToken token = tokenMapper.findByToken(tokenStr);
        if (token == null) {
            log.warn("無効な登録トークンが使用されました: token={}", tokenStr);
            throw new IllegalArgumentException("無効な登録トークンです");
        }

        // 使用済みチェック
        if (token.getUsedAt() != null) {
            log.warn("使用済みの登録トークンが再利用されました: token={}, usedAt={}", tokenStr, token.getUsedAt());
            throw new IllegalArgumentException("このトークンはすでに使用済みです");
        }

        // 有効期限チェック（発行から24時間以内）
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            log.warn("有効期限切れの登録トークンが使用されました: token={}, expiresAt={}", tokenStr, token.getExpiresAt());
            throw new IllegalArgumentException("このトークンは有効期限切れです（発行から24時間が経過しました）");
        }

        return token;
    }

    // =========================================================
    // プライベートユーティリティ
    // =========================================================

    /**
     * AgentEnrollmentToken エンティティを EnrollmentTokenResponse に変換する
     *
     * <p>ステータスは以下のルールで決定する:
     * <ul>
     *   <li>used_at != null         → USED（使用済み）</li>
     *   <li>expires_at &lt; 現在日時 → EXPIRED（期限切れ）</li>
     *   <li>それ以外                → UNUSED（未使用・有効）</li>
     * </ul>
     * </p>
     *
     * @param token エンティティ
     * @return レスポンス DTO
     */
    private EnrollmentTokenResponse toResponse(AgentEnrollmentToken token) {
        EnrollmentTokenResponse res = new EnrollmentTokenResponse();
        res.setId(token.getId());
        res.setToken(token.getToken());
        res.setExpiresAt(token.getExpiresAt());
        res.setUsedAt(token.getUsedAt());
        res.setUsedByAgentNumber(token.getUsedByAgentNumber());
        res.setNote(token.getNote());
        res.setCreatedAt(token.getCreatedAt());

        // ステータスを計算する
        if (token.getUsedAt() != null) {
            res.setStatus("USED");
        } else if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            res.setStatus("EXPIRED");
        } else {
            res.setStatus("UNUSED");
        }

        return res;
    }
}
