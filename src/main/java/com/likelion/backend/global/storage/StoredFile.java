package com.likelion.backend.global.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {

  private final byte[] bytes;
  private final String contentType;
}
