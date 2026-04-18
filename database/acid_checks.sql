-- Atomicity:
-- транзакція не повинна залишати "половину" працівника при помилці.
START TRANSACTION;

INSERT INTO Employee (
    first_name,
    last_name,
    inn,
    status,
    hire_date
) VALUES (
    'Тест',
    'Атомарність',
    '9999999999',
    'Активний',
    CURRENT_DATE
);

INSERT INTO JobHistory (
    employee_id,
    start_date,
    event_type
) VALUES (
    LAST_INSERT_ID(),
    CURRENT_DATE,
    'ПРИЙНЯТТЯ'
);

-- Повторний inn має спричинити помилку та rollback.
INSERT INTO Employee (
    first_name,
    last_name,
    inn,
    status,
    hire_date
) VALUES (
    'Тест',
    'Дублікат',
    '9999999999',
    'Активний',
    CURRENT_DATE
);

ROLLBACK;

-- Consistency:
-- перевірка, що департамент не видаляється при наявності працівників.
SELECT d.id, d.name, COUNT(e.id) AS employee_count
FROM Department d
LEFT JOIN Employee e ON e.current_department_id = d.id
GROUP BY d.id, d.name
HAVING employee_count > 0;

-- Isolation:
-- сценарій виконується в двох сесіях.
-- Session A
-- START TRANSACTION;
-- UPDATE Employee SET status = 'У відпустці' WHERE id = 1;
-- DO SLEEP(20);
-- COMMIT;
--
-- Session B
-- START TRANSACTION;
-- SELECT status FROM Employee WHERE id = 1;
-- COMMIT;

-- Durability:
-- після COMMIT запис має бути видимий при наступній сесії.
START TRANSACTION;

INSERT INTO Vacation (
    employee_id,
    start_date,
    end_date,
    type
) VALUES (
    1,
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL 7 DAY,
    'Щорічна'
);

COMMIT;

SELECT *
FROM Vacation
WHERE employee_id = 1
ORDER BY id DESC;
