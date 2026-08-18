package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabMission;

public interface LabAddonRecommender {

  LabAddonRecommendation recommend(BaseProduct baseProduct, String prompt, LabMission mission);
}
