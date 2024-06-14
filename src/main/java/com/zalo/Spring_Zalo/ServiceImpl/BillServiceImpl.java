package com.zalo.Spring_Zalo.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zalo.Spring_Zalo.Entities.Bill;
import com.zalo.Spring_Zalo.Repo.BillRepo;
import com.zalo.Spring_Zalo.Service.BillService;

@Service

public class BillServiceImpl implements BillService {
    @Autowired
    private BillRepo billRepository;

    public Bill findBillByCode(String billCode) {
        // Ensure billCode is not null before calling the repository
        if (billCode != null) {
            return billRepository.findByBillCode(billCode);
        } else {
            // Handle the case where billCode is null, throw an exception, log, etc.
            throw new IllegalArgumentException("Bill code cannot be null");
        }
    }

    public Bill savebill(Bill bill) {
        return billRepository.save(bill);
    }
}
