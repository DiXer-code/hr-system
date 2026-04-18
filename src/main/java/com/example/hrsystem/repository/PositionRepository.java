package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Position;
import com.example.hrsystem.report.PositionStaffingReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Integer> {
    List<Position> findAllByOrderByNameAsc();

    List<Position> findByDepartmentIdOrderByNameAsc(Integer departmentId);

    long countByDepartmentId(Integer departmentId);

    @Query("""
            select new com.example.hrsystem.report.PositionStaffingReport(
                d.name,
                p.name,
                p.salary,
                coalesce(sum(case when e.status <> 'Звільнений' then 1 else 0 end), 0)
            )
            from Position p
            left join p.department d
            left join Employee e on e.position = p
            group by d.id, d.name, p.id, p.name, p.salary
            order by coalesce(sum(case when e.status <> 'Звільнений' then 1 else 0 end), 0) asc, d.name asc, p.name asc
            """)
    List<PositionStaffingReport> buildPositionStaffingReport();
}
