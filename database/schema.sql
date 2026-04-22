CREATE TABLE Department (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);

CREATE TABLE Position (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    salary DECIMAL(12,2),
    department_id INT,
    CONSTRAINT fk_position_department
        FOREIGN KEY (department_id) REFERENCES Department(id)
);

CREATE TABLE Employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    gender VARCHAR(255),
    marital_status VARCHAR(255),
    inn VARCHAR(255) UNIQUE,
    address_registration VARCHAR(255),
    address_actual VARCHAR(255),
    email VARCHAR(255),
    phone_main VARCHAR(255),
    phone_work VARCHAR(255),
    birth_date DATE,
    hire_date DATE,
    dismissal_date DATE,
    avatar LONGBLOB,
    avatar_content_type VARCHAR(255),
    status VARCHAR(255) NOT NULL DEFAULT 'Активний',
    current_department_id INT,
    current_position_id INT,
    CONSTRAINT fk_employee_department
        FOREIGN KEY (current_department_id) REFERENCES Department(id),
    CONSTRAINT fk_employee_position
        FOREIGN KEY (current_position_id) REFERENCES Position(id)
);

CREATE TABLE Document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    document_category VARCHAR(64),
    data LONGBLOB,
    CONSTRAINT fk_document_employee
        FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE JobHistory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    department_id INT,
    position_id INT,
    start_date DATE,
    end_date DATE,
    personal_salary DECIMAL(12,2),
    event_type VARCHAR(255),
    CONSTRAINT fk_job_history_employee
        FOREIGN KEY (employee_id) REFERENCES Employee(id),
    CONSTRAINT fk_job_history_department
        FOREIGN KEY (department_id) REFERENCES Department(id),
    CONSTRAINT fk_job_history_position
        FOREIGN KEY (position_id) REFERENCES Position(id)
);

CREATE TABLE Vacation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    type VARCHAR(255),
    CONSTRAINT fk_vacation_employee
        FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE Timesheet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    work_date DATE,
    worked_hours INT,
    CONSTRAINT fk_timesheet_employee
        FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE Education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    institution VARCHAR(255),
    faculty VARCHAR(255),
    degree VARCHAR(255),
    graduation_year VARCHAR(255),
    CONSTRAINT fk_education_employee
        FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE Role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE `User` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    role_id INT NOT NULL,
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id) REFERENCES Role(id)
);

CREATE INDEX idx_employee_department ON Employee(current_department_id);
CREATE INDEX idx_employee_position ON Employee(current_position_id);
CREATE INDEX idx_employee_status ON Employee(status);
CREATE INDEX idx_employee_hire_date ON Employee(hire_date);
CREATE INDEX idx_employee_name ON Employee(last_name, first_name);

CREATE INDEX idx_position_department ON Position(department_id);
CREATE INDEX idx_document_employee ON Document(employee_id);
CREATE INDEX idx_job_history_employee_dates ON JobHistory(employee_id, start_date, end_date);
CREATE INDEX idx_vacation_period ON Vacation(employee_id, start_date, end_date);
CREATE INDEX idx_timesheet_employee_date ON Timesheet(employee_id, work_date);
