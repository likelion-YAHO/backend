package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.dto.DesignAnalysisResponse;
import com.likelion.backend.domain.product.dto.ProductResponse;
import com.likelion.backend.domain.product.entity.DesignOption;
import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductCategory;
import com.likelion.backend.domain.product.entity.ProductImage;
import com.likelion.backend.domain.product.repository.DesignOptionRepository;
import com.likelion.backend.domain.product.repository.ProductImageRepository;
import com.likelion.backend.domain.product.repository.ProductRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import com.likelion.backend.global.storage.FileStorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private static final int MAX_IMAGE_COUNT = 5;
  private static final String PRODUCT_IMAGE_DIR = "products";

  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final DesignOptionRepository designOptionRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;
  private final ProductConditionAnalyzer productConditionAnalyzer;
  private final ProductDesignRecommender productDesignRecommender;

  /**
   * 제품 사진 + 카테고리 등록과 AI 상태 분석을 한 요청으로 처리
   */
  @Transactional
  public ProductResponse createProduct(Long userId, String categoryValue, List<MultipartFile> images) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

    ProductCategory category = parseCategory(categoryValue);
    List<MultipartFile> validImages = validateImages(images);

    ConditionAnalysisResult analysis =
        productConditionAnalyzer.analyze(validImages, category);

    List<String> uploadedUrls = new ArrayList<>();
    try {
      for (MultipartFile image : validImages) {
        uploadedUrls.add(fileStorageService.upload(image, PRODUCT_IMAGE_DIR));
      }

      Product product = Product.builder()
          .user(user)
          .category(category)
          .aiCondition(analysis.getCondition())
          .upcyclable(analysis.isUpcyclable())
          .recyclableParts(analysis.getRecyclableParts())
          .sizeHint(analysis.getSizeHint())
          .userPrompt(null)
          .build();
      Product saved = productRepository.save(product);

      List<ProductImage> productImages = new ArrayList<>();
      for (int i = 0; i < uploadedUrls.size(); i++) {
        ProductImage productImage = ProductImage.builder()
            .product(saved)
            .imageUrl(uploadedUrls.get(i))
            .sortOrder(i)
            .build();
        productImages.add(productImageRepository.save(productImage));
      }

      return ProductResponse.of(saved, productImages, analysis.getMessage());
    } catch (RuntimeException e) {
      // DB 저장 실패 등으로 예외 시 이미 올린 로컬 파일 정리
      uploadedUrls.forEach(fileStorageService::deleteByUrl);
      throw e;
    }
  }

  public List<ProductResponse> getMyProducts(Long userId) {
    return productRepository.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
        .map(product -> {
          List<ProductImage> images =
              productImageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
          List<DesignOption> options =
              designOptionRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
          return ProductResponse.of(product, images, options);
        })
        .toList();
  }

  public ProductResponse getProduct(Long userId, Long productId) {
    Product product = getOwnedProduct(userId, productId);
    List<ProductImage> images =
        productImageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
    List<DesignOption> options =
        designOptionRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
    return ProductResponse.of(product, images, options);
  }

  /**
   * 디자인 가이드 프롬프트를 저장하고 AI 시안 추천을 생성한다.
   * 재호출 시 기존 시안은 교체된다.
   */
  @Transactional
  public DesignAnalysisResponse analyzeDesign(Long userId, Long productId, String userPrompt) {
    if (!StringUtils.hasText(userPrompt)) {
      throw new CustomException(GlobalErrorCode.DESIGN_PROMPT_REQUIRED);
    }
    String prompt = userPrompt.trim();
    if (prompt.length() > 255) {
      throw new CustomException(GlobalErrorCode.DESIGN_PROMPT_REQUIRED);
    }

    Product product = getOwnedProduct(userId, productId);
    if (!product.isUpcyclable()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_NOT_UPCYCLABLE);
    }

    List<ProductImage> images =
        productImageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
    if (images.isEmpty()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_REQUIRED);
    }

    List<DesignRecommendation> recommendations =
        productDesignRecommender.recommend(product, images, prompt);

    product.updateUserPrompt(prompt);
    deleteExistingDesignOptions(product.getId(), images);

    String fallbackImageUrl = images.get(0).getImageUrl();
    List<DesignOption> savedOptions = new ArrayList<>();
    for (int i = 0; i < recommendations.size(); i++) {
      DesignRecommendation recommendation = recommendations.get(i);
      String imageUrl =
          StringUtils.hasText(recommendation.getImageUrl())
              ? recommendation.getImageUrl()
              : fallbackImageUrl;
      DesignOption option =
          DesignOption.builder()
              .product(product)
              .name(recommendation.getName())
              .description(recommendation.getDescription())
              .imageUrl(imageUrl)
              .sortOrder(i)
              .build();
      savedOptions.add(designOptionRepository.save(option));
    }

    return DesignAnalysisResponse.of(product, savedOptions);
  }

  @Transactional
  public void deleteProduct(Long userId, Long productId) {
    Product product = getOwnedProduct(userId, productId);
    List<ProductImage> images =
        productImageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());

    deleteExistingDesignOptions(product.getId(), images);
    images.forEach(image -> fileStorageService.deleteByUrl(image.getImageUrl()));
    productImageRepository.deleteAll(images);
    productRepository.delete(product);
  }

  /** 기존 시안 DB 삭제 + 생성 이미지 파일 정리(제품 원본 URL은 삭제하지 않음) */
  private void deleteExistingDesignOptions(Long productId, List<ProductImage> productImages) {
    List<DesignOption> existing =
        designOptionRepository.findAllByProductIdOrderBySortOrderAsc(productId);
    Set<String> productImageUrls =
        productImages.stream()
            .map(ProductImage::getImageUrl)
            .filter(StringUtils::hasText)
            .collect(Collectors.toSet());

    for (DesignOption option : existing) {
      String url = option.getImageUrl();
      if (StringUtils.hasText(url) && !productImageUrls.contains(url)) {
        fileStorageService.deleteByUrl(url);
      }
    }
    designOptionRepository.deleteAllByProductId(productId);
  }

  private Product getOwnedProduct(Long userId, Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.PRODUCT_NOT_FOUND));
    if (!product.getUser().getId().equals(userId)) {
      throw new CustomException(GlobalErrorCode.PRODUCT_ACCESS_DENIED);
    }
    return product;
  }

  private ProductCategory parseCategory(String categoryValue) {
    if (!StringUtils.hasText(categoryValue)) {
      throw new CustomException(GlobalErrorCode.INVALID_PRODUCT_CATEGORY);
    }
    try {
      return ProductCategory.fromValue(categoryValue);
    } catch (IllegalArgumentException e) {
      throw new CustomException(GlobalErrorCode.INVALID_PRODUCT_CATEGORY);
    }
  }

  private List<MultipartFile> validateImages(List<MultipartFile> images) {
    if (CollectionUtils.isEmpty(images)) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_REQUIRED);
    }
    List<MultipartFile> nonEmpty = images.stream()
        .filter(file -> file != null && !file.isEmpty())
        .toList();
    if (nonEmpty.isEmpty()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_REQUIRED);
    }
    if (nonEmpty.size() > MAX_IMAGE_COUNT) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);
    }
    return nonEmpty;
  }
}
