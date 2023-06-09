package com.lpi.admissionscommittee.repository;

import com.lpi.admissionscommittee.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    public Set<Subject> findByFaculties_id(Long facultyId);
    Optional<Subject> findByName(String subjectName);

}