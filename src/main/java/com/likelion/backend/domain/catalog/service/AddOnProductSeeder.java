package com.likelion.backend.domain.catalog.service;

import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.repository.AddOnProductRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddOnProductSeeder implements ApplicationRunner {

  /**
   * src/main/resources/static/ 아래 파일
   * 브라우저 접근: http://localhost:8080/keyring-1.png
   */
  private static final List<SeedItem> SEEDS =
      List.of(
          new SeedItem("가죽 갈색 참", AddOnCategory.KEYRING, 270000, 0, "/keyring-1.png"),
          new SeedItem("가죽 검정 참", AddOnCategory.KEYRING, 270000, 1, "/keyring-2.png"),
          new SeedItem("베어 키링", AddOnCategory.KEYRING, 270000, 2, "/keyring-3.png"),
          new SeedItem("래빗 키링", AddOnCategory.KEYRING, 270000, 3, "/keyring-4.png"),
          new SeedItem("레드 모노그램 스카프", AddOnCategory.SCARF, 175000, 0, "/scarf-1.png"),
          new SeedItem("블루 모노그램 스카프", AddOnCategory.SCARF, 175000, 1, "/scarf-2.png"),
          new SeedItem("노랑 모노그램 스카프", AddOnCategory.SCARF, 175000, 2, "/scarf-4.png"),
          new SeedItem("혼합 모노그램 스카프", AddOnCategory.SCARF, 175000, 3, "/scarf-3.png")
          );

  private final AddOnProductRepository addOnProductRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (addOnProductRepository.countByActiveTrue() == 0) {
      log.info("추가상품 시드 데이터를 생성합니다. (static 이미지 포함)");
      List<AddOnProduct> products =
          SEEDS.stream()
              .map(
                  s ->
                      AddOnProduct.builder()
                          .name(s.name())
                          .category(s.category())
                          .price(s.price())
                          .imageUrl(s.imageUrl())
                          .active(true)
                          .sortOrder(s.sortOrder())
                          .build())
              .toList();
      addOnProductRepository.saveAll(products);
      return;
    }

    // 이미 시드된 경우 imageUrl 만 보강
    Map<String, String> imageByName = new LinkedHashMap<>();
    SEEDS.forEach(s -> imageByName.put(s.name(), s.imageUrl()));

    List<AddOnProduct> existing = addOnProductRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc();
    int updated = 0;
    for (AddOnProduct product : existing) {
      String expected = imageByName.get(product.getName());
      if (expected != null && !expected.equals(product.getImageUrl())) {
        product.updateImageUrl(expected);
        updated++;
      } else if (expected != null && !StringUtils.hasText(product.getImageUrl())) {
        product.updateImageUrl(expected);
        updated++;
      }
    }
    if (updated > 0) {
      log.info("추가상품 imageUrl {}건을 static 경로로 갱신했습니다.", updated);
    }
  }

  private record SeedItem(
      String name, AddOnCategory category, int price, int sortOrder, String imageUrl) {}
}
