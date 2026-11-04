package com.rikkei.microservice.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicinse")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicinse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicineName;
    private String description;
    private Double price;
    private Integer quantity;
}
