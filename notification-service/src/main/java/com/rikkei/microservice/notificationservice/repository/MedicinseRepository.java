package com.rikkei.microservice.notificationservice.repository;

import com.rikkei.microservice.notificationservice.entity.Medicinse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicinseRepository extends JpaRepository<Medicinse, Long> {
}
