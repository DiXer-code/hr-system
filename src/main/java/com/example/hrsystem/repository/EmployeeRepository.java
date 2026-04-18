package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.report.DepartmentHeadcountReport;
import com.example.hrsystem.report.StatusDistributionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByOrderByLastNameAscFirstNameAsc();

    List<Employee> findByDepartmentIdOrderByLastNameAscFirstNameAsc(Integer departmentId);

    List<Employee> findTop6ByOrderByHireDateDescIdDesc();

    long countByDepartmentId(Integer departmentId);

    long countByStatus(String status);

    @Query("""
            select e
            from Employee e
            where lower(concat(coalesce(e.firstName, ''), ' ', coalesce(e.lastName, ''))) like lower(concat('%', :keyword, '%'))
               or lower(concat(coalesce(e.lastName, ''), ' ', coalesce(e.firstName, ''))) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(e.firstName, '')) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(e.lastName, '')) like lower(concat('%', :keyword, '%'))
            order by e.lastName asc, e.firstName asc
            """)
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
            select new com.example.hrsystem.report.DepartmentHeadcountReport(
                d.name,
                count(e),
                coalesce(sum(case when e.status = 'Активний' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'У відпустці' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'Лікарняний' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'Звільнений' then 1 else 0 end), 0),
                sum(e.position.salary)
            )
            from Department d
            left join Employee e on e.department = d
            group by d.id, d.name
            order by count(e) desc, d.name asc
            """)
    List<DepartmentHeadcountReport> buildDepartmentHeadcountReport();

    @Query("""
            select new com.example.hrsystem.report.DepartmentHeadcountReport(
                'Без департаменту',
                count(e),
                coalesce(sum(case when e.status = 'Активний' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'У відпустці' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'Лікарняний' then 1 else 0 end), 0),
                coalesce(sum(case when e.status = 'Звільнений' then 1 else 0 end), 0),
                sum(e.position.salary)
            )
            from Employee e
            where e.department is null
            """)
    DepartmentHeadcountReport buildUnassignedDepartmentSummary();

    @Query("""
            select new com.example.hrsystem.report.StatusDistributionReport(
                coalesce(e.status, 'Активний'),
                count(e)
            )
            from Employee e
            group by e.status
            order by count(e) desc, e.status asc
            """)
    List<StatusDistributionReport> buildStatusDistribution();
}
