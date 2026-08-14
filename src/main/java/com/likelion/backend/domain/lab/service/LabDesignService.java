package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.dto.*;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import com.likelion.backend.global.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.repository.LabDesignRepository;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import org.springframework.transaction.annotation.Transactional;
import com.likelion.backend.domain.lab.dto.LabDesignLikeResponseDto;
import com.likelion.backend.domain.lab.entity.LabDesignLike;
import com.likelion.backend.domain.lab.repository.LabDesignLikeRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabDesignService {

    private final LabDesignRepository labDesignRepository;
    private final LabMissionRepository labMissionRepository;
    private final UserRepository userRepository; // 테스트를 위해 임시 유저 조회용
    private final LabDesignLikeRepository labDesignLikeRepository;

    private final AiProperties aiProperties;

    public AiDesignResponseDto generateAiDesign(AiDesignRequestDto request) {

        // 1. 시도 횟수 방어 로직 (3회 초과 시 에러 반환)
        if (request.getCurrentTryCount() != null && request.getCurrentTryCount() > 3) {
            throw new CustomException(GlobalErrorCode.AI_GENERATION_LIMIT_EXCEEDED);
        }

        // 2. 진짜 AI 호출 로직
        String baseProductName = request.getBaseProduct().getProductName();
        String refinedPrompt = "A professional studio product photograph of a complete, entire MCM " + baseProductName +
                ", viewed from a distance so that the entire bag including its bottom, sides, and straps are fully visible with generous negative space around it. " +
                "Base model: " + baseProductName + ". " +
                "User customization request: " + request.getPrompt() + ". " +
                "Centered composition, clean white studio background, high resolution, photorealistic leather texture.";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiProperties.getImageModel());
        requestBody.put("prompt", refinedPrompt);
        requestBody.put("n", 1);
        requestBody.put("size", aiProperties.getImageSize());
        // ❌ 문제의 'response_format'은 깔끔하게 제거했습니다!

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String requestUrl = aiProperties.getBaseUrl() + "/images/generations";

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    requestUrl,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            log.info("OpenAI 응답 전문 수신 완료");

            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                if (data != null && !data.isEmpty()) {
                    Map<String, Object> item = data.get(0);

                    // OpenAI가 url을 주든 b64_json을 주든 안전하게 처리
                    if (item.containsKey("url")) {
                        String imageUrl = (String) item.get("url");
                        return new AiDesignResponseDto(imageUrl);
                    } else if (item.containsKey("b64_json")) {
                        String b64 = (String) item.get("b64_json");

                        // 로컬 파일 저장 로직
                        byte[] imageBytes = Base64.getDecoder().decode(b64);
                        Path uploadDir = Paths.get("./uploads/ai-design");
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectories(uploadDir);
                        }

                        String fileName = UUID.randomUUID().toString() + ".png";
                        Path filePath = uploadDir.resolve(fileName);
                        Files.write(filePath, imageBytes);

                        log.info("이미지 파일 저장 완료 - path: {}", filePath.toAbsolutePath());

                        String fileUrl = "http://localhost:8080/uploads/ai-design/" + fileName;
                        return new AiDesignResponseDto(fileUrl);
                    }
                }
            }

            throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);

        } catch (Exception e) {
            log.error("AI 이미지 생성 실패 상세 에러: ", e);
            throw new CustomException(GlobalErrorCode.AI_GENERATION_FAILED);
        }
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
        return new LabDesignResponseDto(savedDesign);
    }

    @Transactional(readOnly = true)
    public List<LabDesignListResponseDto> getGalleryList(String sort) {
        List<LabDesign> designs;

        if ("popular".equalsIgnoreCase(sort)) {
            designs = labDesignRepository.findAllByOrderByLikesCountDesc();
        } else {
            designs = labDesignRepository.findAllByOrderByCreatedAtDesc();
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