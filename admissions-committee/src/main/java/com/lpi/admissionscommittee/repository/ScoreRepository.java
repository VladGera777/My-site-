package com.lpi.admissionscommittee.repository;

import com.lpi.admissionscommittee.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByApplicantId(Long applicantId);
}
