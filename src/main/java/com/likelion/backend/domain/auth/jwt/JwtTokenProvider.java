package com.likelion.backend.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String TOKEN_TYPE_ACCESS = "access";
  public static final String TOKEN_TYPE_REFRESH = "refresh";
  private static final String CLAIM_TOKEN_TYPE = "tokenType";
  private static final String CLAIM_EMAIL = "email";

  private final SecretKey secretKey;
  private final long accessTokenValidityMs;
  private final long refreshTokenValidityMs;

  public JwtTokenProvider(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-token-validity}") long accessTokenValidityMs,
      @Value("${app.jwt.refresh-token-validity}") long refreshTokenValidityMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenValidityMs = accessTokenValidityMs;
    this.refreshTokenValidityMs = refreshTokenValidityMs;
  }

  public String createAccessToken(Long userId, String email) {
    return createToken(userId, email, TOKEN_TYPE_ACCESS, accessTokenValidityMs);
  }

  public String createRefreshToken(Long userId, String email) {
    return createToken(userId, email, TOKEN_TYPE_REFRESH, refreshTokenValidityMs);
  }

  public boolean validateAccessToken(String token) {
    return validateTokenType(token, TOKEN_TYPE_ACCESS);
  }

  public boolean validateRefreshToken(String token) {
    return validateTokenType(token, TOKEN_TYPE_REFRESH);
  }

  public Long getUserId(String token) {
    return Long.valueOf(parseClaims(token).getSubject());
  }

  public String getEmail(String token) {
    return parseClaims(token).get(CLAIM_EMAIL, String.class);
  }

  public long getAccessTokenValiditySeconds() {
    return accessTokenValidityMs / 1000;
  }

  public long getRefreshTokenValidityMs() {
    return refreshTokenValidityMs;
  }

  private String createToken(Long userId, String email, String tokenType, long validityMs) {
    Date now = new Date();
    Date expiresAt = new Date(now.getTime() + validityMs);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(userId))
        .claim(CLAIM_EMAIL, email)
        .claim(CLAIM_TOKEN_TYPE, tokenType)
        .issuedAt(now)
        .expiration(expiresAt)
        .signWith(secretKey)
        .compact();
  }

  private boolean validateTokenType(String token, String expectedType) {
    try {
      Claims claims = parseClaims(token);
      return expectedType.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
