package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import com.likelion.backend.domain.lab.repository.LabDesignRepository;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import com.likelion.backend.domain.user.entity.Provider;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
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
@Order(4)
@RequiredArgsConstructor
public class LabGallerySeeder implements ApplicationRunner {

  /** 새 컷: src/main/resources/static/lab-gallery/{file} */
  private static final List<GallerySeed> SEEDS =
      List.of(
          new GallerySeed(
              "lab.gallery.skulikelion@local",
              "lab-gallery-skulikelion",
              "SKUlikelion",
              BaseProduct.ELLA_BOSTON_BAG,
              "Boho Boston",
              "/mcmlab_SKUlikelion.png",
              165),
          new GallerySeed(
              "lab.gallery.babylon@local",
              "lab-gallery-babylon",
              "Babyllion",
              BaseProduct.STARK_SIDE_STUDS_BACKPACK,
              "Boho Backpack",
              "/mcmlab_Babylon.png",
              133),
          new GallerySeed(
              "lab.gallery.sjftrack@local",
              "lab-gallery-sjftrack",
              "SJFTRACK",
              BaseProduct.PINA_TAMBOURINE_BAG,
              "Boho Tambourine",
              "/mcmlab_SJFTRACK.png",
              93),
          new GallerySeed(
              "lab.gallery.mcmupcycling@local",
              "lab-gallery-mcmupcycling",
              "MCMupcycling",
              BaseProduct.TRACY_SATCHEL,
              "Boho Satchel",
              "/lab-gallery/mcmupcycling.png",
              68),
          new GallerySeed(
              "lab.gallery.sdkflfl.shopper@local",
              "lab-gallery-sdkflfl-shopper",
              "sdkflfl",
              BaseProduct.TONI_TOP_ZIP_SHOPPER,
              "Boho Shopper",
              "/lab-gallery/sdkflfl-shopper.png",
              35),
          new GallerySeed(
              "lab.gallery.sdkflfl.satchel@local",
              "lab-gallery-sdkflfl-satchel",
              "sdkflfl",
              BaseProduct.TRACY_SATCHEL,
              "Western Satchel",
              "/lab-gallery/sdkflfl-satchel.png",
              21));

  private final LabDesignRepository labDesignRepository;
  private final LabMissionRepository labMissionRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    LabMission mission =
        labMissionRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc().orElse(null);
    if (mission == null) {
      log.warn("활성 랩 미션이 없어 갤러리 시드를 생성하지 않습니다.");
      return;
    }

    int created = 0;
    for (GallerySeed seed : SEEDS) {
      User creator = findOrCreateCreator(seed);
      if (!seed.nickname().equals(creator.getNickname())) {
        creator.updateNickname(seed.nickname());
      }

      LabDesign design =
          labDesignRepository
              .findFirstByUser_IdAndProductionStatus(creator.getId(), ProductionStatus.VIRTUAL)
              .orElse(null);
      if (design == null) {
        design =
            LabDesign.builder()
                .user(creator)
                .mission(mission)
                .baseProduct(seed.baseProduct())
                .designName(seed.designName())
                .concept("BOHO CHIC")
                .aiPrompt("BOHO CHIC " + seed.baseProduct().getProductName())
                .usedMaterials("Vintage Visetos, Suede, Pink Leather")
                .imageUrl(seed.imageUrl())
                .build();
        design.initLikesCount(seed.likesCount());
        labDesignRepository.save(design);
        created++;
      } else {
        design.syncGallerySeed(
            seed.baseProduct(), seed.designName(), seed.imageUrl(), seed.likesCount());
      }
    }

    if (created > 0) {
      log.info("랩 갤러리 시드 출품작 {}건을 생성했습니다.", created);
    }
  }

  private User findOrCreateCreator(GallerySeed seed) {
    return userRepository
        .findByEmail(seed.email())
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .email(seed.email())
                        .nickname(seed.nickname())
                        .provider(Provider.GOOGLE)
                        .socialId(seed.socialId())
                        .build()));
  }

  private record GallerySeed(
      String email,
      String socialId,
      String nickname,
      BaseProduct baseProduct,
      String designName,
      String imageUrl,
      int likesCount) {}
}
