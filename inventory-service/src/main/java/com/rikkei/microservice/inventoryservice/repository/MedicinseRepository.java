package com.rikkei.microservice.inventoryservice.repository;

import com.rikkei.microservice.inventoryservice.entity.Medicinse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicinseRepository extends JpaRepository<Medicinse, Long> {
}
