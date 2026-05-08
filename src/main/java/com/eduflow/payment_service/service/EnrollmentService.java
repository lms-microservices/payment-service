package com.eduflow.payment_service.service;

import com.eduflow.payment_service.dto.*;
import com.eduflow.payment_service.entity.Enrollment;
import com.eduflow.payment_service.entity.Enrollment.EnrollmentStatus;
import com.eduflow.payment_service.entity.Payment;
import com.eduflow.payment_service.entity.Payment.PaymentStatus;
import com.eduflow.payment_service.repository.EnrollmentRepository;
import com.eduflow.payment_service.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

   @Autowired
   private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.routing-key.payment-success}")
   private String paymentSuccessRoutingKey;

    private static final Double MOCK_COURSE_PRICE = 49.99;

    /**
     * POST /api/enrollments
     *
     * Flow:
     * 1. Check student is not already enrolled
     * 2. Mock payment — cardNumber ending in "4242" = success, else = declined
     * 3. Save Payment record as SUCCEEDED
     * 4. Save Enrollment record as ACTIVE
     * 5. Publish PaymentSuccessEvent to RabbitMQ
     * 6. Return EnrollmentResponse
     */
    @Transactional
    public EnrollmentResponse enroll(Long studentId, EnrollmentRequest request) {

        // ── Step 1: Check already enrolled ────────────────────────────────────
        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId, request.courseId(), EnrollmentStatus.ACTIVE);

        if (alreadyEnrolled) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You are already enrolled in this course"
            );
        }

        // ── Step 2: Mock payment logic ─────────────────────────────────────────
        // cardNumber ending in "4242" = Visa test card = success
        // anything else = declined
        boolean paymentSuccess = request.cardNumber().endsWith("4242");

        if (!paymentSuccess) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Mock payment declined. Use card number ending in 4242 for success."
            );
        }

        log.info("Mock payment approved | studentId={} courseId={} cardHolder={}",
                studentId, request.courseId(), request.cardHolderName());

        // ── Step 3: Save Payment record ────────────────────────────────────────
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "");

        Payment payment = Payment.builder()
                .paymentIntentId(paymentIntentId)
                .studentId(studentId)
                .courseId(request.courseId())
                .amount(MOCK_COURSE_PRICE)
                .status(PaymentStatus.SUCCEEDED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved | paymentIntentId={}", paymentIntentId);

        // ── Step 4: Save Enrollment record ─────────────────────────────────────
        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(request.courseId())
                .paymentId(savedPayment.getId())
                .paidAmount(MOCK_COURSE_PRICE)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment created | enrollmentId={} studentId={} courseId={}",
                savedEnrollment.getId(), studentId, request.courseId());

        // ── Step 5: Publish PaymentSuccessEvent to RabbitMQ ───────────────────
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                studentId,
              request.courseId(),
               MOCK_COURSE_PRICE
        );
        rabbitTemplate.convertAndSend(paymentExchange, paymentSuccessRoutingKey, event);
        log.info("PaymentSuccessEvent published | routingKey={}", paymentSuccessRoutingKey);

        // ── Step 6: Return response ────────────────────────────────────────────
        return new EnrollmentResponse(
                savedEnrollment.getId(),
                savedEnrollment.getCourseId(),
                savedEnrollment.getStudentId(),
                savedEnrollment.getStatus().name(),
                savedEnrollment.getPaidAmount(),
                savedEnrollment.getEnrolledAt()
        );
    }

    /**
     * GET /api/enrollments/my
     * Returns all active enrollments for the logged-in student.
     */
    public List<EnrollmentResponse> getMyEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(e -> new EnrollmentResponse(
                        e.getId(),
                        e.getCourseId(),
                        e.getStudentId(),
                        e.getStatus().name(),
                        e.getPaidAmount(),
                        e.getEnrolledAt()
                ))
                .toList();
    }

    /**
     * GET /api/enrollments/course/{courseId}
     * Returns all students enrolled in a specific course.
     * For instructor/admin use only — role check is done at the controller level via the Gateway.
     */
    public List<StudentEnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(e -> new StudentEnrollmentResponse(
                        e.getId(),
                        e.getStudentId(),
                        e.getStatus().name(),
                        e.getEnrolledAt()
                ))
                .toList();
    }
}