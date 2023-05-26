package com.lpi.admissionscommittee.service.mapper;


import com.lpi.admissionscommittee.annotations.Mapper;
import com.lpi.admissionscommittee.dto.ApplicantDto;
import com.lpi.admissionscommittee.dto.EnrollmentRequestDto;
import com.lpi.admissionscommittee.dto.FacultyDto;
import com.lpi.admissionscommittee.dto.other.RequestWithNamesDto;
import com.lpi.admissionscommittee.entity.Applicant;
import com.lpi.admissionscommittee.entity.EnrollmentRequest;
import com.lpi.admissionscommittee.entity.Faculty;
import com.lpi.admissionscommittee.service.ScoreService;
import com.lpi.admissionscommittee.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Mapper
public class EnrollmentRequestMapper {
    private final ApplicantMapper applicantMapper;
    private final FacultyMapper facultyMapper;
    private final ScoreService scoreService;
    private final SubjectService subjectService;

    public EnrollmentRequestDto mapToDto(EnrollmentRequest enrollmentRequest) {
        log.debug("EnrollmentRequest entity before mapping: {}", enrollmentRequest);
        ApplicantDto applicantDto = applicantMapper.mapToDto(enrollmentRequest.getApplicant());
        applicantDto.setScores(scoreService.getAllForApplicantId(applicantDto.getId()));
        FacultyDto facultyDto = facultyMapper.mapToDto(enrollmentRequest.getFaculty());
        facultyDto.setSubjects(subjectService.getAllForFaculty(facultyDto.getId()));
        return EnrollmentRequestDto.builder()
                .id(enrollmentRequest.getId())
                .points(enrollmentRequest.getPoints())
                .registrationDate(enrollmentRequest.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")))
                .status(enrollmentRequest.getStatus())
                .tempStatus(enrollmentRequest.getTempStatus())
                .applicant(applicantDto)
                .faculty(facultyDto).build();
    }

    public List<EnrollmentRequestDto> mapToDto(List<EnrollmentRequest> enrollmentRequests) {
        log.debug("Mapping List<EnrollmentRequest> to DTOs");
        return enrollmentRequests.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public EnrollmentRequest mapToEntity(EnrollmentRequestDto enrollmentRequestDTO) {
        log.debug("EnrollmentRequest Dto before mapping: {}", enrollmentRequestDTO);
        return EnrollmentRequest.builder()
                .points(enrollmentRequestDTO.getPoints())
                .status(enrollmentRequestDTO.getStatus())
                .tempStatus(enrollmentRequestDTO.getTempStatus())
                .applicant(applicantMapper.mapToEntityWithId(enrollmentRequestDTO.getApplicant()))
                .faculty(facultyMapper.mapToEntityWithId(enrollmentRequestDTO.getFaculty())).build();
    }

    public RequestWithNamesDto mapToDtoWithNames(EnrollmentRequest request) {
        Applicant applicant = request.getApplicant();
        Faculty faculty = request.getFaculty();
        return RequestWithNamesDto.builder()
                .faculty(faculty.getName())
                .applicant(applicant.getLastName() + " " + applicant.getFirstName())
                .id(request.getId())
                .points(request.getPoints())
                .date(request.getRegistrationDate())
                .status(request.getStatus())
                .build();
    }
}
