package com.likelion.backend.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장 (로컬)
 */
public interface FileStorageService {

  /**
   * 파일을 저장하고 클라이언트가 접근 가능한 URL을 반환
   *
   * @param file 업로드 파일
   * @param directory 논리 디렉터리 (예: products)
   * @return 공개 URL (예: http://localhost:8080/uploads/products/uuid.jpg)
   */
  String upload(MultipartFile file, String directory);

  /**
   * 저장된 파일 URL에 해당하는 로컬 파일을 삭제 (없으면 무시)
   */
  void deleteByUrl(String fileUrl);

  /**
   * 저장된 파일 URL에서 바이트를 읽음 (AI Vision 재분석용)
   */
  StoredFile readByUrl(String fileUrl);

  /**
   * 바이트 배열을 파일로 저장하고 공개 URL을 반환 (AI 생성 이미지 등)
   */
  String uploadBytes(byte[] bytes, String directory, String extension, String contentType);
}
