package com.student.academic;

import com.student.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AcademicController {
    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    @GetMapping("/departments")
    public ApiResponse<?> departments(Authentication authentication) {
        return ApiResponse.ok(academicService.departments(authentication));
    }

    @PostMapping("/departments")
    public ApiResponse<Void> createDepartment(@RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        academicService.createDepartment(body, authentication);
        return ApiResponse.ok();
    }

    @PutMapping("/departments/{id}")
    public ApiResponse<Void> updateDepartment(@PathVariable long id, @RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        academicService.updateDepartment(id, body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/departments/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable long id, Authentication authentication) {
        academicService.deleteDepartment(id, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/classes")
    public ApiResponse<?> classes(Authentication authentication) {
        return ApiResponse.ok(academicService.classes(authentication));
    }

    @PostMapping("/classes")
    public ApiResponse<Void> createClass(@RequestBody Map<String, Object> body,
                                         Authentication authentication) {
        academicService.createClass(body, authentication);
        return ApiResponse.ok();
    }

    @PutMapping("/classes/{id}")
    public ApiResponse<Void> updateClass(@PathVariable long id, @RequestBody Map<String, Object> body,
                                         Authentication authentication) {
        academicService.updateClass(id, body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/classes/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable long id, Authentication authentication) {
        academicService.deleteClass(id, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/students")
    public ApiResponse<?> students(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword,
                                   Authentication authentication) {
        return ApiResponse.ok(academicService.students(page, size, keyword, authentication));
    }

    @PostMapping("/students")
    public ApiResponse<Void> createStudent(@RequestBody Map<String, Object> body,
                                            Authentication authentication) {
        academicService.createStudent(body, authentication);
        return ApiResponse.ok();
    }

    @PutMapping("/students/{id}")
    public ApiResponse<Void> updateStudent(@PathVariable long id, @RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        academicService.updateStudent(id, body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/students/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable long id, Authentication authentication) {
        academicService.deleteStudent(id, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/teachers")
    public ApiResponse<?> teachers(Authentication authentication) {
        return ApiResponse.ok(academicService.teachers(authentication));
    }

    @GetMapping("/courses")
    public ApiResponse<?> courses(Authentication authentication) {
        return ApiResponse.ok(academicService.courses(authentication));
    }

    @PostMapping("/courses")
    public ApiResponse<Void> createCourse(@RequestBody Map<String, Object> body,
                                          Authentication authentication) {
        academicService.createCourse(body, authentication);
        return ApiResponse.ok();
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Void> updateCourse(@PathVariable long id, @RequestBody Map<String, Object> body,
                                          Authentication authentication) {
        academicService.updateCourse(id, body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable long id, Authentication authentication) {
        academicService.deleteCourse(id, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/enrollments")
    public ApiResponse<?> enrollments(@RequestParam(required = false) Long courseId,
                                      Authentication authentication) {
        return ApiResponse.ok(academicService.enrollments(courseId, authentication));
    }

    @PostMapping("/enrollments")
    public ApiResponse<Void> createEnrollment(@RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        academicService.createEnrollment(body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/enrollments/{id}")
    public ApiResponse<Void> deleteEnrollment(@PathVariable long id, Authentication authentication) {
        academicService.deleteEnrollment(id, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/grades")
    public ApiResponse<?> grades(Authentication authentication) {
        return ApiResponse.ok(academicService.grades(authentication));
    }

    @PutMapping("/grades/{enrollmentId}")
    public ApiResponse<?> saveGrade(@PathVariable long enrollmentId, @RequestBody Map<String, Object> body,
                                    Authentication authentication) {
        return ApiResponse.ok(academicService.saveGrade(enrollmentId, body, authentication));
    }

    @GetMapping("/attendance")
    public ApiResponse<?> attendance(Authentication authentication) {
        return ApiResponse.ok(academicService.attendance(authentication));
    }

    @PutMapping("/attendance")
    public ApiResponse<Void> saveAttendance(@RequestBody Map<String, Object> body,
                                             Authentication authentication) {
        academicService.saveAttendance(body, authentication);
        return ApiResponse.ok();
    }

    @GetMapping("/notices")
    public ApiResponse<?> notices(Authentication authentication) {
        return ApiResponse.ok(academicService.notices(authentication));
    }

    @PostMapping("/notices")
    public ApiResponse<Void> createNotice(@RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        academicService.createNotice(body, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/notices/{id}")
    public ApiResponse<Void> deleteNotice(@PathVariable long id, Authentication authentication) {
        academicService.deleteNotice(id, authentication);
        return ApiResponse.ok();
    }
}
