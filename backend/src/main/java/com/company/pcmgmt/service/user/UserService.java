package com.company.pcmgmt.service.user;

import com.company.pcmgmt.api.dto.request.user.UserCreateRequest;
import com.company.pcmgmt.api.dto.request.user.UserUpdateRequest;
import com.company.pcmgmt.api.dto.response.user.UserResponse;
import com.company.pcmgmt.domain.entity.User;
import com.company.pcmgmt.domain.mapper.user.UserMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * システムユーザー管理サービス
 *
 * <p>システムユーザーの CRUD 操作を担当するビジネスロジック層。
 * 管理者（ADMIN ロール）のみがアクセスできるエンドポイントに対応する。
 * パスワードの BCrypt ハッシュ化はこのサービスで行う。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /** ユーザー情報の DB アクセスを担うマッパー */
    private final UserMapper userMapper;

    /** パスワードのハッシュ化に使用するエンコーダー（BCrypt） */
    private final PasswordEncoder passwordEncoder;

    /**
     * 全ユーザーを取得する
     *
     * @return UserResponse のリスト（パスワードハッシュは含まない）
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        // 全ユーザーを取得し、レスポンス DTO に変換して返す
        return userMapper.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 指定IDのユーザーを取得する
     *
     * @param id ユーザーID
     * @return UserResponse
     * @throws ResourceNotFoundException 指定IDのユーザーが存在しない場合
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userMapper.findById(id);
        // 存在しない場合は 404 例外をスロー
        if (user == null) throw new ResourceNotFoundException("ユーザーが見つかりません: id=" + id);
        return toResponse(user);
    }

    /**
     * システムユーザーを新規登録する
     *
     * @param req 登録リクエスト（パスワードは平文）
     * @return 登録後のユーザーレスポンス（パスワードハッシュは含まない）
     * @throws IllegalArgumentException ユーザー名が既に使用されている場合
     */
    @Transactional
    public UserResponse create(UserCreateRequest req) {
        // ユーザー名の重複チェック（excludeId=null は新規登録を意味する）
        if (userMapper.existsByUsername(req.getUsername(), null)) {
            throw new IllegalArgumentException("ユーザー名 [" + req.getUsername() + "] は既に使用されています");
        }

        // リクエストからエンティティを組み立て
        User user = new User();
        user.setUsername(req.getUsername());   // ログインユーザー名
        // パスワードを BCrypt でハッシュ化して保存（平文は保存しない）
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName()); // 表示名
        user.setRole(req.getRole());               // ロール
        user.setEmail(req.getEmail());             // メールアドレス
        // 新規登録は必ず有効状態（true）で開始
        user.setIsActive(true);

        // DBに保存（INSERT 後、user.id に自動採番IDがセットされる）
        userMapper.insert(user);
        log.info("ユーザー登録: username={}", user.getUsername());

        // 保存後のデータをレスポンス DTO に変換して返す
        return toResponse(userMapper.findById(user.getId()));
    }

    /**
     * ユーザー情報を更新する
     *
     * <p>パスワードが指定された場合のみハッシュ化して更新する。
     * 空文字や null の場合はパスワードを変更しない。</p>
     *
     * @param id  更新対象のユーザーID
     * @param req 更新リクエスト
     * @return 更新後のユーザーレスポンス
     * @throws ResourceNotFoundException 指定IDのユーザーが存在しない場合
     */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        // 更新対象の存在確認（存在しない場合は 404 例外をスロー）
        User user = userMapper.findById(id);
        if (user == null) throw new ResourceNotFoundException("ユーザーが見つかりません: id=" + id);

        // 既存エンティティに更新値をセット
        user.setDisplayName(req.getDisplayName()); // 表示名
        user.setRole(req.getRole());               // ロール
        user.setEmail(req.getEmail());             // メールアドレス
        // isActive が指定された場合のみ更新
        if (req.getIsActive() != null) user.setIsActive(req.getIsActive());
        // パスワードが入力された場合のみ BCrypt でハッシュ化して更新
        if (StringUtils.hasText(req.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        userMapper.update(user);
        log.info("ユーザー更新: id={}", id);

        // 更新後のデータをレスポンス DTO に変換して返す
        return toResponse(userMapper.findById(id));
    }

    /**
     * User エンティティを UserResponse DTO に変換する（内部処理用）
     *
     * <p>パスワードハッシュは含めない。</p>
     *
     * @param user 変換元 User エンティティ
     * @return UserResponse DTO
     */
    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());                   // ユーザーID
        r.setUsername(user.getUsername());       // ユーザー名
        r.setDisplayName(user.getDisplayName()); // 表示名
        r.setRole(user.getRole());               // ロール
        r.setEmail(user.getEmail());             // メールアドレス
        r.setIsActive(user.getIsActive());       // 有効フラグ
        r.setLastLoginAt(user.getLastLoginAt()); // 最終ログイン日時
        r.setCreatedAt(user.getCreatedAt());     // 作成日時
        r.setUpdatedAt(user.getUpdatedAt());     // 更新日時
        return r;
    }
}
