package com.zalo.Spring_Zalo.Controller;

import com.zalo.Spring_Zalo.DTO.CustomerRewardDTO;
import com.zalo.Spring_Zalo.Entities.CustomerReward;
import com.zalo.Spring_Zalo.Repo.CustomerRewardRepo;
import com.zalo.Spring_Zalo.Response.ApiResponse;
import com.zalo.Spring_Zalo.Service.CustomerRewardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer_rewards")
@CrossOrigin("*")
public class CustomerRewardController {
    @Autowired
    private CustomerRewardRepo customerRewardRepo;

    @Autowired
    private CustomerRewardService customerRewardService;

    @GetMapping("/{customerId}")
    public ResponseEntity<List<CustomerRewardDTO>> getCustomerRewards(@PathVariable Integer customerId) {
        List<CustomerReward> customerRewards = customerRewardRepo.findByCustomer_Id(customerId);

        List<CustomerRewardDTO> customerRewardDTOs = customerRewards.stream()
                .map(this::mapToCustomerRewardDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(customerRewardDTOs);
    }

    private CustomerRewardDTO mapToCustomerRewardDTO(CustomerReward customerReward) {
        CustomerRewardDTO customerRewardDTO = new CustomerRewardDTO();
        customerRewardDTO.setId(customerReward.getId());
        customerRewardDTO.setStatus(customerReward.getStatus());
        customerRewardDTO.setExchangeRewardDate(customerReward.getExchangeRewardDate());

        // Set customer information
        customerRewardDTO.setCustomerId(customerReward.getCustomer().getId());
        customerRewardDTO.setCustomerName(customerReward.getCustomer().getName());
        // Add more customer fields if needed

        // Set reward information
        customerRewardDTO.setRewardId(customerReward.getReward().getId());
        customerRewardDTO.setRewardName(customerReward.getReward().getName());
        // Add more reward fields if needed

        // Set event information
        customerRewardDTO.setEventId(customerReward.getEvent().getId());
        customerRewardDTO.setEventName(customerReward.getEvent().getName());
        customerRewardDTO.setImage(customerReward.getReward().getImage());
        customerRewardDTO.setPoint(customerReward.getReward().getPointReward());

        // Add more event fields if needed

        return customerRewardDTO;
    }

    @PostMapping("/customer/{customerId}/reward/{rewardId}")
    public ResponseEntity<ApiResponse> getExchangeRewards(@PathVariable("customerId") Integer customerId,
            @PathVariable("rewardId") Integer rewardId) throws Exception {
        ApiResponse apiResponse = customerRewardService.exchangeRewards(customerId, rewardId);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/reward/{rewardId}")
    public ResponseEntity<List<CustomerRewardDTO>> getRewardId(@PathVariable Integer rewardId) {

        try {
            List<CustomerRewardDTO> customerRewardDTOs;
            customerRewardDTOs = customerRewardService.getRewardId(rewardId);
            return ResponseEntity.ok(customerRewardDTOs);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;

    }

    /**
     * this methob filter list customer has reward folowing status
     * 1: customer had register for the reward
     * 2: customer had recive the reward
     *
     * @param status
     * @return List Cusomter reward
     */
    @GetMapping("/reward/status/{status}")
    public ResponseEntity<List<CustomerReward>> getListCustomerRegisterReward(@PathVariable("status") Integer status) {
        List<CustomerReward> listRegisteCustomerRewards = customerRewardRepo.findByStatus(status);
        return ResponseEntity.ok(listRegisteCustomerRewards);
    }

    /**
     * list all customer has reward by event Id
     *
     * @return
     */
    @GetMapping("/event/{eventId}/customer/{customerId}")
    public ResponseEntity<List<Object>> getListCustomerReward(@PathVariable("eventId") Integer eventId,
            @PathVariable("customerId") Integer customerId) {
        List<Object> result = new ArrayList<>();
        for (Object[] objects : customerRewardRepo.findByCustomerIdAndEventId(customerId, eventId)) {
            Map<String, Object> map = new HashMap<>();

            map.put("reward", objects[0]);
            map.put("phoneNumber", objects[1]);
            map.put("exchangeRewardDate", objects[2]);
            map.put("status", objects[3]);
            map.put("statusReq", 200);
            result.add(map);
        }
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("event/{eventId}/win")
    public ResponseEntity<Object> getCustomerListWin(@PathVariable("eventId") Integer eventId) {
        List<Object> result = new ArrayList<>();
        for (Object[] objects : customerRewardRepo.findByEventId(eventId)) {
            Map<String, Object> map = new HashMap<>();

            map.put("reward", objects[0]);
            map.put("phoneNumber", objects[1]);
            map.put("exchangeRewardDate", objects[2]);
            map.put("status", objects[3]);
            map.put("statusReq", 200);

            result.add(map);
        }
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/win/{customerId}")
    public ResponseEntity<Object> getCustomerWin(@PathVariable("customerId") Integer customerId) {
        List<Object> list = customerRewardRepo.findByCustomerId(customerId);
        if (list != null && list.size() > 0) {
            return ResponseEntity.ok(list);
        }
        return new ResponseEntity<>(new ApiResponse("Customer id win not found !!!", false, 205),
                HttpStatus.BAD_GATEWAY);
    }

    @PostMapping("/admin/")
    public ApiResponse ChangeCustomerRewardsStatus(@RequestParam("customerRewardId") Integer customerRewardId,
            @RequestParam("status") Integer status) {
        Optional<CustomerReward> theChange = customerRewardRepo.findById(customerRewardId);
        if (theChange.isEmpty()) {
            return new ApiResponse("Không tìm thấy lịch sử giải thưởng! Vui lòng thử lại", false, 404);
        }
        CustomerReward customerReward = theChange.get();

        switch (status) {
            case 0:
                // Không trúng thưởng
                customerReward.setStatus(0);
                break;
            case 1:
                // Đã trúng thưởng
                customerReward.setStatus(1);
                break;
            case 2:
                // Đã đăng ký
                customerReward.setStatus(2);
                break;
            case 3:
                // Đã đổi
                customerReward.setStatus(3);
                break;
            default:
                // Trường hợp không xác định
                return new ApiResponse("Giá trị trạng thái không hợp lệ!", false, 400);
        }

        // Lưu trạng thái mới vào cơ sở dữ liệu
        customerRewardRepo.save(customerReward);

        return new ApiResponse("Cập nhật trạng thái thành công ", true, 200);
    }

    @GetMapping("/admin/{rewardId}")
    public ResponseEntity<List<CustomerReward>> filterRewrardByRewward(@RequestParam("rewardId") Integer rewardId) {
        List<CustomerReward> list = customerRewardRepo.findByReWardId(rewardId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/admin/")
    public ResponseEntity<Page<CustomerRewardDTO>> getAllCustomerRewards(
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<CustomerReward> customerRewardsPage = customerRewardRepo.findAll(pageable);

        if (customerRewardsPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<CustomerRewardDTO> customerRewardDTOPage = customerRewardsPage.map(this::mapToCustomerRewardDTO);

        return ResponseEntity.ok(customerRewardDTOPage);
    }

}
