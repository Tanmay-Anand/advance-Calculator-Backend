package com.tanmay.advance_Calculator_Backend.repository;

import com.tanmay.advance_Calculator_Backend.domain.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {

    // Find all calculations by user ID and type, ordered by timestamp descending
    List<Calculation> findByUserIdAndTypeOrderByTimestampDesc(
            Long userId,
            Calculation.CalculationType type
    );

    // Delete all calculations by user ID and type
    void deleteByUserIdAndType(Long userId, Calculation.CalculationType type);

    // Find a specific calculation by ID and user ID (for security)
    Calculation findByIdAndUserId(Long id, Long userId);
}
