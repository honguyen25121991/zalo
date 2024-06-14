package com.zalo.Spring_Zalo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "bill_code")
    private String billCode;
    @Column(name = "scan_date")
    private LocalDate scanDate;
    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "update_date")
    private LocalDateTime updateDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EnumManager.Billtatus status;
    @Column(name = "point", columnDefinition = "int default 0")
    private int point;
    @Column(name = "image")
    private String image;
    @Column(name = "delete_flag", columnDefinition = "boolean default false")
    private Boolean deleteFlag;
    @Column(name = "Event")
    private int eventId;
    @Column(name = "Customer")
    private int customerId;
    @Column(name = "eventName")
    private String eventName;
}
