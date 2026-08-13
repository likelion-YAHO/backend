package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.dto.*;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabDesignService {

    private final LabDesignRepository labDesignRepository;
    private final LabMissionRepository labMissionRepository;
    private final UserRepository userRepository; // 테스트를 위해 임시 유저 조회용
    private final LabDesignLikeRepository labDesignLikeRepository;

    public AiDesignResponseDto generateAiDesignDummy(AiDesignRequestDto request) {
        // TODO: 향후 OpenAI 연동 로직이 들어갈 자리입니다.
        // 현재는 테스트를 위해 단일 더미 이미지 URL을 반환합니다.
        String dummyImageUrl = "https://via.placeholder.com/500x500.png?text=MCM+Dummy+Bag";

        return new AiDesignResponseDto(dummyImageUrl);
    }

    @Transactional
    public LabDesignResponseDto createLabDesign(Long userId, LabDesignCreateRequestDto request) {
        // 1. 유저 및 미션 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        LabMission mission = labMissionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_MISSION_NOT_FOUND));

        // 2. 엔티티 생성 및 데이터 세팅
        LabDesign labDesign = LabDesign.builder()
                .user(user)
                .mission(mission)
                .baseProduct(request.getBaseProduct())
                .designName(request.getDesignName())
                .concept(request.getConcept())
                .aiPrompt(request.getAiPrompt())
                .usedMaterials(request.getUsedMaterials())
                .imageUrl(request.getImageUrl())
                .build();

        // 3. DB 저장 후 DTO로 반환
        LabDesign savedDesign = labDesignRepository.save(labDesign);
        return new LabDesignResponseDto(savedDesign);
    }

    @Transactional(readOnly = true)
    public List<LabDesignListResponseDto> getGalleryList(String sort) {
        List<LabDesign> designs;

        // 정렬 기준에 따라 다르게 조회 (대소문자 구분 없이 처리)
        if ("popular".equalsIgnoreCase(sort)) {
            designs = labDesignRepository.findAllByOrderByLikesCountDesc();
        } else {
            // 기본값은 최신순(latest)
            designs = labDesignRepository.findAllByOrderByCreatedAtDesc();
        }

        // Entity 리스트를 DTO 리스트로 변환하여 반환
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
            // 이미 좋아요를 눌렀다면 -> 좋아요 취소 (삭제 및 카운트 감소)
            labDesignLikeRepository.delete(existingLike.get());
            design.decrementLikeCount();
            isLiked = false;
        } else {
            // 좋아요를 누르지 않았다면 -> 좋아요 추가 (생성 및 카운트 증가)
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
        // VIRTUAL(가상) 상태가 아닌, 실물 제작이 확정된 디자인들만 조회
        List<LabDesign> editions = labDesignRepository.findAllByProductionStatusNot(ProductionStatus.VIRTUAL);

        return editions.stream()
                .map(LabEditionResponseDto::new)
                .collect(Collectors.toList());
    }
}