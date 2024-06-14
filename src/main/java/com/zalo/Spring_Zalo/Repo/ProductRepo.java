package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zalo.Spring_Zalo.Entities.Product;

public interface ProductRepo extends JpaRepository<Product, Integer> {

}
