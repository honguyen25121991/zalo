package com.zalo.Spring_Zalo.Service;

import com.zalo.Spring_Zalo.DTO.CustomerRewardDTO;
import com.zalo.Spring_Zalo.Entities.CustomerReward;
import com.zalo.Spring_Zalo.Entities.ListReward;
import com.zalo.Spring_Zalo.Entities.Reward;
import com.zalo.Spring_Zalo.Response.ApiResponse;

import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface CustomerRewardService {
    @Modifying(clearAutomatically = true)
    ApiResponse exchangeRewards(Integer customerId, Integer rewardId) throws Exception;

    List<CustomerRewardDTO> getRewardId(Integer rewardId) throws Exception;

}
