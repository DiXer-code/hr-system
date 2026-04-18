-- 1. Звіт по департаментах
EXPLAIN
SELECT d.name AS department_name,
       COUNT(e.id) AS total_employees,
       SUM(CASE WHEN e.status = 'Активний' THEN 1 ELSE 0 END) AS active_employees,
       SUM(CASE WHEN e.status = 'У відпустці' THEN 1 ELSE 0 END) AS vacation_employees,
       SUM(CASE WHEN e.status = 'Лікарняний' THEN 1 ELSE 0 END) AS sick_employees,
       SUM(CASE WHEN e.status = 'Звільнений' THEN 1 ELSE 0 END) AS dismissed_employees,
       COALESCE(SUM(p.salary), 0) AS payroll_fund
FROM Department d
LEFT JOIN Employee e ON e.current_department_id = d.id
LEFT JOIN Position p ON p.id = e.current_position_id
GROUP BY d.id, d.name
ORDER BY total_employees DESC, d.name ASC;

-- 2. Позиції без активних призначень
EXPLAIN
SELECT d.name AS department_name,
       p.name AS position_name,
       p.salary,
       SUM(CASE WHEN e.id IS NOT NULL AND e.status <> 'Звільнений' THEN 1 ELSE 0 END) AS assigned_employees
FROM Position p
LEFT JOIN Department d ON d.id = p.department_id
LEFT JOIN Employee e ON e.current_position_id = p.id
GROUP BY d.id, d.name, p.id, p.name, p.salary
HAVING assigned_employees = 0
ORDER BY d.name, p.name;

-- 3. Останні найми
EXPLAIN
SELECT e.id,
       e.last_name,
       e.first_name,
       d.name AS department_name,
       p.name AS position_name,
       e.hire_date
FROM Employee e
LEFT JOIN Department d ON d.id = e.current_department_id
LEFT JOIN Position p ON p.id = e.current_position_id
ORDER BY e.hire_date DESC, e.id DESC
LIMIT 6;

-- 4. Поточні відпустки
EXPLAIN
SELECT e.last_name,
       e.first_name,
       d.name AS department_name,
       v.type,
       v.start_date,
       v.end_date
FROM Vacation v
JOIN Employee e ON e.id = v.employee_id
LEFT JOIN Department d ON d.id = e.current_department_id
WHERE CURRENT_DATE BETWEEN v.start_date AND v.end_date
ORDER BY v.start_date ASC, v.id ASC;

-- 5. Останні кадрові події
EXPLAIN
SELECT e.last_name,
       e.first_name,
       d.name AS department_name,
       p.name AS position_name,
       j.event_type,
       j.start_date,
       j.end_date,
       j.personal_salary
FROM JobHistory j
JOIN Employee e ON e.id = j.employee_id
LEFT JOIN Department d ON d.id = j.department_id
LEFT JOIN Position p ON p.id = j.position_id
ORDER BY j.start_date DESC, j.id DESC
LIMIT 8;

-- 6. Підсумок по табелях за 30 днів
EXPLAIN
SELECT COALESCE(SUM(t.worked_hours), 0) AS worked_hours_last_30_days
FROM Timesheet t
WHERE t.work_date >= CURRENT_DATE - INTERVAL 30 DAY;
