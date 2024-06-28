package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.dto.*;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.entity.RetiredDeveloper;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.repository.RetiredDeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DMakerService {
    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloperRepository;

    @Transactional
    public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {

        validateCreateDeveloperRequest(request);

        //business logic start
        Developer developer = Developer.builder()
                .developerLevel(request.getDeveloperLevel())
                .developerSkillType(request.getDeveloperSkillType())
                .experienceYears(request.getExperienceYears())
                .memberId(request.getMemberId())
                .statusCode(StatusCode.EMPLOYED)
                .name(request.getName())
                .age(request.getAge())
                .build();

        developerRepository.save(developer);
        return CreateDeveloper.Response.fromEntity(developer);
    }

    private DeveloperValidationDto validateCreateDeveloperRequest(CreateDeveloper.Request request) {
        DeveloperValidationDto developerValidationDto = null;

        // business validation
        developerValidationDto = validateDeveloperLevel(request.getDeveloperLevel(), request.getExperienceYears());

        try {
            if (developerRepository.findByMemberId(request.getMemberId()).isPresent()) {
                developerValidationDto = new DeveloperValidationDto(DUPLICATED_MEMBER_ID,
                        DUPLICATED_MEMBER_ID.getMessage());
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
            developerValidationDto = new DeveloperValidationDto(INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVER_ERROR.getMessage());
        }
//                .ifPresent(developer -> {
//                    throw new DMakerException(DUPLICATED_MEMBER_ID);
//                });

        return developerValidationDto;
    }

    public List<DeveloperDto> getAllEmployedDevelopers() {

        return developerRepository.findDevelopersByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .collect(Collectors.toList());
    }

    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return developerRepository.findByMemberId(memberId)
                .map(DeveloperDetailDto::fromEntity)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
    }

    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {

        validateEditDeveloperRequest(request, memberId);

        Developer developer = developerRepository.findByMemberId(memberId).orElseThrow(
                ()-> new DMakerException((NO_DEVELOPER)));

        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return  DeveloperDetailDto.fromEntity(developer);
    }

    private void validateEditDeveloperRequest(EditDeveloper.Request request, String memberId) {

        validateDeveloperLevel(request.getDeveloperLevel(), request.getExperienceYears());

    }

    private DeveloperValidationDto validateDeveloperLevel(DeveloperLevel developerLevel, Integer experienceYears) {
        if (developerLevel == DeveloperLevel.SENIOR
                && experienceYears < 10) {
            return new DeveloperValidationDto(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED,
                    LEVEL_EXPERIENCE_YEARS_NOT_MATCHED.getMessage());
//            throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
        }
//        if (developerLevel == DeveloperLevel.JUNGNIOR
//                && (experienceYears < 4 || experienceYears > 10)) {
//            throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
//        }
//        if (developerLevel == DeveloperLevel.JUNIOR && experienceYears > 4) {
//            throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
//        }
    }

    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        // 1. EMPLOYED -> RETIRED
        Developer developer = developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
        developer.setStatusCode(StatusCode.RETIRED);

        // 2. save into RetiredDeveloper
        RetiredDeveloper retiredDeveloper = RetiredDeveloper.builder()
                .memberId(memberId)
                .name(developer.getName())
                .build();
        retiredDeveloperRepository.save(retiredDeveloper);
        return  DeveloperDetailDto.fromEntity(developer);
    }
}
