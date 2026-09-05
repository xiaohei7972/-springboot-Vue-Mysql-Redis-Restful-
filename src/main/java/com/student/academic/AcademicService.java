package com.student.academic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.student.common.BusinessException;
import com.student.common.PageResult;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.StudentSystemMapper;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AcademicService(StudentSystemMapper mapper, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
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
        mapper.insertDepartment(required(body, "name"), required(body, "code"), body.get("description"));
    }

    @Transactional
    public void updateDepartment(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        mapper.updateDepartment(id, required(body, "name"), required(body, "code"), body.get("description"));
    }

    @Transactional
    public void deleteDepartment(long id, Authentication authentication) {
        requireAdmin(authentication);
        mapper.deleteDepartment(id);
    }

    public List<Map<String, Object>> classes(Authentication authentication) {
        requireAdmin(authentication);
        return mapper.listClasses();
    }

    @Transactional
    public void createClass(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        mapper.insertClass(required(body, "name"), required(body, "code"), requiredLong(body, "departmentId"),
                requiredInt(body, "gradeYear"));
    }

    @Transactional
    public void updateClass(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        mapper.updateClass(id, required(body, "name"), required(body, "code"), requiredLong(body, "departmentId"),
                requiredInt(body, "gradeYear"));
    }

    @Transactional
    public void deleteClass(long id, Authentication authentication) {
        requireAdmin(authentication);
        mapper.deleteClass(id);
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
        body.putIfAbsent("status", "在读");
        mapper.insertStudent(body);
    }

    @Transactional
    public void updateStudent(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        body.put("id", id);
        body.putIfAbsent("status", "在读");
        mapper.updateStudent(body);
    }

    @Transactional
    public void deleteStudent(long id, Authentication authentication) {
        requireAdmin(authentication);
        mapper.deleteStudent(id);
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

        body.put("userId", account.getId());
        body.put("name", name);
        mapper.insertTeacher(body);
    }

    @Transactional
    public void updateTeacher(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        body.put("id", id);
        mapper.updateTeacher(body);
        Long userId = mapper.findTeacherUserId(id);
        if (userId != null) {
            UserEntity account = userMapper.selectById(userId);
            if (account != null) {
                account.setRealName(required(body, "name").trim());
                account.setStatus("0".equals(String.valueOf(body.get("userStatus"))) ? "0" : "1");
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
        Long userId = mapper.findTeacherUserId(id);
        mapper.deleteTeacher(id);
        if (userId != null) {
            userMapper.deleteById(userId);
        }
    }

    public List<Map<String, Object>> courses(Authentication authentication) {
        Long teacherUserId = "TEACHER".equals(role(authentication)) ? userId(authentication) : null;
        return mapper.listCourses(teacherUserId);
    }

    @Transactional
    public void createCourse(Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        body.putIfAbsent("credit", 2);
        body.putIfAbsent("hours", 32);
        mapper.insertCourse(body);
    }

    @Transactional
    public void updateCourse(long id, Map<String, Object> body, Authentication authentication) {
        requireAdmin(authentication);
        body.put("id", id);
        body.putIfAbsent("credit", 2);
        body.putIfAbsent("hours", 32);
        mapper.updateCourse(body);
    }

    @Transactional
    public void deleteCourse(long id, Authentication authentication) {
        requireAdmin(authentication);
        mapper.deleteCourse(id);
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

    private Long requiredLong(Map<String, Object> body, String key) {
        return Long.valueOf(required(body, key));
    }

    private Integer requiredInt(Map<String, Object> body, String key) {
        return Integer.valueOf(required(body, key));
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
