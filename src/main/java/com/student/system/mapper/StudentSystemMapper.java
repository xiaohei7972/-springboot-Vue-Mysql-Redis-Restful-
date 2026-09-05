package com.student.system.mapper;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentSystemMapper {
    @Select("SELECT id, name, code, description, created_at AS createdAt FROM sys_department ORDER BY id DESC")
    List<Map<String, Object>> listDepartments();

    @Insert("INSERT INTO sys_department(name, code, description) VALUES (#{name}, #{code}, #{description})")
    int insertDepartment(@Param("name") String name, @Param("code") String code, @Param("description") Object description);

    @Update("UPDATE sys_department SET name=#{name}, code=#{code}, description=#{description} WHERE id=#{id}")
    int updateDepartment(@Param("id") long id, @Param("name") String name, @Param("code") String code, @Param("description") Object description);

    @Delete("DELETE FROM sys_department WHERE id=#{id}")
    int deleteDepartment(@Param("id") long id);

    @Select("""
            SELECT c.id, c.name, c.code, c.grade_year AS gradeYear, c.department_id AS departmentId,
                   d.name AS departmentName
            FROM sys_class c JOIN sys_department d ON d.id=c.department_id ORDER BY c.id DESC
            """)
    List<Map<String, Object>> listClasses();

    @Insert("INSERT INTO sys_class(name, code, department_id, grade_year) VALUES (#{name}, #{code}, #{departmentId}, #{gradeYear})")
    int insertClass(@Param("name") String name, @Param("code") String code, @Param("departmentId") long departmentId, @Param("gradeYear") int gradeYear);

    @Update("UPDATE sys_class SET name=#{name}, code=#{code}, department_id=#{departmentId}, grade_year=#{gradeYear} WHERE id=#{id}")
    int updateClass(@Param("id") long id, @Param("name") String name, @Param("code") String code, @Param("departmentId") long departmentId, @Param("gradeYear") int gradeYear);

    @Delete("DELETE FROM sys_class WHERE id=#{id}")
    int deleteClass(@Param("id") long id);

    @Select("""
            <script>
            SELECT COUNT(*) FROM student s
            <where>
              <if test="keyword != null and keyword != ''">
                s.student_no LIKE CONCAT('%', #{keyword}, '%')
                OR s.name LIKE CONCAT('%', #{keyword}, '%')
                OR s.phone LIKE CONCAT('%', #{keyword}, '%')
              </if>
            </where>
            </script>
            """)
    long countStudents(@Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT s.id, s.student_no AS studentNo, s.name, s.gender, s.phone, s.email,
                   s.department_id AS departmentId, d.name AS departmentName, s.class_id AS classId,
                   c.name AS className, s.admission_year AS admissionYear, s.status
            FROM student s LEFT JOIN sys_department d ON d.id=s.department_id
            LEFT JOIN sys_class c ON c.id=s.class_id
            <where>
              <if test="keyword != null and keyword != ''">
                s.student_no LIKE CONCAT('%', #{keyword}, '%')
                OR s.name LIKE CONCAT('%', #{keyword}, '%')
                OR s.phone LIKE CONCAT('%', #{keyword}, '%')
              </if>
            </where>
            ORDER BY s.id DESC LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> listStudents(@Param("keyword") String keyword, @Param("size") int size, @Param("offset") int offset);

    @Insert("""
            INSERT INTO student(student_no, name, gender, phone, email, department_id, class_id, admission_year, status)
            VALUES (#{studentNo}, #{name}, #{gender}, #{phone}, #{email}, #{departmentId}, #{classId}, #{admissionYear}, #{status})
            """)
    int insertStudent(Map<String, Object> body);

    @Update("""
            UPDATE student SET student_no=#{studentNo}, name=#{name}, gender=#{gender}, phone=#{phone}, email=#{email},
            department_id=#{departmentId}, class_id=#{classId}, admission_year=#{admissionYear}, status=#{status} WHERE id=#{id}
            """)
    int updateStudent(Map<String, Object> body);

    @Delete("DELETE FROM student WHERE id=#{id}")
    int deleteStudent(@Param("id") long id);

    @Select("""
            SELECT t.id, t.user_id AS userId, t.teacher_no AS teacherNo, t.name, t.title, t.phone,
                   t.department_id AS departmentId, d.name AS departmentName,
                   u.username, u.status AS userStatus
            FROM teacher t
            LEFT JOIN sys_department d ON d.id=t.department_id
            LEFT JOIN sys_user u ON u.id=t.user_id
            ORDER BY t.id DESC
            """)
    List<Map<String, Object>> listTeachers();

    @Insert("""
            INSERT INTO teacher(user_id, teacher_no, name, title, department_id, phone)
            VALUES (#{userId}, #{teacherNo}, #{name}, #{title}, #{departmentId}, #{phone})
            """)
    int insertTeacher(Map<String, Object> body);

    @Update("""
            UPDATE teacher SET teacher_no=#{teacherNo}, name=#{name}, title=#{title},
            department_id=#{departmentId}, phone=#{phone} WHERE id=#{id}
            """)
    int updateTeacher(Map<String, Object> body);

    @Delete("DELETE FROM teacher WHERE id=#{id}")
    int deleteTeacher(@Param("id") long id);

    @Select("SELECT user_id FROM teacher WHERE id=#{id}")
    Long findTeacherUserId(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM course WHERE teacher_id=#{teacherId}")
    long countTeacherCoursesById(@Param("teacherId") long teacherId);

    @Select("""
            <script>
            SELECT c.id, c.course_no AS courseNo, c.name, c.credit, c.hours, c.semester,
                   c.teacher_id AS teacherId, t.name AS teacherName, c.description
            FROM course c LEFT JOIN teacher t ON t.id=c.teacher_id
            <if test="teacherUserId != null">WHERE t.user_id=#{teacherUserId}</if>
            ORDER BY c.id DESC
            </script>
            """)
    List<Map<String, Object>> listCourses(@Param("teacherUserId") Long teacherUserId);

    @Insert("""
            INSERT INTO course(course_no, name, credit, hours, semester, teacher_id, description)
            VALUES (#{courseNo}, #{name}, #{credit}, #{hours}, #{semester}, #{teacherId}, #{description})
            """)
    int insertCourse(Map<String, Object> body);

    @Update("""
            UPDATE course SET course_no=#{courseNo}, name=#{name}, credit=#{credit}, hours=#{hours},
            semester=#{semester}, teacher_id=#{teacherId}, description=#{description} WHERE id=#{id}
            """)
    int updateCourse(Map<String, Object> body);

    @Delete("DELETE FROM course WHERE id=#{id}")
    int deleteCourse(@Param("id") long id);

    @Select("""
            <script>
            SELECT e.id, e.course_id AS courseId, c.course_no AS courseNo, c.name AS courseName,
                   e.student_id AS studentId, s.student_no AS studentNo, s.name AS studentName,
                   e.status, e.enrolled_at AS enrolledAt
            FROM enrollment e JOIN course c ON c.id=e.course_id JOIN student s ON s.id=e.student_id
            <where>
              <if test="studentUserId != null">s.user_id=#{studentUserId}</if>
              <if test="teacherUserId != null">c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{teacherUserId})</if>
              <if test="courseId != null">e.course_id=#{courseId}</if>
            </where>
            ORDER BY e.id DESC
            </script>
            """)
    List<Map<String, Object>> listEnrollments(@Param("studentUserId") Long studentUserId,
                                              @Param("teacherUserId") Long teacherUserId,
                                              @Param("courseId") Long courseId);

    @Insert("INSERT INTO enrollment(course_id, student_id) VALUES (#{courseId}, #{studentId})")
    int insertEnrollment(@Param("courseId") long courseId, @Param("studentId") long studentId);

    @Delete("""
            <script>
            DELETE FROM enrollment WHERE id=#{id}
            <if test="studentId != null">AND student_id=#{studentId}</if>
            </script>
            """)
    int deleteEnrollment(@Param("id") long id, @Param("studentId") Long studentId);

    @Select("""
            <script>
            SELECT g.id, g.enrollment_id AS enrollmentId, c.name AS courseName,
                   s.student_no AS studentNo, s.name AS studentName, g.usual_score AS usualScore,
                   g.midterm_score AS midtermScore, g.final_score AS finalScore, g.total_score AS totalScore,
                   g.grade_status AS gradeStatus
            FROM grade g JOIN enrollment e ON e.id=g.enrollment_id JOIN course c ON c.id=e.course_id
            JOIN student s ON s.id=e.student_id
            <where>
              <if test="studentUserId != null">s.user_id=#{studentUserId}</if>
              <if test="teacherUserId != null">c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{teacherUserId})</if>
            </where>
            ORDER BY g.id DESC
            </script>
            """)
    List<Map<String, Object>> listGrades(@Param("studentUserId") Long studentUserId, @Param("teacherUserId") Long teacherUserId);

    @Select("""
            SELECT COUNT(*) FROM enrollment e JOIN course c ON c.id=e.course_id
            WHERE e.id=#{enrollmentId} AND c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})
            """)
    long teacherOwnsEnrollment(@Param("enrollmentId") long enrollmentId, @Param("userId") long userId);

    @Insert("""
            INSERT INTO grade(enrollment_id, usual_score, midterm_score, final_score, total_score, grade_status)
            VALUES (#{enrollmentId}, #{usualScore}, #{midtermScore}, #{finalScore}, #{totalScore}, #{gradeStatus})
            ON DUPLICATE KEY UPDATE usual_score=VALUES(usual_score), midterm_score=VALUES(midterm_score),
            final_score=VALUES(final_score), total_score=VALUES(total_score), grade_status=VALUES(grade_status)
            """)
    int saveGrade(@Param("enrollmentId") long enrollmentId, @Param("usualScore") BigDecimal usualScore,
                  @Param("midtermScore") BigDecimal midtermScore, @Param("finalScore") BigDecimal finalScore,
                  @Param("totalScore") BigDecimal totalScore, @Param("gradeStatus") String gradeStatus);

    @Select("""
            <script>
            SELECT a.id, a.course_id AS courseId, c.name AS courseName, a.student_id AS studentId,
                   s.student_no AS studentNo, s.name AS studentName, a.attendance_date AS attendanceDate,
                   a.status, a.remark
            FROM attendance a JOIN course c ON c.id=a.course_id JOIN student s ON s.id=a.student_id
            <where>
              <if test="studentUserId != null">s.user_id=#{studentUserId}</if>
              <if test="teacherUserId != null">c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{teacherUserId})</if>
            </where>
            ORDER BY a.attendance_date DESC, a.id DESC
            </script>
            """)
    List<Map<String, Object>> listAttendance(@Param("studentUserId") Long studentUserId, @Param("teacherUserId") Long teacherUserId);

    @Select("SELECT COUNT(*) FROM course WHERE id=#{courseId} AND teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})")
    long teacherOwnsCourse(@Param("courseId") long courseId, @Param("userId") long userId);

    @Insert("""
            INSERT INTO attendance(course_id, student_id, attendance_date, status, remark)
            VALUES (#{courseId}, #{studentId}, #{attendanceDate}, #{status}, #{remark})
            ON DUPLICATE KEY UPDATE status=VALUES(status), remark=VALUES(remark)
            """)
    int saveAttendance(@Param("courseId") long courseId, @Param("studentId") long studentId,
                       @Param("attendanceDate") LocalDate attendanceDate, @Param("status") String status, @Param("remark") Object remark);

    @Select("""
            SELECT n.id, n.title, n.content, n.target_role AS targetRole, n.published_at AS publishedAt,
                   n.status, u.real_name AS publisherName
            FROM notice n JOIN sys_user u ON u.id=n.publisher_id
            WHERE n.target_role='ALL' OR n.target_role=#{role} ORDER BY n.published_at DESC
            """)
    List<Map<String, Object>> listNotices(@Param("role") String role);

    @Insert("INSERT INTO notice(title, content, publisher_id, target_role) VALUES (#{title}, #{content}, #{publisherId}, #{targetRole})")
    int insertNotice(@Param("title") String title, @Param("content") String content, @Param("publisherId") long publisherId, @Param("targetRole") String targetRole);

    @Delete("DELETE FROM notice WHERE id=#{id}")
    int deleteNotice(@Param("id") long id);

    @Select("SELECT id FROM student WHERE user_id=#{userId}")
    Long findStudentIdByUser(@Param("userId") long userId);

    @Select("SELECT COUNT(*) FROM student")
    long countStudentsAll();
    @Select("SELECT COUNT(*) FROM teacher")
    long countTeachers();
    @Select("SELECT COUNT(*) FROM course")
    long countCourses();
    @Select("SELECT COUNT(*) FROM sys_department")
    long countDepartments();
    @Select("SELECT COUNT(*) FROM course WHERE teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})")
    long countTeacherCourses(@Param("userId") long userId);
    @Select("SELECT COUNT(DISTINCT e.student_id) FROM enrollment e JOIN course c ON c.id=e.course_id WHERE c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})")
    long countTeacherStudents(@Param("userId") long userId);
    @Select("SELECT COUNT(*) FROM grade g JOIN enrollment e ON e.id=g.enrollment_id JOIN course c ON c.id=e.course_id WHERE c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})")
    long countTeacherGrades(@Param("userId") long userId);
    @Select("SELECT COUNT(*) FROM attendance a JOIN course c ON c.id=a.course_id WHERE c.teacher_id=(SELECT id FROM teacher WHERE user_id=#{userId})")
    long countTeacherAttendance(@Param("userId") long userId);
    @Select("SELECT COUNT(*) FROM enrollment WHERE student_id=#{studentId}")
    long countStudentCourses(@Param("studentId") long studentId);
    @Select("SELECT COUNT(*) FROM grade g JOIN enrollment e ON e.id=g.enrollment_id WHERE e.student_id=#{studentId}")
    long countStudentGrades(@Param("studentId") long studentId);
    @Select("SELECT COUNT(*) FROM attendance WHERE student_id=#{studentId}")
    long countStudentAttendance(@Param("studentId") long studentId);
    @Select("SELECT COUNT(*) FROM notice WHERE target_role='ALL' OR target_role='STUDENT'")
    long countStudentNotices();
    @Select("""
            SELECT id, title, target_role AS targetRole, published_at AS publishedAt
            FROM notice WHERE target_role='ALL' OR target_role=#{role} ORDER BY published_at DESC LIMIT 5
            """)
    List<Map<String, Object>> recentNotices(@Param("role") String role);
}
