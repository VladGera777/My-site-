package com.lpi.admissionscommittee.controller;

import com.lpi.admissionscommittee.dto.ApplicantDto;
import com.lpi.admissionscommittee.dto.ScoreDto;
import com.lpi.admissionscommittee.dto.UserDto;
import com.lpi.admissionscommittee.entity.Role;
import com.lpi.admissionscommittee.exceptions.ScoreAlreadyInListException;
import com.lpi.admissionscommittee.service.ApplicantService;
import com.lpi.admissionscommittee.service.ScoreService;
import com.lpi.admissionscommittee.service.SubjectService;
import com.lpi.admissionscommittee.service.UserService;
import com.lpi.admissionscommittee.utils.Constants;
import com.lpi.admissionscommittee.utils.validators.FileValidator;
import com.lpi.admissionscommittee.utils.validators.ParamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/applicant")
public class ApplicantController {
    private final ApplicantService applicantService;
    private final ScoreService scoreService;
    private final UserService userService;
    private final SubjectService subjectService;


    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String viewAccountAndProfile(Model model, @PathVariable("username") String username) {
        UserDto userDto = userService.findByUsername(username);
        if (userDto != null) {
            model.addAttribute(Constants.USER_DTO, userDto);
            Optional<ApplicantDto> applicantDto = applicantService.getByUserId(userDto.getId());
            applicantDto.ifPresent(dto -> addApplicantModel(model, dto));
            return Constants.VIEW_PROFILE;
        }
        return Constants.REDIRECT_HOME;
    }

    @GetMapping("/{username}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String getProfileForm(Model model, @PathVariable("username") String username) {
        UserDto userDto = userService.findByUsername(username);
        if (userDto != null) {
            if (!model.containsAttribute("username")) {
                model.addAttribute("username", userDto.getUsername());
                Optional<ApplicantDto> applicantDto = applicantService.getByUserId(userDto.getId());
                if (userDto.getRole().equals(Role.USER)) {
                    addApplicantModel(model, applicantDto
                            .orElse(ApplicantDto.builder()
                                    .userDetails(userDto)
                                    .requests(new ArrayList<>())
                                    .scores(new ArrayList<>())
                                    .build()));
                }
            }
            return Constants.APPLICANTS_EDIT_PROFILE;
        }
        return Constants.REDIRECT_HOME;
    }


    @RequestMapping(value = "/{username}/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String editProfile(@Valid @ModelAttribute ApplicantDto applicantDTO,
                              BindingResult result,
                              @PathVariable String username,
                              RedirectAttributes redirectAttributes) {
        UserDto userDto = userService.findByUsername(username);
        if (userDto == null) {
            redirectAttributes.addFlashAttribute(Constants.ERROR,
                    "Couldn't find user with given username. Please try again.");
            return Constants.APPLICANTS_EDIT_PROFILE;
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.register", result);
            redirectAttributes.addFlashAttribute(Constants.USER_DTO, userDto);
            return Constants.APPLICANTS_EDIT_PROFILE;
        }
        applicantService.addNewOrEditApplicant(applicantDTO, userDto.getId());
        redirectAttributes.addFlashAttribute(Constants.MESSAGE, "Your changes have been submitted");
        return Constants.REDIRECT_EDIT_APPLICANT;
    }


    @PostMapping("/save-certificate/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String saveCertificate(@ModelAttribute("file") MultipartFile file,
                                  @PathVariable String username, Model model) {
        boolean isValid = FileValidator.validate(file);
        if (!isValid) {
            return Constants.APPLICANTS_EDIT_PROFILE;
        }
        applicantService.saveFile(file, username);
        return Constants.REDIRECT_EDIT_APPLICANT;
    }

    @GetMapping(value = "/download-certificate/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable String username) {
        return applicantService.downloadFile(username);
    }


    @PostMapping("/{username}/new-score")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String addNewScore(@Valid @ModelAttribute("newScore") ScoreDto newScore,
                              @PathVariable String username,
                              BindingResult result,
                              Model model) throws ScoreAlreadyInListException {
        if (result.hasErrors()) {
            return "applicants/editProfile";
        }
        scoreService.addNewScore(newScore);
        return Constants.REDIRECT_EDIT_APPLICANT;
    }


    @RequestMapping(value = "/{username}/edit-score/{id}", method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ROLE_ADMIN') or #username == authentication.principal.username")
    public String editScore(@Valid @ModelAttribute("score") ScoreDto scoreDto,
                            BindingResult result,
                            @PathVariable String username,
                            @PathVariable String id,
                            Model model) throws ScoreAlreadyInListException {
        if (result.hasErrors() || !ParamValidator.isIntegerOrLong(id)) {
            model.addAttribute(Constants.ERROR, "Something went wrong, please try again");
        } else {
            scoreService.editScore(scoreDto, Long.parseLong(id));
        }
        return Constants.REDIRECT_EDIT_APPLICANT;
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAllApplicants(@RequestParam(defaultValue = "1") String page,
                                   @RequestParam(defaultValue = "5") String size,
                                   @RequestParam(defaultValue = "lastName") String sortBy,
                                   Sort.Direction sort, Model model) {
        int pageNumber = ParamValidator.isIntegerOrLong(page) ? Math.max(Integer.parseInt(page), 1) : 1;
        int sizeNumber = ParamValidator.isIntegerOrLong(size) ? Math.max(Integer.parseInt(size), 0) : 5;
        Page<ApplicantDto> applicantDTOPage = applicantService.getAllApplicants(pageNumber, sizeNumber, sort, sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        return addPaginationModel(pageNumber, applicantDTOPage, model);
    }

    @RequestMapping(value = "/{id}/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteApplicant(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (id.matches("(\\d)+")) {
            long idNumber = Long.parseLong(id);
            applicantService.safeDeleteApplicant(idNumber);
            redirectAttributes.addAttribute(Constants.MESSAGE, "Applicant " + idNumber + " has been deleted");
        } else {
            redirectAttributes.addAttribute("error", "Incorrect ID: " + id);
        }
        return Constants.REDIRECT_APPLICANT_ALL;
    }

    private String addPaginationModel(int page, Page<ApplicantDto> paginated, Model model) {
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginated.getTotalPages());
        model.addAttribute("totalItems", paginated.getTotalElements());
        model.addAttribute("applicants", paginated);
        return Constants.ALL_APPLICANTS;
    }

    private void addApplicantModel(Model model, ApplicantDto applicantDTO) {
        model.addAttribute(Constants.APPLICANT_DTO, applicantDTO);
        model.addAttribute(Constants.SCORES, applicantDTO.getScores());
        model.addAttribute(Constants.REQUESTS, applicantDTO.getRequests());
        model.addAttribute("newScore", ScoreDto.builder().applicantId(applicantDTO.getId()).build());
        model.addAttribute("allSubjects", subjectService.getAll());
    }

}
