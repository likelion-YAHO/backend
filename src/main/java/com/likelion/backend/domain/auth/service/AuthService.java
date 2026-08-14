package com.likelion.backend.domain.auth.service;

import com.likelion.backend.domain.auth.dto.AuthResponse;
import com.likelion.backend.domain.auth.dto.LoginRequest;
import com.likelion.backend.domain.auth.dto.LogoutRequest;
import com.likelion.backend.domain.auth.dto.SignupRequest;
import com.likelion.backend.domain.auth.dto.TokenRefreshRequest;
import com.likelion.backend.domain.auth.entity.RefreshToken;
import com.likelion.backend.domain.auth.jwt.JwtTokenProvider;
import com.likelion.backend.domain.auth.repository.RefreshTokenRepository;
import com.likelion.backend.domain.user.entity.Provider;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public AuthResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(GlobalErrorCode.EMAIL_ALREADY_EXISTS);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .phone(request.getPhone())
        .provider(Provider.LOCAL)
        .build();

    User saved = userRepository.save(user);
    return issueTokens(saved);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(GlobalErrorCode.INVALID_CREDENTIALS));

    if (user.getPassword() == null
        || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException(GlobalErrorCode.INVALID_CREDENTIALS);
    }

    return issueTokens(user);
  }

  /**
   * refresh token 검증 후 access/refresh 재발급
   */
  @Transactional
  public AuthResponse refresh(TokenRefreshRequest request) {
    String rawRefreshToken = request.getRefreshToken();

    if (!jwtTokenProvider.validateRefreshToken(rawRefreshToken)) {
      throw new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
    }

    RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN));

    if (stored.isExpired()) {
      refreshTokenRepository.delete(stored);
      throw new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
    }

    Long userId = jwtTokenProvider.getUserId(rawRefreshToken);
    if (!stored.getUser().getId().equals(userId)) {
      refreshTokenRepository.delete(stored);
      throw new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 기존 refresh 폐기 후 새 토큰 쌍 발급
    refreshTokenRepository.delete(stored);
    return issueTokens(stored.getUser());
  }

  /**
   * 전달받은 refresh token을 삭제 (본인 소유 토큰만)
   */
  @Transactional
  public void logout(Long userId, LogoutRequest request) {
    RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
        .orElseThrow(() -> new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN));

    if (!stored.getUser().getId().equals(userId)) {
      throw new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
    }

    refreshTokenRepository.delete(stored);
  }

  private AuthResponse issueTokens(User user) {
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .token(refreshToken)
            .expiresAt(LocalDateTime.now().plusSeconds(
                jwtTokenProvider.getRefreshTokenValidityMs() / 1000))
            .build());

    return AuthResponse.of(
        accessToken,
        refreshToken,
        jwtTokenProvider.getAccessTokenValiditySeconds(),
        user);
  }
}
