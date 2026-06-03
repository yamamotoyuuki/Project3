package com.company.pcmgmt.domain.mapper.user;

import com.company.pcmgmt.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * システムユーザー MyBatis マッパーインターフェース
 *
 * <p>users テーブルに対する CRUD クエリを定義する。
 * 認証処理・ユーザー管理 API の両方から使用される。
 * 実装は {@code resources/mapper/UserMapper.xml} に記述する。</p>
 */
@Mapper
public interface UserMapper {

    /**
     * ユーザー名でユーザーを取得する（認証処理用）
     *
     * @param username ログインユーザー名
     * @return User エンティティ（存在しない場合は null）
     */
    User findByUsername(@Param("username") String username);

    /**
     * 指定IDのユーザーを取得する
     *
     * @param id ユーザーID
     * @return User エンティティ（存在しない場合は null）
     */
    User findById(@Param("id") Long id);

    /**
     * 全ユーザーを取得する（ユーザー管理画面用）
     *
     * @return User エンティティのリスト
     */
    java.util.List<User> findAll();

    /**
     * 最終ログイン日時を現在日時に更新する
     *
     * <p>ログイン成功時に呼び出される。</p>
     *
     * @param id 対象ユーザーのID
     */
    void updateLastLoginAt(@Param("id") Long id);

    /**
     * ユーザーを新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code user.id} にセットされる。</p>
     *
     * @param user 登録するユーザーエンティティ
     */
    void insert(User user);

    /**
     * ユーザー情報を更新する
     *
     * @param user 更新するユーザーエンティティ（id フィールドが必須）
     */
    void update(User user);

    /**
     * ユーザー名の重複チェックを行う
     *
     * @param username  チェック対象のユーザー名
     * @param excludeId 更新時に自分自身を除外するためのID（新規登録時は null）
     * @return 重複している場合は true
     */
    boolean existsByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);
}
