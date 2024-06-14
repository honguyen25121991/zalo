package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zalo.Spring_Zalo.Entities.CustomerEvent;

public interface CustomerEventRepo extends JpaRepository<CustomerEvent, Integer> {

}
