package com.likelion.backend.domain.user.service;

import com.likelion.backend.domain.user.dto.UserMeResponse;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public UserMeResponse getMe(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
    return UserMeResponse.from(user);
  }
}
