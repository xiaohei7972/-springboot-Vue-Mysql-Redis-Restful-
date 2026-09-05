CREATE DATABASE IF NOT EXISTS student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS operation_log, notice, attendance, grade, enrollment, course, student, teacher, sys_class, sys_department, sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL UNIQUE, code VARCHAR(30) NOT NULL UNIQUE,
  description VARCHAR(255), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE sys_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL, code VARCHAR(30) NOT NULL UNIQUE,
  department_id BIGINT NOT NULL, grade_year INT NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_class_department FOREIGN KEY (department_id) REFERENCES sys_department(id)
);
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50) NOT NULL, role VARCHAR(20) NOT NULL, status CHAR(1) NOT NULL DEFAULT '1',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT UNIQUE, teacher_no VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL, title VARCHAR(50), department_id BIGINT, phone VARCHAR(30),
  CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_teacher_department FOREIGN KEY (department_id) REFERENCES sys_department(id)
);
CREATE TABLE student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT UNIQUE, student_no VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL, gender VARCHAR(10), phone VARCHAR(30), email VARCHAR(100),
  department_id BIGINT, class_id BIGINT, admission_year INT, status VARCHAR(20) NOT NULL DEFAULT '在读',
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_student_department FOREIGN KEY (department_id) REFERENCES sys_department(id),
  CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES sys_class(id)
);
CREATE TABLE course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, course_no VARCHAR(30) NOT NULL UNIQUE, name VARCHAR(100) NOT NULL,
  credit DECIMAL(4,1) NOT NULL DEFAULT 2.0, hours INT NOT NULL DEFAULT 32, semester VARCHAR(30) NOT NULL,
  teacher_id BIGINT, description VARCHAR(255),
  CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id)
);
CREATE TABLE enrollment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, course_id BIGINT NOT NULL, student_id BIGINT NOT NULL,
  enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, status VARCHAR(20) NOT NULL DEFAULT '正常',
  UNIQUE KEY uk_enrollment (course_id, student_id),
  CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student(id)
);
CREATE TABLE grade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, enrollment_id BIGINT NOT NULL UNIQUE,
  usual_score DECIMAL(5,2), midterm_score DECIMAL(5,2), final_score DECIMAL(5,2), total_score DECIMAL(5,2),
  grade_status VARCHAR(20), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_grade_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment(id)
);
CREATE TABLE attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, course_id BIGINT NOT NULL, student_id BIGINT NOT NULL,
  attendance_date DATE NOT NULL, status VARCHAR(20) NOT NULL, remark VARCHAR(255),
  UNIQUE KEY uk_attendance (course_id, student_id, attendance_date),
  CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student(id)
);
CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(150) NOT NULL, content TEXT NOT NULL,
  publisher_id BIGINT NOT NULL, target_role VARCHAR(20) NOT NULL DEFAULT 'ALL', published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL DEFAULT '已发布', CONSTRAINT fk_notice_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
);
CREATE TABLE operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT, action VARCHAR(100) NOT NULL, method VARCHAR(10), path VARCHAR(255),
  ip VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
