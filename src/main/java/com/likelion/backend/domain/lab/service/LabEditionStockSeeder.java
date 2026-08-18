package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import com.likelion.backend.domain.lab.repository.LabDesignRepository;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.store.entity.StoreStock;
import com.likelion.backend.domain.store.repository.StoreRepository;
import com.likelion.backend.domain.store.repository.StoreStockRepository;
import com.likelion.backend.domain.user.entity.Provider;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class LabEditionStockSeeder implements ApplicationRunner {

  private static final String SEED_EMAIL = "lab.edition.seed@local";
  private static final Map<String, Integer> STOCK_BY_STORE_NAME =
      Map.of(
          "MCM 롯데백화점 본점", 2,
          "MCM 신세계면세점 본점", 3,
          "MCM 롯데면세점 명동본점", 0,
          "MCM KUNSTHALLE", 0);

  private final StoreStockRepository storeStockRepository;
  private final StoreRepository storeRepository;
  private final LabDesignRepository labDesignRepository;
  private final LabMissionRepository labMissionRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    List<Store> stores = storeRepository.findAll();
    if (stores.isEmpty()) {
      log.warn("매장 시드가 없어 랩 에디션 재고를 생성하지 않습니다.");
      return;
    }

    LabMission mission =
        labMissionRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc().orElse(null);
    if (mission == null) {
      log.warn("활성 랩 미션이 없어 랩 에디션 재고를 생성하지 않습니다.");
      return;
    }

    User creator = findOrCreateCreator();
    int createdEditions = 0;
    int createdStocks = 0;

    for (LabEditionCatalog.Item seed : LabEditionCatalog.ITEMS) {
      LabDesign edition =
          labDesignRepository
              .findFirstByDesignNameAndProductionStatusNot(
                  seed.designName(), ProductionStatus.VIRTUAL)
              .orElse(null);
      if (edition == null) {
        edition = createEdition(mission, creator, seed);
        createdEditions++;
      } else {
        edition.syncReadyEditionCatalog(
            seed.primaryImageUrl(),
            seed.concept(),
            seed.color(),
            seed.size(),
            seed.stock(),
            seed.price());
      }

      if (!storeStockRepository.existsByLabDesignId(edition.getId())) {
        storeStockRepository.saveAll(buildStocks(stores, edition));
        createdStocks += stores.size();
      }
    }

    if (createdEditions > 0 || createdStocks > 0) {
      log.info("랩 에디션 시드 에디션 {}건, 매장 재고 {}건을 생성했습니다.", createdEditions, createdStocks);
    }
  }

  private User findOrCreateCreator() {
    return userRepository
        .findByEmail(SEED_EMAIL)
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .email(SEED_EMAIL)
                        .nickname("Lab Edition")
                        .provider(Provider.GOOGLE)
                        .socialId("lab-edition-seed")
                        .build()));
  }

  private LabDesign createEdition(LabMission mission, User creator, LabEditionCatalog.Item seed) {
    LabDesign design =
        LabDesign.builder()
            .user(creator)
            .mission(mission)
            .baseProduct(seed.baseProduct())
            .designName(seed.designName())
            .concept(seed.concept())
            .aiPrompt(seed.concept() + " " + seed.baseProduct().getProductName())
            .usedMaterials("Vintage Visetos, Suede, Nappa Leather")
            .imageUrl(seed.primaryImageUrl())
            .color(seed.color())
            .size(seed.size())
            .stock(seed.stock())
            .build();
    design.markAsReadyEdition(seed.price());
    return labDesignRepository.save(design);
  }

  private List<StoreStock> buildStocks(List<Store> stores, LabDesign edition) {
    return stores.stream()
        .map(
            store ->
                StoreStock.builder()
                    .store(store)
                    .labDesign(edition)
                    .stockCount(STOCK_BY_STORE_NAME.getOrDefault(store.getName(), 0))
                    .build())
        .toList();
  }

}
