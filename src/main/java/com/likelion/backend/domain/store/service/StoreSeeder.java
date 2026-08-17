package com.likelion.backend.domain.store.service;

import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class StoreSeeder implements ApplicationRunner {

  private static final List<SeedItem> SEEDS =
      List.of(
          new SeedItem(
              "MCM 롯데백화점 본점",
              "서울 중구 남대문로 81 롯데백화점본점 1층",
              "02-772-3198",
              37.5647,
              126.9816),
          new SeedItem(
              "MCM 신세계면세점 본점",
              "서울 중구 퇴계로 77 9F 신세계면세점 본점",
              "02-6370-4084",
              37.5603,
              126.9810),
          new SeedItem(
              "MCM 롯데면세점 명동본점",
              "서울 중구 을지로 30",
              "02-759-6681",
              37.5653,
              126.9810),
          new SeedItem(
              "MCM KUNSTHALLE",
              "서울 강남구 언주로 734 MCM빌딩 1층",
              "02-511-0234",
              37.5226,
              127.0343));

  private final StoreRepository storeRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (storeRepository.count() > 0) {
      return;
    }

    log.info("예약 매장 시드 데이터를 생성합니다.");
    storeRepository.saveAll(
        SEEDS.stream()
            .map(
                s ->
                    Store.builder()
                        .name(s.name())
                        .address(s.address())
                        .phone(s.phone())
                        .latitude(s.latitude())
                        .longitude(s.longitude())
                        .build())
            .toList());
  }

  private record SeedItem(
      String name, String address, String phone, double latitude, double longitude) {}
}
