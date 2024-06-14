package com.zalo.Spring_Zalo.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.zalo.Spring_Zalo.DTO.RewardDTO;
import com.zalo.Spring_Zalo.Entities.Reward;
import com.zalo.Spring_Zalo.Exception.ResourceNotFoundException;
import com.zalo.Spring_Zalo.Repo.RewardRepo;
import com.zalo.Spring_Zalo.Service.RewardService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/reward")
public class RewardController {
    @Autowired
    private RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Autowired
    private RewardRepo rewardRepo;

    /**
     * add new reward
     * 
     * @param reward
     * @return reward
     */
    @PostMapping("/add")
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward, @PathVariable Integer eventId) {
        if (reward == null) {
            return ResponseEntity.badRequest().build();
        }
        Reward newReward = rewardService.addReward(reward);
        return new ResponseEntity<>(newReward, HttpStatus.CREATED);

    }

    /**
     * Get infomation of 1 reward
     * 
     * @param eventId
     * @param rewardId
     * @return
     */
    @GetMapping("/event/{eventId}/reward/{rewardId}")
    public ResponseEntity<Reward> getRewardInfo(@PathVariable Integer eventId, @PathVariable Integer rewardId) {
        Reward rewardInfo = rewardRepo.getInfoReward(eventId, rewardId);
        if (rewardInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(rewardInfo);
    }

    /**
     * get List reward from database by event Id
     * 
     * @param event_id
     * @return List Reward
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RewardDTO>> getListRewardWithPagination(
            @PathVariable Integer eventId,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Reward> rewardsPage = rewardRepo.findByEvent_Id(eventId, pageable);

        if (rewardsPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<RewardDTO> rewardDTOList = rewardsPage.getContent().stream()
                .map(this::mapToRewardDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rewardDTOList);
    }

    // Method to map Reward entity data to RewardDTO
    private RewardDTO mapToRewardDTO(Reward reward) {
        return new RewardDTO(
                reward.getId(),
                reward.getName(),
                reward.getPointReward(),
                reward.getQuantity(),
                reward.getImage(),
                reward.getReward_type(),
                reward.getEvent().getId()
        // Add other fields if needed
        );
    }

    @GetMapping("/{rewardId}")
    public ResponseEntity<Reward> getRewardId(@PathVariable Integer rewardId) {
        Reward reward = rewardRepo.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("event", "eventId", rewardId));
        return ResponseEntity.ok(reward);
    }

    @GetMapping("/")
    public ResponseEntity<List<RewardDTO>> getAllRewardsWithEventIds() {

        List<Reward> rewards = rewardRepo.findAll(); // Fetch all rewards

        List<RewardDTO> rewardDTOList = rewards.stream()
                .map(this::mapToRewardDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rewardDTOList);
    }

}
