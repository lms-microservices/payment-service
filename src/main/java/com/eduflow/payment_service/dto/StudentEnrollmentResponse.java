package com.eduflow.payment_service.dto;

import java.time.LocalDateTime;

/**
 * Response item for GET /api/enrollments/course/{courseId}
 * Used by instructor/admin to see who is enrolled in their course.
 */
public record StudentEnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        String status,
        LocalDateTime enrolledAt
) {}
