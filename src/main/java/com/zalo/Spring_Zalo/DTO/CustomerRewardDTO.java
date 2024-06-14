package com.zalo.Spring_Zalo.DTO;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CustomerRewardDTO {
     private int id;
    private int status;
    private LocalDate exchangeRewardDate;
    private int customerId;
    private String customerName; // Add more customer fields if needed
    private int rewardId;
    private String rewardName; // Add more reward fields if needed
    private int eventId;
    private String eventName;
    private String image;
    private int point;

    public CustomerRewardDTO() {
        this.id = id;
        this.status = status;
        this.exchangeRewardDate = exchangeRewardDate;
        this.customerId = customerId;
        this.customerName = customerName;
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.image = image;
        this.point = point;
    } 
    
}
