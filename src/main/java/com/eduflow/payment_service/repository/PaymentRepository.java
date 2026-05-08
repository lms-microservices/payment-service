package com.eduflow.payment_service.repository;

import com.eduflow.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    boolean existsByStudentIdAndCourseIdAndStatus(
            Long studentId, Long courseId, Payment.PaymentStatus status);
}
