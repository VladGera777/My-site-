package com.lpi.admissionscommittee.repository;

import com.lpi.admissionscommittee.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Faculty findByName(String name);
}
