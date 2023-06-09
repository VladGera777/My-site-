package com.lpi.admissionscommittee.service.mapper;

import com.lpi.admissionscommittee.annotations.Mapper;
import com.lpi.admissionscommittee.dto.SubjectDto;
import com.lpi.admissionscommittee.entity.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Mapper
public class SubjectMapper {

    private final FacultyMapper facultyMapper;

    public SubjectDto mapToDto (Subject subject) {
        log.debug("Subject entity before mapping: {}", subject);
         SubjectDto dto = SubjectDto.builder()
                 .id(subject.getId())
                 .name(subject.getName()).build();
         if(subject.getFaculties() != null) {
            dto.setFaculties(facultyMapper.mapToDto(subject.getFaculties()));
         }
         return dto;
    }

    public Set<SubjectDto> mapToDto (Set<Subject> subjects) {
        log.debug("Mapping a set of subjects to DTO");
        return subjects.stream().map(this::mapToDto).collect(Collectors.toSet());
    }
}
