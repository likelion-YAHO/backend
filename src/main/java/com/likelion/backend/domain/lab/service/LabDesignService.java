package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.dto.AiDesignRequestDto;
import com.likelion.backend.domain.lab.dto.AiDesignResponseDto;
import com.likelion.backend.domain.lab.dto.LabDesignCreateRequestDto;
import com.likelion.backend.domain.lab.dto.LabDesignDetailResponseDto;
import com.likelion.backend.domain.lab.dto.LabDesignLikeResponseDto;
import com.likelion.backend.domain.lab.dto.LabDesignListResponseDto;
import com.likelion.backend.domain.lab.dto.LabDesignResponseDto;
import com.likelion.backend.domain.lab.dto.LabEditionResponseDto;
import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabAiGenerationAttempt;
import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.LabDesignLike;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import com.likelion.backend.domain.lab.repository.LabAiGenerationAttemptRepository;
import com.likelion.backend.domain.lab.repository.LabDesignLikeRepository;
import com.likelion.backend.domain.lab.repository.LabDesignRepository;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.config.AiProperties;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import com.likelion.backend.global.storage.FileStorageService;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabDesignService {

    private static final int MAX_GENERATION_TRIES = 3;
    private static final String LAB_DESIGN_IMAGE_DIR = "lab-designs";

    private final LabDesignRepository labDesignRepository;
    private final LabMissionRepository labMissionRepository;
    private final UserRepository userRepository;
    private final LabDesignLikeRepository labDesignLikeRepository;
    private final LabAiGenerationAttemptRepository labAiGenerationAttemptRepository;
    private final FileStorageService fileStorageService;
    private final AiProperties aiProperties;

    @Transactional
    public AiDesignResponseDto generateAiDesign(Long userId, AiDesignRequestDto request) {
        if (request.getBaseProduct() == null) {
            throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new CustomException(GlobalErrorCode.AI_NOT_CONFIGURED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        LabMission mission = labMissionRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_MISSION_NOT_FOUND));

        LabAiGenerationAttempt attempt = labAiGenerationAttemptRepository
                .findByUser_IdAndMission_IdAndBaseProduct(
                        userId, mission.getId(), request.getBaseProduct())
                .orElseGet(() ->
                        labAiGenerationAttemptRepository.save(
                                LabAiGenerationAttempt.builder()
                                        .user(user)
                                        .mission(mission)
                                        .baseProduct(request.getBaseProduct())
                                        .build()));

        // 1. 시도 횟수 방어 로직 (3회 초과 시 에러 반환)
        if (attempt.getTryCount() >= MAX_GENERATION_TRIES) {
            throw new CustomException(GlobalErrorCode.AI_GENERATION_LIMIT_EXCEEDED);
        }

        // 2. 진짜 AI 호출 로직
        String imageUrl = generateAndStoreImage(request.getBaseProduct(), request.getPrompt());
        attempt.incrementTryCount();

        // TODO: 추후 Lab 전용 AI 추천 로직 추가 시, 아래 하드코딩된 추천 참/스카프 ID 및 이름을 실제 DB 또는 AI 응답 값으로 연동 필요!
        Long recommendedCharmId = 3L; // 실제 DB에 존재하는 참 ID
        String recommendedCharmName = "베어 키링";
        Long recommendedScarfId = 5L; // 실제 DB에 존재하는 스카프 ID
        String recommendedScarfName = "레드 모노그램 스카프";

        return new AiDesignResponseDto(
                imageUrl,
                attempt.getTryCount(),
                recommendedCharmId,
                recommendedCharmName,
                recommendedScarfId,
                recommendedScarfName,
                List.of() // designOptions (랩 플로우에서는 사용하지 않으므로 빈 리스트 전달)
        );
    }

    @Transactional
    public LabDesignResponseDto createLabDesign(Long userId, LabDesignCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        LabMission mission = labMissionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_MISSION_NOT_FOUND));

        LabDesign labDesign = LabDesign.builder()
                .user(user)
                .mission(mission)
                .baseProduct(request.getBaseProduct())
                .designName(request.getDesignName())
                .concept(request.getConcept())
                .aiPrompt(request.getAiPrompt())
                .usedMaterials(request.getUsedMaterials())
                .imageUrl(request.getImageUrl())
                .pointColor(request.getPointColor())
                .metalColor(request.getMetalColor())
                .charmOptionId(request.getCharmOptionId())
                .scarfOptionId(request.getScarfOptionId())
                .build();

        LabDesign savedDesign = labDesignRepository.save(labDesign);
        if (request.getBaseProduct() != null) {
            labAiGenerationAttemptRepository.deleteByUser_IdAndMission_IdAndBaseProduct(
                    userId, mission.getId(), request.getBaseProduct());
        }
        return new LabDesignResponseDto(savedDesign);
    }

    private String generateAndStoreImage(BaseProduct baseProduct, String prompt) {
        String baseProductName = baseProduct.getProductName();
        String refinedPrompt =
                "A professional studio product photograph of a complete, entire MCM "
                        + baseProductName
                        + ", viewed from a distance so that the entire bag including its bottom, sides, and straps are fully visible with generous negative space around it. "
                        + "Base model: "
                        + baseProductName
                        + ". "
                        + "User customization request: "
                        + prompt
                        + ". "
                        + "Centered composition, clean white studio background, high resolution, photorealistic leather texture.";

        RestTemplate restTemplate = buildRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiProperties.getImageModel());
        requestBody.put("prompt", refinedPrompt);
        requestBody.put("n", 1);
        requestBody.put("size", aiProperties.getImageSize());
        requestBody.put("quality", aiProperties.getImageQuality());

        try {
            String baseUrl = aiProperties.getBaseUrl();
            if (baseUrl != null && baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            baseUrl + "/images/generations",
                            new HttpEntity<>(requestBody, headers),
                            Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (data == null || data.isEmpty()) {
                throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
            }
            Map<String, Object> item = data.get(0);
            byte[] imageBytes;
            if (item.get("b64_json") instanceof String b64 && StringUtils.hasText(b64)) {
                imageBytes = Base64.getDecoder().decode(b64);
            } else if (item.get("url") instanceof String remoteUrl && StringUtils.hasText(remoteUrl)) {
                imageBytes = restTemplate.getForObject(remoteUrl, byte[].class);
                if (imageBytes == null || imageBytes.length == 0) {
                    throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
                }
            } else {
                throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
            }
            // 로컬 파일 저장 로직
            return fileStorageService.uploadBytes(
                    imageBytes, LAB_DESIGN_IMAGE_DIR, ".png", "image/png");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 이미지 생성 실패 상세 에러: ", e);
            throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofMillis(aiProperties.getImageTimeoutMs()));
        return new RestTemplate(requestFactory);
    }

    @Transactional(readOnly = true)
    public List<LabDesignListResponseDto> getGalleryList(String sort) {
        List<LabDesign> designs;

        if ("popular".equalsIgnoreCase(sort)) {
            designs = labDesignRepository.findAllByProductionStatusOrderByLikesCountDesc(
                    ProductionStatus.VIRTUAL);
        } else {
            designs = labDesignRepository.findAllByProductionStatusOrderByCreatedAtDesc(
                    ProductionStatus.VIRTUAL);
        }

        return designs.stream()
                .map(LabDesignListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabDesignDetailResponseDto getDesignDetail(Long designId) {
        LabDesign design = labDesignRepository.findById(designId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_DESIGN_NOT_FOUND));

        return new LabDesignDetailResponseDto(design);
    }

    @Transactional
    public LabDesignLikeResponseDto toggleLike(Long userId, Long designId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        LabDesign design = labDesignRepository.findById(designId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_DESIGN_NOT_FOUND));

        Optional<LabDesignLike> existingLike = labDesignLikeRepository.findByUserIdAndLabDesignId(userId, designId);

        boolean isLiked;

        if (existingLike.isPresent()) {
            labDesignLikeRepository.delete(existingLike.get());
            design.decrementLikeCount();
            isLiked = false;
        } else {
            LabDesignLike newLike = LabDesignLike.builder()
                    .user(user)
                    .labDesign(design)
                    .build();
            labDesignLikeRepository.save(newLike);
            design.incrementLikeCount();
            isLiked = true;
        }

        return new LabDesignLikeResponseDto(isLiked, design.getLikesCount());
    }

    @Transactional(readOnly = true)
    public List<LabEditionResponseDto> getLabEditions() {
        List<LabDesign> editions = labDesignRepository.findAllByProductionStatusNot(ProductionStatus.VIRTUAL);

        return editions.stream()
                .map(LabEditionResponseDto::new)
                .collect(Collectors.toList());
    }
}