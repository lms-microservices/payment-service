package com.eduflow.payment_service.dto;



/**
 * RabbitMQ event published to payment.exchange with routing key payment.success
 * Consumed by enrollment-service (and any other interested service).
 */
public record PaymentSuccessEvent(
        Long studentId,
        Long courseId,
        Double amount
) {}
