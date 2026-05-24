package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.UserCreateRequest;
import com.company.pcmgmt.api.dto.request.UserUpdateRequest;
import com.company.pcmgmt.api.dto.response.UserResponse;
import com.company.pcmgmt.domain.entity.User;
import com.company.pcmgmt.domain.mapper.UserMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userMapper.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) throw new ResourceNotFoundException("ユーザーが見つかりません: id=" + id);
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        if (userMapper.existsByUsername(req.getUsername(), null)) {
            throw new IllegalArgumentException("ユーザー名 [" + req.getUsername() + "] は既に使用されています");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName());
        user.setRole(req.getRole());
        user.setEmail(req.getEmail());
        user.setIsActive(true);

        userMapper.insert(user);
        log.info("ユーザー登録: username={}", user.getUsername());
        return toResponse(userMapper.findById(user.getId()));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        User user = userMapper.findById(id);
        if (user == null) throw new ResourceNotFoundException("ユーザーが見つかりません: id=" + id);

        user.setDisplayName(req.getDisplayName());
        user.setRole(req.getRole());
        user.setEmail(req.getEmail());
        if (req.getIsActive() != null) user.setIsActive(req.getIsActive());
        if (StringUtils.hasText(req.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userMapper.update(user);
        log.info("ユーザー更新: id={}", id);
        return toResponse(userMapper.findById(id));
    }

    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setDisplayName(user.getDisplayName());
        r.setRole(user.getRole());
        r.setEmail(user.getEmail());
        r.setIsActive(user.getIsActive());
        r.setLastLoginAt(user.getLastLoginAt());
        r.setCreatedAt(user.getCreatedAt());
        r.setUpdatedAt(user.getUpdatedAt());
        return r;
    }
}
