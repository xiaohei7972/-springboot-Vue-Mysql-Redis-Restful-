package com.student.academic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.student.common.BusinessException;
import com.student.common.PageResult;
import com.student.system.entity.ClassEntity;
import com.student.system.entity.CourseEntity;
import com.student.system.entity.DepartmentEntity;
import com.student.system.entity.StudentEntity;
import com.student.system.entity.TeacherEntity;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.ClassMapper;
import com.student.system.mapper.CourseMapper;
import com.student.system.mapper.DepartmentMapper;
import com.student.system.mapper.StudentMapper;
import com.student.system.mapper.StudentSystemMapper;
import com.student.system.mapper.TeacherMapper;
import com.student.system.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AcademicService {
    private final StudentSystemMapper mapper;
    private final DepartmentMapper departmentMapper;
    private final ClassMapper classMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AcademicService(StudentSystemMapper mapper, DepartmentMapper departmentMapper, ClassMapper classMapper,
                           StudentMapper studentMapper, TeacherMapper teacherMapper, CourseMapper courseMapper,
                           UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.departmentMapper = departmentMapper;
        this.classMapper = classMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Map<String, Object>> departments(Authentication authentication) {
        requireAdmin(authentication);
        return mapper.listDepartments();
    }

    @Transactional
    public void createDepartment(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        DepartmentEntity department = new DepartmentEntity();
        department.setName(required(body, "name"));
        department.setCode(required(body, "code"));
        department.setDescription(stringValue(body.get("description")));
        departmentMapper.insert(department);
    }

    @Transactional
    public void updateDepartment(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        DepartmentEntity department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(404, "院系不存在");
        }
        department.setName(required(body, "name"));
        department.setCode(required(body, "code"));
        department.setDescription(stringValue(body.get("description")));
        departmentMapper.updateById(department);
    }

    @Transactional
    public void deleteDepartment(long id, Authentication authentication) {
        requireAdmin(authentication);
        departmentMapper.deleteById(id);
    }

    public List<Map<String, Object>> classes(Authentication authentication) {
        requireAdmin(authentication);
        return mapper.listClasses();
    }

    @Transactional
    public void createClass(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        ClassEntity entity = new ClassEntity();
        entity.setName(required(body, "name"));
        entity.setCode(required(body, "code"));
        entity.setDepartmentId(requiredLong(body, "departmentId"));
        entity.setGradeYear(requiredInt(body, "gradeYear"));
        classMapper.insert(entity);
    }

    @Transactional
    public void updateClass(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        ClassEntity entity = classMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "班级不存在");
        }
        entity.setName(required(body, "name"));
        entity.setCode(required(body, "code"));
        entity.setDepartmentId(requiredLong(body, "departmentId"));
        entity.setGradeYear(requiredInt(body, "gradeYear"));
        classMapper.updateById(entity);
    }

    @Transactional
    public void deleteClass(long id, Authentication authentication) {
        requireAdmin(authentication);
        classMapper.deleteById(id);
    }

    public PageResult<Map<String, Object>> students(int page, int size, String keyword, Authentication authentication) {
        requireAdmin(authentication);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        String key = keyword == null ? "" : keyword.trim();
        return new PageResult<>(mapper.listStudents(key, safeSize, (safePage - 1) * safeSize),
                mapper.countStudents(key), safePage, safeSize);
    }

    @Transactional
    public void createStudent(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        StudentEntity student = new StudentEntity();
        student.setStudentNo(required(body, "studentNo"));
        student.setName(required(body, "name"));
        student.setGender(stringValue(body.get("gender")));
        student.setPhone(stringValue(body.get("phone")));
        student.setEmail(stringValue(body.get("email")));
        student.setDepartmentId(optionalLong(body.get("departmentId")));
        student.setClassId(optionalLong(body.get("classId")));
        student.setAdmissionYear(optionalInt(body.get("admissionYear")));
        student.setStatus(body.get("status") == null ? "在读" : String.valueOf(body.get("status")));
        studentMapper.insert(student);
    }

    @Transactional
    public void updateStudent(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        StudentEntity student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        if (body.containsKey("studentNo")) student.setStudentNo(required(body, "studentNo"));
        if (body.containsKey("name")) student.setName(required(body, "name"));
        if (body.containsKey("gender")) student.setGender(stringValue(body.get("gender")));
        if (body.containsKey("phone")) student.setPhone(stringValue(body.get("phone")));
        if (body.containsKey("email")) student.setEmail(stringValue(body.get("email")));
        if (body.containsKey("departmentId")) student.setDepartmentId(optionalLong(body.get("departmentId")));
        if (body.containsKey("classId")) student.setClassId(optionalLong(body.get("classId")));
        if (body.containsKey("admissionYear")) student.setAdmissionYear(optionalInt(body.get("admissionYear")));
        if (body.containsKey("status")) student.setStatus(required(body, "status"));
        studentMapper.updateById(student);
    }

    @Transactional
    public void deleteStudent(long id, Authentication authentication) {
        requireAdmin(authentication);
        studentMapper.deleteById(id);
    }

    public List<Map<String, Object>> teachers(Authentication authentication) {
        requireAdmin(authentication);
        return mapper.listTeachers();
    }

    @Transactional
    public void createTeacher(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        String username = required(body, "username").trim();
        String name = required(body, "name").trim();
        String teacherNo = required(body, "teacherNo").trim();
        if (userMapper.selectCount(new QueryWrapper<UserEntity>().eq("username", username)) > 0) {
            throw new BusinessException(409, "用户名已存在");
        }

        UserEntity account = new UserEntity();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(
                body.get("password") == null || String.valueOf(body.get("password")).isBlank()
                        ? "123456" : String.valueOf(body.get("password"))));
        account.setRealName(name);
        account.setRole("TEACHER");
        account.setStatus("1");
        userMapper.insert(account);

        TeacherEntity teacher = new TeacherEntity();
        teacher.setUserId(account.getId());
        teacher.setTeacherNo(teacherNo);
        teacher.setName(name);
        teacher.setTitle(stringValue(body.get("title")));
        teacher.setDepartmentId(optionalLong(body.get("departmentId")));
        teacher.setPhone(stringValue(body.get("phone")));
        teacherMapper.insert(teacher);
    }

    @Transactional
    public void updateTeacher(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        TeacherEntity teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }
        if (body.containsKey("teacherNo")) teacher.setTeacherNo(required(body, "teacherNo").trim());
        if (body.containsKey("name")) teacher.setName(required(body, "name").trim());
        if (body.containsKey("title")) teacher.setTitle(stringValue(body.get("title")));
        if (body.containsKey("departmentId")) teacher.setDepartmentId(optionalLong(body.get("departmentId")));
        if (body.containsKey("phone")) teacher.setPhone(stringValue(body.get("phone")));
        teacherMapper.updateById(teacher);

        Long userId = teacher.getUserId();
        if (userId != null) {
            UserEntity account = userMapper.selectById(userId);
            if (account != null) {
                if (body.containsKey("name")) {
                    account.setRealName(teacher.getName());
                }
                if (body.containsKey("userStatus")) {
                    account.setStatus("0".equals(String.valueOf(body.get("userStatus"))) ? "0" : "1");
                }
                if (body.get("password") != null && !String.valueOf(body.get("password")).isBlank()) {
                    account.setPassword(passwordEncoder.encode(String.valueOf(body.get("password"))));
                }
                userMapper.updateById(account);
            }
        }
    }

    @Transactional
    public void deleteTeacher(long id, Authentication authentication) {
        requireAdmin(authentication);
        if (mapper.countTeacherCoursesById(id) > 0) {
            throw new BusinessException(409, "教师仍负责课程，请先调整课程授课教师");
        }
        TeacherEntity teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }
        Long userId = teacher.getUserId();
        teacherMapper.deleteById(id);
        if (userId != null) {
            userMapper.deleteById(userId);
        }
    }

    public List<Map<String, Object>> courses(Authentication authentication) {
        String currentRole = role(authentication);
        Long teacherUserId = "TEACHER".equals(currentRole) ? userId(authentication) : null;
        Long studentUserId = "STUDENT".equals(currentRole) ? userId(authentication) : null;
        return mapper.listCourses(teacherUserId, studentUserId);
    }

    public List<Map<String, Object>> availableCourses(Authentication authentication) {
        requireRole(authentication, "STUDENT");
        return mapper.listAvailableCourses(userId(authentication));
    }

    @Transactional
    public void createCourse(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        CourseEntity course = new CourseEntity();
        course.setCourseNo(required(body, "courseNo").trim());
        course.setName(required(body, "name").trim());
        course.setCredit(body.get("credit") == null ? new BigDecimal("2.0") : decimalValue(body.get("credit")));
        course.setHours(body.get("hours") == null ? 32 : Integer.valueOf(String.valueOf(body.get("hours"))));
        course.setSemester(required(body, "semester").trim());
        course.setTeacherId(optionalLong(body.get("teacherId")));
        course.setDescription(stringValue(body.get("description")));
        courseMapper.insert(course);
    }

    @Transactional
    public void updateCourse(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        CourseEntity course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (body.containsKey("courseNo")) course.setCourseNo(required(body, "courseNo").trim());
        if (body.containsKey("name")) course.setName(required(body, "name").trim());
        if (body.containsKey("credit")) course.setCredit(decimalValue(body.get("credit")));
        if (body.containsKey("hours")) course.setHours(Integer.valueOf(required(body, "hours")));
        if (body.containsKey("semester")) course.setSemester(required(body, "semester").trim());
        if (body.containsKey("teacherId")) course.setTeacherId(optionalLong(body.get("teacherId")));
        if (body.containsKey("description")) course.setDescription(stringValue(body.get("description")));
        courseMapper.updateById(course);
    }

    @Transactional
    public void deleteCourse(long id, Authentication authentication) {
        requireAdmin(authentication);
        if (courseMapper.selectById(id) == null) {
            throw new BusinessException(404, "课程不存在");
        }
        courseMapper.deleteById(id);
    }

    public List<Map<String, Object>> enrollments(Long courseId, Authentication authentication) {
        String currentRole = role(authentication);
        return mapper.listEnrollments("STUDENT".equals(currentRole) ? userId(authentication) : null,
                "TEACHER".equals(currentRole) ? userId(authentication) : null, courseId);
    }

    @Transactional
    public void createEnrollment(Map<String, Object> body, Authentication authentication) {
        String currentRole = role(authentication);
        requireRole(authentication, "ADMIN", "STUDENT");
        long studentId = "STUDENT".equals(currentRole) ? ownStudentId(authentication) : requiredLong(body, "studentId");
        mapper.insertEnrollment(requiredLong(body, "courseId"), studentId);
    }

    @Transactional
    public void deleteEnrollment(long id, Authentication authentication) {
        String currentRole = role(authentication);
        if ("ADMIN".equals(currentRole)) {
            mapper.deleteEnrollment(id, null);
        } else if ("STUDENT".equals(currentRole)) {
            mapper.deleteEnrollment(id, ownStudentId(authentication));
        } else {
            throw new BusinessException(403, "没有权限退选课程");
        }
    }

    public List<Map<String, Object>> grades(Authentication authentication) {
        String currentRole = role(authentication);
        return mapper.listGrades("STUDENT".equals(currentRole) ? userId(authentication) : null,
                "TEACHER".equals(currentRole) ? userId(authentication) : null);
    }

    @Transactional
    public Map<String, Object> saveGrade(long enrollmentId, Map<String, Object> body,
                                         Authentication authentication) {
        ensureTeachingAccess(enrollmentId, authentication);
        BigDecimal usual = decimal(body, "usualScore");
        BigDecimal mid = decimal(body, "midtermScore");
        BigDecimal fin = decimal(body, "finalScore");
        validateScore(usual);
        validateScore(mid);
        validateScore(fin);
        BigDecimal total = usual.multiply(new BigDecimal("0.3"))
                .add(mid.multiply(new BigDecimal("0.3")))
                .add(fin.multiply(new BigDecimal("0.4")))
                .setScale(2, RoundingMode.HALF_UP);
        String status = total.compareTo(new BigDecimal("60")) >= 0 ? "合格" : "不合格";
        mapper.saveGrade(enrollmentId, usual, mid, fin, total, status);
        return Map.of("totalScore", total, "gradeStatus", status);
    }

    public List<Map<String, Object>> attendance(Authentication authentication) {
        String currentRole = role(authentication);
        return mapper.listAttendance("STUDENT".equals(currentRole) ? userId(authentication) : null,
                "TEACHER".equals(currentRole) ? userId(authentication) : null);
    }

    @Transactional
    public void saveAttendance(Map<String, Object> body, Authentication authentication) {
        long courseId = requiredLong(body, "courseId");
        ensureCourseAccess(courseId, authentication);
        mapper.saveAttendance(courseId, requiredLong(body, "studentId"),
                LocalDate.parse(required(body, "attendanceDate")), required(body, "status"), body.get("remark"));
    }

    public List<Map<String, Object>> notices(Authentication authentication) {
        return mapper.listNotices(role(authentication));
    }

    @Transactional
    public void createNotice(Map<String, Object> body, Authentication authentication) {
        requireRole(authentication, "ADMIN", "TEACHER");
        mapper.insertNotice(required(body, "title"), required(body, "content"), userId(authentication),
                String.valueOf(body.getOrDefault("targetRole", "ALL")));
    }

    @Transactional
    public void deleteNotice(long id, Authentication authentication) {
        requireAdmin(authentication);
        mapper.deleteNotice(id);
    }

    private void ensureTeachingAccess(long enrollmentId, Authentication authentication) {
        if ("ADMIN".equals(role(authentication))) {
            return;
        }
        if (!"TEACHER".equals(role(authentication))
                || mapper.teacherOwnsEnrollment(enrollmentId, userId(authentication)) == 0) {
            throw new BusinessException(403, "只能维护自己负责课程的成绩");
        }
    }

    private void ensureCourseAccess(long courseId, Authentication authentication) {
        if ("ADMIN".equals(role(authentication))) {
            return;
        }
        if (!"TEACHER".equals(role(authentication))
                || mapper.teacherOwnsCourse(courseId, userId(authentication)) == 0) {
            throw new BusinessException(403, "只能维护自己负责课程的考勤");
        }
    }

    private long ownStudentId(Authentication authentication) {
        Long id = mapper.findStudentIdByUser(userId(authentication));
        if (id == null) {
            throw new BusinessException(404, "未找到学生档案");
        }
        return id;
    }

    private long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    private void requireAdmin(Authentication authentication) {
        requireRole(authentication, "ADMIN");
    }

    private void requireRole(Authentication authentication, String... allowed) {
        if (authentication == null || Arrays.stream(allowed).noneMatch(item -> item.equals(role(authentication)))) {
            throw new BusinessException(403, "没有权限执行此操作");
        }
    }

    private String role(Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(400, key + " 不能为空");
        }
        return String.valueOf(value);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long requiredLong(Map<String, Object> body, String key) {
        return Long.valueOf(required(body, key));
    }

    private Integer requiredInt(Map<String, Object> body, String key) {
        return Integer.valueOf(required(body, key));
    }

    private Long optionalLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer optionalInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(400, "成绩或学分不能为空");
        }
        return new BigDecimal(String.valueOf(value));
    }

    private BigDecimal decimal(Map<String, Object> body, String key) {
        return new BigDecimal(required(body, key));
    }

    private void validateScore(BigDecimal score) {
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(400, "成绩必须在0到100之间");
        }
    }
}
