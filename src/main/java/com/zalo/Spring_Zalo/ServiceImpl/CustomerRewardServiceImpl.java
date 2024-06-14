package com.zalo.Spring_Zalo.ServiceImpl;

import com.zalo.Spring_Zalo.Controller.CustomerController;
import com.zalo.Spring_Zalo.DTO.CustomerRewardDTO;
import com.zalo.Spring_Zalo.Entities.*;
import com.zalo.Spring_Zalo.Exception.ApiNotFoundException;
import com.zalo.Spring_Zalo.Exception.ResourceNotFoundException;
import com.zalo.Spring_Zalo.Repo.*;
import com.zalo.Spring_Zalo.Response.ApiResponse;
import com.zalo.Spring_Zalo.Service.CustomerRewardService;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerRewardServiceImpl implements CustomerRewardService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRewardService.class);
    @Autowired
    private ProductEventRepo productEventRepo;

    @Autowired
    private CustomerRewardRepo customerRewardRepo;

    @Autowired
    private RewardRepo rewardRepo;

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private CustomerPointRepo customerPointRepo;

    @Override
    @Transactional(rollbackOn = { Throwable.class, ResourceNotFoundException.class })

    public ApiResponse exchangeRewards(Integer customerId, Integer rewardId) throws Exception {

        Reward reward = rewardRepo.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("reward", "rewardId", rewardId));
        if (reward == null) {
            throw new ApiNotFoundException("No prize found please check again !!!");
        }
        Customer customer = customerRepo.findById(customerId).get();
        if (customer == null) {
            throw new ApiNotFoundException("customer not found ");
        }
        CustomerReward customerReward = customerRewardRepo.findByCustomerIdAndReWardId(customerId, rewardId);
        System.out.println("customer Reward " + customerReward);
        Integer eventId = reward.getEvent().getId();
        System.out.println("event :" + eventId);
        System.out.println("customerId :" + customerId);
        CustomerPoint customerPoint = customerPointRepo.findByCustomerAndEvent(customerId, eventId)
                .orElseThrow(() -> new ApiNotFoundException("CustomerPoint is not found !!!"));

        Event eventNow = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("event", "eventId", rewardId));
        if (customerReward == null) {
            CustomerReward newCustomerReward = new CustomerReward();
            newCustomerReward.setCustomer(customer);
            newCustomerReward.setEvent(eventNow);
            newCustomerReward.setReward(reward);
            if (reward.getReward_type() == 1) {
                newCustomerReward.setStatus(1);
            } else {
                newCustomerReward.setStatus(2);
            }
            // newCustomerReward.setStatus(eventNow.getType() == 1 ? 1 : 2);
            newCustomerReward.setExchangeRewardDate(LocalDate.now());
            if (customerPoint.getPoint() < reward.getPointReward()) {
                throw new ApiNotFoundException("You do not have enough points to redeem this prize !!!");
            }
            // Update customer points
            customerPoint.setPoint(customerPoint.getPoint() - reward.getPointReward());

            // Save entities
            customerRewardRepo.saveAndFlush(newCustomerReward);
            customerPointRepo.saveAndFlush(customerPoint);
            reward.setQuantity(reward.getQuantity() - 1);
            rewardRepo.saveAndFlush(reward);
            if (newCustomerReward.getStatus() == 1) {
                return new ApiResponse("Congratulations on winning your prize !!!", true, 200);
            } else {
                return new ApiResponse("Congratulations on register your prize !!!", true, 201);
            }
        }
        if (customerReward.getReward().getId() == rewardId) {
            return new ApiResponse("You're already have have this prize", false, 409);
        }
        if (customerPoint == null) {
            throw new ApiNotFoundException("you have no points !!!");
        }
        if (reward.getQuantity() <= 0) {
            throw new ApiNotFoundException("Sorry, the product is out of stock !!!");
        }
        if (customerPoint.getPoint() < reward.getPointReward()) {
            throw new ApiNotFoundException("You do not have enough points to redeem this prize !!!");
        }

        if (reward.getReward_type() == 1) {
            customerReward.setStatus(1);
        } else {
            customerReward.setStatus(2);
        }
        customerPoint.setPoint(customerPoint.getPoint() - reward.getPointReward());
        customerReward.setExchangeRewardDate(LocalDate.now());
        customerRewardRepo.saveAndFlush(customerReward);
        customerPointRepo.saveAndFlush(customerPoint);
        reward.setQuantity(reward.getQuantity() - 1);
        rewardRepo.saveAndFlush(reward);
        if (customerReward.getStatus() == 1) {
            return new ApiResponse("Congratulations on winning your prize !!!", true, 200);
        } else {
            return new ApiResponse("Congratulations on register your prize !!!", true, 201);
        }

    }

    @Override
    public List<CustomerRewardDTO> getRewardId(Integer rewardId) throws Exception {
        List<CustomerReward> customerRewards = customerRewardRepo.findByReWardId(rewardId);
        return mapToCustomerRewardDTO(customerRewards);
    }

    private List<CustomerRewardDTO> mapToCustomerRewardDTO(List<CustomerReward> customerRewards) {
        return customerRewards.stream()
                .map(this::mapToCustomerRewardDTO)
                .collect(Collectors.toList());
    }

    private CustomerRewardDTO mapToCustomerRewardDTO(CustomerReward customerReward) {
        CustomerRewardDTO customerRewardDTO = new CustomerRewardDTO();
        // Map các trường từ CustomerReward sang CustomerRewardDTO
        customerRewardDTO.setId(customerReward.getId());
        // Map các trường khác...

        // Lấy thông tin về Customer
        Customer customer = customerReward.getCustomer();
        if (customer != null) {
            customerRewardDTO.setCustomerId(customer.getId());
            customerRewardDTO.setCustomerName(customer.getName());
            // Các thông tin khác của Customer nếu cần thiết
        }

        // Lấy thông tin về Reward
        Reward reward = customerReward.getReward();
        if (reward != null) {
            customerRewardDTO.setRewardId(reward.getId());
            customerRewardDTO.setRewardName(reward.getName());
            // Các thông tin khác của Reward nếu cần thiết
        }

        // Lấy thông tin về Event
        Event event = customerReward.getEvent();
        if (event != null) {
            customerRewardDTO.setEventId(event.getId());
            customerRewardDTO.setEventName(event.getName());
            // Các thông tin khác của Event nếu cần thiết
        }

        return customerRewardDTO;
    }

}