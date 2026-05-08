package com.eduflow.payment_service.dto;

import java.time.LocalDateTime;

/**
 * Response body for POST /api/enrollments (HTTP 201)
 * and items inside GET /api/enrollments/my
 * Matches the API Contract exactly.
 */
public record EnrollmentResponse(
        Long enrollmentId,
        Long courseId,
        Long studentId,
        String status,
        Double paidAmount,
        LocalDateTime enrolledAt
) {}
