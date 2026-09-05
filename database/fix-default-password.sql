USE student_management;

UPDATE sys_user
SET password = '$2a$10$DsQgcCwv/B9pmXQpUzUWVeTf1i.s957eVVRZ1g3oJpl1xJ8AgE/tu',
    status = '1'
WHERE username IN ('admin', 'teacher01', 'student01');
