package com.zalo.Spring_Zalo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "companys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "company_name", length = 40)
    private String name;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    List<Event> businessEvents = new ArrayList<>();
    @Column(name = "company_code", length = 10, unique = true)
    private String companyCode;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "address", length = 100)
    private String address;

    @Override
    public String toString() {
        return "Company [id=" + id + ", name=" + name + ", companyCode=" + companyCode
                + ", email=" + email + ", phoneNumber=" + phoneNumber + ", address=" + address
                + ", users=" + (users != null ? users.size() : "null") + "]";
    }

}