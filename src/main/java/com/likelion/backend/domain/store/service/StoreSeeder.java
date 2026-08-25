package com.likelion.backend.domain.store.service;

import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
              127.0343),
          new SeedItem(
              "MCM 현대백화점 무역센터점",
              "서울 강남구 테헤란로 517 현대백화점 무역센터점 9F",
              "02-2142-6285",
              37.5085,
              127.0596),
          new SeedItem(
              "MCM 현대백화점면세점 무역센터점",
              "서울 강남구 테헤란로 517 현대백화점면세점 8F",
              "1811-6688",
              37.5086,
              127.0597),
          new SeedItem(
              "MCM 스타필드 코엑스몰",
              "서울 강남구 영동대로 513 스타필드 코엑스몰",
              "1833-9001",
              37.5125,
              127.0588),
          new SeedItem(
              "MCM 파르나스몰",
              "서울 강남구 테헤란로 521 파르나스몰",
              "02-559-7089",
              37.5094,
              127.0613),
          new SeedItem(
              "MCM HAUS",
              "서울 강남구 압구정로 412 MCM HAUS 1F",
              "02-540-1404",
              37.5272,
              127.0419),
          new SeedItem(
              "MCM 갤러리아 명품관",
              "서울 강남구 압구정로 343 갤러리아 명품관 WEST",
              "1544-6600",
              37.5283,
              127.0401),
          new SeedItem(
              "MCM 현대백화점 압구정본점",
              "서울 강남구 압구정로 165 현대백화점 압구정본점",
              "1588-3650",
              37.5274,
              127.0275),
          new SeedItem(
              "MCM 롯데백화점 잠실점",
              "서울 송파구 올림픽로 240 롯데백화점 잠실점",
              "1577-0001",
              37.5121,
              127.0994),
          new SeedItem(
              "MCM 롯데백화점 강남점",
              "서울 강남구 도곡로 401 롯데백화점 강남점",
              "1577-0001",
              37.4970,
              127.0533),
          new SeedItem(
              "MCM 롯데월드몰",
              "서울 송파구 올림픽로 300 롯데월드몰",
              "02-3213-5000",
              37.5137,
              127.1042));

  private final StoreRepository storeRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Set<String> existingNames =
        storeRepository.findAll().stream().map(Store::getName).collect(Collectors.toSet());

    List<Store> toSave =
        SEEDS.stream()
            .filter(s -> !existingNames.contains(s.name()))
            .map(
                s ->
                    Store.builder()
                        .name(s.name())
                        .address(s.address())
                        .phone(s.phone())
                        .latitude(s.latitude())
                        .longitude(s.longitude())
                        .build())
            .toList();

    if (toSave.isEmpty()) {
      return;
    }

    storeRepository.saveAll(toSave);
    log.info("예약 매장 시드 {}건을 추가했습니다.", toSave.size());
  }

  private record SeedItem(
      String name, String address, String phone, double latitude, double longitude) {}
}
