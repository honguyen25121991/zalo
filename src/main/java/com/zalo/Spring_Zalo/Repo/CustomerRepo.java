package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zalo.Spring_Zalo.Entities.Customer;

import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    Customer getByIdZalo(String zaloId);

    Customer findByIdZalo(String zaloId);

}
