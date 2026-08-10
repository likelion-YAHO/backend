package com.likelion.backend.domain.user.repository;

import com.likelion.backend.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findBySocialId(String socialId);

  boolean existsByEmail(String email);
}
