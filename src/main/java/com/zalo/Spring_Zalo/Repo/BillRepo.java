package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zalo.Spring_Zalo.Entities.Bill;

public interface BillRepo extends JpaRepository<Bill, Integer> {

    Bill findByBillCode(String billCode);

}
