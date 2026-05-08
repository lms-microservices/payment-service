package com.eduflow.payment_service.controller;

import com.eduflow.payment_service.dto.EnrollmentRequest;
import com.eduflow.payment_service.dto.EnrollmentResponse;
import com.eduflow.payment_service.dto.StudentEnrollmentResponse;
import com.eduflow.payment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * POST /api/enrollments
     * Student only. Enroll in a course — triggers mock payment flow.
     * studentId is injected by the Gateway via X-User-Id header.
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(
            @RequestHeader("X-User-Id") Long studentId,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        log.info("Enroll request | studentId={} courseId={}", studentId, request.courseId());
        EnrollmentResponse response = enrollmentService.enroll(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/enrollments/my
     * Student only. Returns all courses the logged-in student is enrolled in.
     */
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-User-Id") Long studentId
    ) {
        log.info("Get my enrollments | studentId={}", studentId);
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(studentId));
    }

    /**
     * GET /api/enrollments/course/{courseId}
     * Instructor or Admin only. Get all students enrolled in a specific course.
     * Role enforcement is handled by the API Gateway — this service trusts the header.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudentEnrollmentResponse>> getEnrollmentsByCourse(
            @RequestHeader("X-User-Id") Long requesterId,
            @PathVariable Long courseId
    ) {
        log.info("Get enrollments by course | courseId={} requestedBy={}", courseId, requesterId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }
}
