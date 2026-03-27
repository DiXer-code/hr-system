package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByOrderByLastNameAscFirstNameAsc();

    List<Employee> findByDepartmentIdOrderByLastNameAscFirstNameAsc(Integer departmentId);

    long countByDepartmentId(Integer departmentId);

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
}
