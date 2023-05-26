package com.lpi.admissionscommittee.service;

import com.lpi.admissionscommittee.dto.SubjectDto;
import com.lpi.admissionscommittee.entity.Faculty;
import com.lpi.admissionscommittee.entity.Subject;
import com.lpi.admissionscommittee.repository.SubjectRepository;
import com.lpi.admissionscommittee.service.mapper.SubjectMapper;
import com.lpi.admissionscommittee.utils.Constants;
import com.lpi.admissionscommittee.utils.validators.ParamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper mapper;
    private final SubjectMapper subjectMapper;

    public SubjectDto addNewSubject(String subjectName) {
        // Check if subject already exists
        if (subjectRepository.findByName(subjectName).isPresent()) {
            throw new IllegalArgumentException("Subject with name: " + subjectName + " already exists!");
        }
        Subject subject = subjectRepository.save(Subject.builder().name(subjectName).build());
        return mapper.mapToDto(subject);
    }

    public Subject editSubjectName(SubjectDto dto, Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        subject.setName(dto.getName());
        return subjectRepository.save(subject);
    }

    public Subject getById(String id) {
        if (ParamValidator.isIntegerOrLong(id)) {
            return subjectRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new IllegalArgumentException(Constants.COULD_NOT_FIND_SUBJECT+ id));
        } else {
            throw new IllegalArgumentException(Constants.COULD_NOT_FIND_SUBJECT + id);
        }
    }

    public void deleteSubject(Long id) {
        // Check if subject belongs to any faculty - if so, remove connections before deleting
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(Constants.COULD_NOT_FIND_SUBJECT + id));
        List<Faculty> faculties = subject.getFaculties();
        if (!faculties.isEmpty()) {
            faculties.clear();
        }
        subjectRepository.deleteById(id);
    }

    public Set<SubjectDto> getAllForFaculty(Long facultyId) {
        Set<Subject> subjects = subjectRepository.findByFaculties_id(facultyId);
        return subjectMapper.mapToDto(subjects);
    }

    public Set<SubjectDto> getAll() {
        Set<Subject> subjects = new HashSet<>(subjectRepository.findAll());
        return subjectMapper.mapToDto(subjects);
    }
}
