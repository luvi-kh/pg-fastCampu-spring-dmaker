package com.fastcampus.programming.dmaker.dto;

import com.fastcampus.programming.dmaker.exception.DMakerErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperValidationDto {
    private DMakerErrorCode errorCode;
    private  String errorMessage;
}
