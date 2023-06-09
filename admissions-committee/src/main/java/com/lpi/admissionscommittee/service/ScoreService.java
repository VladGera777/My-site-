package com.lpi.admissionscommittee.service;

import com.lpi.admissionscommittee.dto.ScoreDto;
import com.lpi.admissionscommittee.entity.Applicant;
import com.lpi.admissionscommittee.entity.Score;
import com.lpi.admissionscommittee.exceptions.NoSuchApplicantException;
import com.lpi.admissionscommittee.exceptions.ScoreAlreadyInListException;
import com.lpi.admissionscommittee.repository.ApplicantRepository;
import com.lpi.admissionscommittee.repository.ScoreRepository;
import com.lpi.admissionscommittee.service.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreMapper mapper;
    private final ApplicantRepository applicantRepository;

    public void addNewScore(ScoreDto scoreDTO) throws ScoreAlreadyInListException {
        Score newScore = mapper.mapToEntity(scoreDTO);
        setApplicantToScoreEntity(scoreDTO, newScore);
        scoreRepository.save(newScore);
    }

    private void setApplicantToScoreEntity(ScoreDto scoreDTO, Score newScore) throws ScoreAlreadyInListException {
        Long applicantId = scoreDTO.getApplicantId();
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new NoSuchApplicantException("Could not find applicant with ID: " + applicantId));
        List<String> applicantScores = applicant.getScores().stream()
                .map(Score::getSubjectName)
                .collect(Collectors.toList());
        if (applicantScores.contains(newScore.getSubjectName())) {
            throw new ScoreAlreadyInListException();
        }
        newScore.setApplicant(applicant);
    }

    public List<ScoreDto> getAllForApplicantId(Long applicantID) {
        List<Score> scores = scoreRepository.findByApplicantId(applicantID);
        return scores.stream().map(mapper::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void editScore(ScoreDto scoreDTO, Long id) throws ScoreAlreadyInListException {
        Score score = scoreRepository.getReferenceById(id);
        mapper.mapToEntity(score, scoreDTO);
        setApplicantToScoreEntity(scoreDTO, score);
        scoreRepository.save(score);
    }

    public void deleteScore(Long id) {
        scoreRepository.deleteById(id);
    }
}
