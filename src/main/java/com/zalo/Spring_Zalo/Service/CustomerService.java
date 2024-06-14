package com.zalo.Spring_Zalo.Service;

import java.util.Optional;

import com.zalo.Spring_Zalo.Entities.Customer;

public interface CustomerService {
    Customer createAccountCustomer(Customer customer);

    Customer getUserById(Integer userId);

    Customer getUserByZaloId(String zaloId);

    Customer findUserByZaloId(String zaloId);

    Customer updateCustomer(Customer customer, Integer userId);

    Customer updateAddressCustomer(Customer customer, Integer userId);
}
