package com.zalo.Spring_Zalo.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardDTO {
    
    private int id;
    private String name;
    private int pointReward;
    private int quantity;
    private String image;
    private int reward_Type;
    private int eventId;
    public RewardDTO(int id, String name, int pointReward, int quantity, String image, int reward_Type, int eventId) {
        this.id = id;
        this.name = name;
        this.pointReward = pointReward;
        this.quantity = quantity;
        this.image = image;
        this.reward_Type = reward_Type;
        this.eventId = eventId;
    }
    
}
