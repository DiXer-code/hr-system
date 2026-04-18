package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VacationRepository extends JpaRepository<Vacation, Integer> {
    List<Vacation> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    @Query("""
            select count(v)
            from Vacation v
            where :today between v.startDate and v.endDate
            """)
    long countActiveOn(@Param("today") LocalDate today);

    @Query("""
            select v
            from Vacation v
            join fetch v.employee e
            left join fetch e.department
            left join fetch e.position
            where :today between v.startDate and v.endDate
            order by v.startDate asc, v.id asc
            """)
    List<Vacation> findActiveOn(@Param("today") LocalDate today);
}
