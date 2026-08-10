package com.likelion.backend.domain.user.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 로그인 ID, unique */
  @Column(nullable = false, length = 100, unique = true)
  private String email;

  /** social 로그인 시 NULL */
  @Column(length = 255)
  private String password;

  /** 유저 이름 (예: 김서경 님) */
  @Column(nullable = false, length = 50)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Provider provider;

  @Column(name = "profile_image", length = 255)
  private String profileImage;

  @Column(length = 255)
  private String phone;

  /** 구글 소셜 고유 ID, unique */
  @Column(name = "social_id", length = 255, unique = true)
  private String socialId;

  @Builder.Default
  @Column(name = "alarm_enabled", nullable = false)
  private boolean alarmEnabled = true;
}
