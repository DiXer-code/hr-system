# Концептуальна, логічна та фізична моделі

## 1. Концептуальна модель

Система оперує такими сутностями:

- `Department`
- `Position`
- `Employee`
- `JobHistory`
- `Document`
- `Vacation`
- `Timesheet`
- `Education`
- `Role`
- `User`

Основні зв'язки:

- один `Department` має багато `Position`;
- один `Department` має багато `Employee`;
- один `Position` може бути призначений багатьом `Employee`;
- один `Employee` має багато `JobHistory`;
- один `Employee` має багато `Document`;
- один `Employee` має багато `Vacation`;
- один `Employee` має багато `Timesheet`;
- один `Role` має багато `User`.

## 2. Логічна модель

### Ключові таблиці та ключі

- `Department(id PK, name, description)`
- `Position(id PK, name, salary, department_id FK -> Department.id)`
- `Employee(id PK, first_name, last_name, middle_name, inn, email, hire_date, dismissal_date, status, current_department_id FK, current_position_id FK, ...)`
- `JobHistory(id PK, employee_id FK, department_id FK, position_id FK, start_date, end_date, personal_salary, event_type)`
- `Document(id PK, employee_id FK, file_name, file_type, data)`
- `Vacation(id PK, employee_id FK, start_date, end_date, type)`
- `Timesheet(id PK, employee_id FK, work_date, worked_hours)`
- `Education(id PK, employee_id FK, institution, faculty, degree, graduation_year)`
- `Role(id PK, name, description)`
- `User(id PK, username, password, email, role_id FK -> Role.id)`

### Нормалізація

- `Department`, `Position`, `Role` виступають довідниками.
- кадрова історія винесена в окрему таблицю `JobHistory`, щоб не дублювати минулі стани в `Employee`;
- документи, табелі, відпустки та освіта відокремлені в підлеглі таблиці, що зменшує надлишковість.

## 3. Патерни проєктування

У проєкті використано такі патерни:

- `MVC` — контролери обробляють запити, шаблони відображають результат.
- `Repository` — доступ до БД інкапсульований в інтерфейсах Spring Data JPA.
- `Service Layer` — бізнес-логіка збереження працівника винесена в `EmployeeService`.
- `DTO/Report record` — для звітів використані окремі record-класи (`DepartmentHeadcountReport`, `PositionStaffingReport`, `StatusDistributionReport`, `ReportDashboard`).

## 4. Фізична модель

Фізична схема БД описана в окремому SQL-файлі:

- [database/schema.sql](../../database/schema.sql)

У фізичній моделі передбачені:

- первинні ключі;
- зовнішні ключі;
- індекси для типових вибірок;
- унікальність `inn` для працівника;
- тип `LONGBLOB` для файлів і фотографій.
