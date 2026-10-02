package com.resilire.backend.doctor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    boolean existsByRut(String rut);

    @Query("SELECT d FROM DoctorProfile d WHERE d.user.enabled = true "
            + "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', CAST(:specialization AS string), '%'))) "
            + "AND (:name IS NULL OR LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))")
    Page<DoctorProfile> search(
            @Param("specialization") String specialization,
            @Param("name") String name,
            Pageable pageable);

    Optional<DoctorProfile> findByIdAndUserEnabledTrue(Long id);
}
