package com.company.pcmgmt.api.controller.user;

import com.company.pcmgmt.api.dto.request.user.UserCreateRequest;
import com.company.pcmgmt.api.dto.request.user.UserUpdateRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.user.UserResponse;
import com.company.pcmgmt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * システムユーザー管理コントローラー
 *
 * <p>システムにログインするユーザーの CRUD 操作エンドポイントを提供する。
 * {@code @PreAuthorize("hasRole('ADMIN')")} により管理者（ADMIN ロール）のみがアクセス可能。</p>
 *
 * <p>ベースパス: {@code /api/v1/users}</p>
 */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")  // 管理者ロールのみアクセス許可
@RequiredArgsConstructor
public class UserController {

    /** ユーザー管理ビジネスロジックを担うサービス */
    private final UserService userService;

    /**
     * 全システムユーザーを取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/users}</p>
     * <p>認証: JWT 認証必須 + ADMIN ロール必須</p>
     *
     * @return ユーザーレスポンスのリスト（パスワードハッシュは含まない）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll()));
    }

    /**
     * 指定IDのユーザー詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/users/{id}}</p>
     * <p>認証: JWT 認証必須 + ADMIN ロール必須</p>
     *
     * @param id ユーザーID（パスパラメータ）
     * @return ユーザーレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    /**
     * システムユーザーを新規登録する
     *
     * <p>ユーザー名が重複する場合は HTTP 400 Bad Request が返される。
     * パスワードはサービス層で BCrypt ハッシュ化される。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/users}</p>
     * <p>認証: JWT 認証必須 + ADMIN ロール必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後のユーザーレスポンス
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest req) {
        // ユーザーを登録してレスポンスを取得
        UserResponse created = userService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ユーザーを登録しました", created));
    }

    /**
     * ユーザー情報を更新する
     *
     * <p>パスワード変更・ロール変更・アカウント停止もこのエンドポイントで行う。</p>
     *
     * <p>エンドポイント: {@code PUT /api/v1/users/{id}}</p>
     * <p>認証: JWT 認証必須 + ADMIN ロール必須</p>
     *
     * @param id  更新対象のユーザーID（パスパラメータ）
     * @param req 更新リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後のユーザーレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("ユーザーを更新しました", userService.update(id, req)));
    }
}
