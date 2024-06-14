package com.zalo.Spring_Zalo.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "product_name", nullable = false, length = 30)
    private String name;
    private boolean status;
    private String picture;
    private int point;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<ProductEvent> productEvents = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<CustomerEvent> customerEvents = new ArrayList<>();

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", status=" + status + ", picture=" + picture + ", point="
                + point + ", productEvents=" + productEvents + ", customerEvents=" + customerEvents + "]";
    }

}
