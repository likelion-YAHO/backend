package com.likelion.backend.domain.user.service;

import com.likelion.backend.domain.user.dto.UpdateAlarmRequest;
import com.likelion.backend.domain.user.dto.UpdateProfileRequest;
import com.likelion.backend.domain.user.dto.UserMeResponse;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import com.likelion.backend.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private static final String PROFILE_IMAGE_DIR = "profiles";

  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;

  public UserMeResponse getMe(Long userId) {
    return UserMeResponse.from(findUser(userId));
  }

  @Transactional
  public UserMeResponse updateMe(Long userId, UpdateProfileRequest request) {
    User user = findUser(userId);

    if (request.getNickname() != null) {
      String nickname = request.getNickname().trim();
      if (!StringUtils.hasText(nickname)) {
        throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
      }
      user.updateNickname(nickname);
    }

    if (request.getEmail() != null) {
      String email = request.getEmail().trim();
      if (!StringUtils.hasText(email)) {
        throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
      }
      if (!email.equals(user.getEmail())
          && userRepository.existsByEmailAndIdNot(email, userId)) {
        throw new CustomException(GlobalErrorCode.EMAIL_ALREADY_EXISTS);
      }
      user.updateEmail(email);
    }

    if (request.getPhone() != null) {
      String phone = request.getPhone().trim();
      user.updatePhone(StringUtils.hasText(phone) ? phone : null);
    }

    return UserMeResponse.from(user);
  }

  @Transactional
  public UserMeResponse updateProfileImage(Long userId, MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new CustomException(GlobalErrorCode.PROFILE_IMAGE_REQUIRED);
    }

    User user = findUser(userId);
    String uploadedUrl = fileStorageService.upload(image, PROFILE_IMAGE_DIR);
    String previousUrl = user.getProfileImage();
    try {
      user.updateProfileImage(uploadedUrl);
    } catch (RuntimeException e) {
      fileStorageService.deleteByUrl(uploadedUrl);
      throw e;
    }
    if (StringUtils.hasText(previousUrl)) {
      fileStorageService.deleteByUrl(previousUrl);
    }
    return UserMeResponse.from(user);
  }

  @Transactional
  public UserMeResponse updateAlarm(Long userId, UpdateAlarmRequest request) {
    User user = findUser(userId);
    user.updateAlarmEnabled(request.getAlarmEnabled());
    return UserMeResponse.from(user);
  }

  private User findUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
  }
}
