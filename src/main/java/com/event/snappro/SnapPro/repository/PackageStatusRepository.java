package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.PackageStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageStatusRepository extends JpaRepository<PackageStatusEntity, Integer> {
    Optional<PackageStatusEntity> findByStatusIgnoreCase(String status);
}
