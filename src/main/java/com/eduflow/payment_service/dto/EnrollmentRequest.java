package com.eduflow.payment_service.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/enrollments
 * Matches the API Contract exactly.
 */

public record EnrollmentRequest(

        @NotNull(message = "courseId is required")
        Long courseId,

        @NotNull(message = "paymentMethod is required")
        String paymentMethod,

        @NotNull(message = "cardHolderName is required")
        String cardHolderName,

        /**
         * Last 4 digits only.
         * Mock logic: ends with "4242" → payment success, anything else → declined.
         */
        @NotNull(message = "cardNumber is required")
        String cardNumber
) {}

