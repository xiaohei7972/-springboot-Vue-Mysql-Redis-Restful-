package com.student.dashboard;

import com.student.common.BusinessException;
import com.student.system.mapper.StudentSystemMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final StudentSystemMapper mapper;

    public DashboardService(StudentSystemMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> summary(Authentication authentication) {
        String currentRole = role(authentication);
        Map<String, Object> result = new LinkedHashMap<>();
        if ("ADMIN".equals(currentRole)) {
            result.put("studentCount", mapper.countStudentsAll());
            result.put("teacherCount", mapper.countTeachers());
            result.put("courseCount", mapper.countCourses());
            result.put("departmentCount", mapper.countDepartments());
        } else if ("TEACHER".equals(currentRole)) {
            long id = userId(authentication);
            result.put("courseCount", mapper.countTeacherCourses(id));
            result.put("studentCount", mapper.countTeacherStudents(id));
            result.put("gradeCount", mapper.countTeacherGrades(id));
            result.put("attendanceCount", mapper.countTeacherAttendance(id));
        } else if ("STUDENT".equals(currentRole)) {
            Long studentId = mapper.findStudentIdByUser(userId(authentication));
            if (studentId == null) {
                throw new BusinessException(404, "未找到学生档案");
            }
            result.put("courseCount", mapper.countStudentCourses(studentId));
            result.put("gradeCount", mapper.countStudentGrades(studentId));
            result.put("attendanceCount", mapper.countStudentAttendance(studentId));
            result.put("noticeCount", mapper.countStudentNotices());
        } else {
            throw new BusinessException(403, "没有权限查看首页统计");
        }
        result.put("recentNotices", mapper.recentNotices(currentRole));
        return result;
    }

    private long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    private String role(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }
}
