package com.student.academic;

import com.student.common.BusinessException;
import com.student.system.entity.StudentEntity;
import com.student.system.mapper.ClassMapper;
import com.student.system.mapper.CourseMapper;
import com.student.system.mapper.DepartmentMapper;
import com.student.system.mapper.StudentMapper;
import com.student.system.mapper.StudentSystemMapper;
import com.student.system.mapper.TeacherMapper;
import com.student.system.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicServiceTest {
    @Mock
    private StudentSystemMapper mapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private ClassMapper classMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private final TestingAuthenticationToken teacher =
            new TestingAuthenticationToken("2", null, "ROLE_TEACHER");
    private final TestingAuthenticationToken admin =
            new TestingAuthenticationToken("1", null, "ROLE_ADMIN");

    @Test
    void teacherGradeUsesConfiguredWeights() {
        when(mapper.teacherOwnsEnrollment(10L, 2L)).thenReturn(1L);
        AcademicService service = service();

        Map<String, Object> result = service.saveGrade(
                10L,
                Map.of("usualScore", 80, "midtermScore", 90, "finalScore", 100),
                teacher);

        assertEquals(new BigDecimal("91.00"), result.get("totalScore"));
        assertEquals("合格", result.get("gradeStatus"));
        verify(mapper).saveGrade(
                eq(10L),
                eq(new BigDecimal("80")),
                eq(new BigDecimal("90")),
                eq(new BigDecimal("100")),
                eq(new BigDecimal("91.00")),
                eq("合格"));
    }

    @Test
    void teacherCannotWriteAnotherTeachersGrade() {
        when(mapper.teacherOwnsEnrollment(10L, 2L)).thenReturn(0L);
        AcademicService service = service();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.saveGrade(
                        10L,
                        Map.of("usualScore", 80, "midtermScore", 90, "finalScore", 100),
                        teacher));

        assertEquals(403, exception.getCode());
        verify(mapper, never()).saveGrade(any(Long.class), any(), any(), any(), any(), any());
    }

    @Test
    void partialStudentUpdatePreservesUnsubmittedFields() {
        StudentEntity existing = new StudentEntity();
        existing.setId(2L);
        existing.setStudentNo("S2025002");
        existing.setName("王同学");
        existing.setDepartmentId(1L);
        existing.setClassId(1L);
        existing.setPhone("13900000002");
        when(studentMapper.selectById(2L)).thenReturn(existing);
        AcademicService service = service();

        service.updateStudent(2L, Map.of("phone", "13900009999"), admin);

        ArgumentCaptor<StudentEntity> captor = ArgumentCaptor.forClass(StudentEntity.class);
        verify(studentMapper).updateById(captor.capture());
        assertEquals("王同学", captor.getValue().getName());
        assertEquals(1L, captor.getValue().getDepartmentId());
        assertEquals(1L, captor.getValue().getClassId());
        assertEquals("13900009999", captor.getValue().getPhone());
    }

    private AcademicService service() {
        return new AcademicService(
                mapper, departmentMapper, classMapper, studentMapper, teacherMapper,
                courseMapper, userMapper, passwordEncoder);
    }
}
