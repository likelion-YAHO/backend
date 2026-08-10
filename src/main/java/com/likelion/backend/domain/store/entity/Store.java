package com.likelion.backend.domain.store.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 예: MCM 롯데면세점 명동본점 */
  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(length = 50)
  private String phone;
}
