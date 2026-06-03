package com.company.pcmgmt.security;

import com.company.pcmgmt.domain.entity.User;
import com.company.pcmgmt.domain.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security ユーザー詳細サービス実装
 *
 * <p>Spring Security の認証処理で使用するユーザー情報をデータベースから取得する。
 * {@link UserDetailsService} インターフェースを実装し、Spring Security のフレームワークに組み込む。</p>
 *
 * <p>また、ログインユーザーのIDを取得するために独自メソッド {@link #loadUserEntityByUsername(String)} も提供する。</p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /** ユーザー情報の DB アクセスを担うマッパー */
    private final UserMapper userMapper;

    /**
     * Spring Security の認証処理で使用するユーザー詳細を取得する
     *
     * <p>ユーザー名でDBを検索し、Spring Security が要求する {@link UserDetails} オブジェクトを返す。
     * ロールは "ROLE_{role}" 形式のGrantedAuthorityとして設定される。</p>
     *
     * @param username ログインユーザー名
     * @return Spring Security の UserDetails オブジェクト
     * @throws UsernameNotFoundException 指定ユーザー名のユーザーが存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ユーザー名でDBからユーザーを検索
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        // Spring Security が使用する UserDetails オブジェクトを構築して返す
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())       // ログインユーザー名
                .password(user.getPasswordHash())   // BCrypt ハッシュ化済みパスワード
                // ロールを "ROLE_" プレフィックス付きの権限として設定（例: "ROLE_ADMIN"）
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .build();
    }

    /**
     * ユーザーエンティティをユーザー名で取得する（ログインユーザーID取得用）
     *
     * <p>Spring Security の UserDetails にはユーザーIDが含まれないため、
     * 貸出登録時の createdBy 設定など、ID が必要な場合にこのメソッドを使用する。</p>
     *
     * @param username ユーザー名
     * @return User エンティティ（存在しない場合は null）
     */
    public User loadUserEntityByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}
