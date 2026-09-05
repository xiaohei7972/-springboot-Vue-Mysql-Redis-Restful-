USE student_management;

INSERT INTO sys_department (id, name, code, description) VALUES
(1, '信息安全学院', 'XXAQ', '信息安全与软件工程相关专业'),
(2, '经济管理学院', 'JGGL', '经济管理类专业');
INSERT INTO sys_class (id, name, code, department_id, grade_year) VALUES
(1, '软件工程2025级1班', 'SE2501', 1, 2025), (2, '信息安全2025级1班', 'IS2501', 1, 2025), (3, '财务管理2025级1班', 'CW2501', 2, 2025);
INSERT INTO sys_user (id, username, password, real_name, role, status) VALUES
(1, 'admin', '$2a$10$DsQgcCwv/B9pmXQpUzUWVeTf1i.s957eVVRZ1g3oJpl1xJ8AgE/tu', '系统管理员', 'ADMIN', '1'),
(2, 'teacher01', '$2a$10$DsQgcCwv/B9pmXQpUzUWVeTf1i.s957eVVRZ1g3oJpl1xJ8AgE/tu', '张老师', 'TEACHER', '1'),
(3, 'student01', '$2a$10$DsQgcCwv/B9pmXQpUzUWVeTf1i.s957eVVRZ1g3oJpl1xJ8AgE/tu', '李同学', 'STUDENT', '1');
INSERT INTO teacher (id, user_id, teacher_no, name, title, department_id, phone) VALUES (1, 2, 'T2025001', '张老师', '讲师', 1, '13800000001');
INSERT INTO student (id, user_id, student_no, name, gender, phone, email, department_id, class_id, admission_year) VALUES
(1, 3, 'S2025001', '李同学', '男', '13900000001', 'student01@example.com', 1, 1, 2025),
(2, NULL, 'S2025002', '王同学', '女', '13900000002', 'student02@example.com', 1, 1, 2025),
(3, NULL, 'S2025003', '赵同学', '男', '13900000003', 'student03@example.com', 1, 2, 2025);
INSERT INTO course (id, course_no, name, credit, hours, semester, teacher_id, description) VALUES
(1, 'SE1001', '软件工程导论', 3.0, 48, '2026-2027-1', 1, '软件工程基础理论与实践'),
(2, 'SE1002', '数据库原理', 3.0, 48, '2026-2027-1', 1, '关系数据库设计与 SQL 实践'),
(3, 'SE1003', 'Web 应用开发', 3.0, 48, '2026-2027-1', 1, '前后端分离应用开发');
INSERT INTO enrollment (id, course_id, student_id) VALUES (1, 1, 1), (2, 2, 1), (3, 3, 1), (4, 1, 2), (5, 1, 3);
INSERT INTO grade (enrollment_id, usual_score, midterm_score, final_score, total_score, grade_status) VALUES
(1, 88, 84, 90, 88.2, '合格'), (2, 92, 88, 86, 88.4, '合格');
INSERT INTO attendance (course_id, student_id, attendance_date, status, remark) VALUES
(1, 1, '2026-09-01', '出勤', NULL), (1, 1, '2026-09-03', '迟到', '迟到5分钟'),
(2, 1, '2026-09-02', '出勤', NULL), (1, 2, '2026-09-01', '请假', '病假');
INSERT INTO notice (id, title, content, publisher_id, target_role) VALUES
(1, '新学期课程安排通知', '请同学们登录系统查看本学期课程和考勤安排。', 1, 'ALL'),
(2, '软件工程综合实践说明', '请按课程任务书完成系统设计、实现、测试与课程论文。', 2, 'STUDENT');
