package com.likelion.backend.domain.review.repository;

import com.likelion.backend.domain.review.entity.ReviewImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

  List<ReviewImage> findAllByReviewId(Long reviewId);
}
