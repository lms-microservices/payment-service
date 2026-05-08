package com.eduflow.payment_service.repository;

import com.eduflow.payment_service.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** Used to check if student is already enrolled before allowing a new enrollment */
    boolean existsByStudentIdAndCourseIdAndStatus(
            Long studentId, Long courseId, Enrollment.EnrollmentStatus status);

    /** Powers GET /api/enrollments/my — all enrollments for a specific student */
    List<Enrollment> findByStudentId(Long studentId);

    /** Powers GET /api/enrollments/course/{courseId} — all students in a course */
    List<Enrollment> findByCourseId(Long courseId);
}
