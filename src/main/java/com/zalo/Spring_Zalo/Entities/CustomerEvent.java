package com.zalo.Spring_Zalo.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "customer_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEvent implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int quantity;
    @Column(name = "create_date")
    private LocalDate createDate;
    @Column(name = "update_date")
    private LocalDate updateDate;
    @Column(name = "delete_flag")
    private boolean deleteFlag;
    @Column(name = "update_by_id")
    private Integer updateById;
    @Column(name = "edit_by_id")
    private Integer editById;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "product_id")
    private Product product;

}
