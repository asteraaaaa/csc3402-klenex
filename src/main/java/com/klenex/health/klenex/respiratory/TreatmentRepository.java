package com.klenex.health.klenex.respiratory;

import com.klenex.health.klenex.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Integer> {
}