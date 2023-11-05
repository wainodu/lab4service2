package org.wainodu.mythirdtestappspringboot.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wainodu.mythirdtestappspringboot.exception.UnsupportedCodeException;
import org.wainodu.mythirdtestappspringboot.exception.ValidationFailedException;
import org.wainodu.mythirdtestappspringboot.model.*;
import org.wainodu.mythirdtestappspringboot.service.ModifyResponseService;
import org.wainodu.mythirdtestappspringboot.service.UnsupportedCodeValidationService;
import org.wainodu.mythirdtestappspringboot.service.ValidationService;
import org.wainodu.mythirdtestappspringboot.util.DateTimeUtil;


import java.util.Date;

@Slf4j
@RestController
public class MyController {
    private final ValidationService validationService;
    private final UnsupportedCodeValidationService unsupportedCodeValidationService;
    private final ModifyResponseService modifyResponseService;

    @Autowired
    public MyController(ValidationService validationService, UnsupportedCodeValidationService unsupportedCodeValidationService,
                        @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService) {
        this.validationService = validationService;
        this.unsupportedCodeValidationService = unsupportedCodeValidationService;
        this.modifyResponseService = modifyResponseService;
    }

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request, BindingResult bindingResult) {

        log.info("request: {}", request);

        Response response = Response.builder()
                .uid(request.getUid())
                .operationUid(request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();

        log.info("response: {}", response);

        try {
            unsupportedCodeValidationService.isValid(Integer.parseInt(request.getUid()));
        } catch (UnsupportedCodeException e) {
            log.error("request: {}", request);
            log.error("response: {}", response);
            log.error("bindingResultError: {}", bindingResult.getFieldError().getDefaultMessage());
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNSUPPORTED_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNSUPPORTED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            validationService.isValid(bindingResult);
        } catch (ValidationFailedException e) {
            log.error("request: {}", request);
            log.error("response: {}", response);
            log.error("bindingResultError: {}", bindingResult.getFieldError().getDefaultMessage());
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.VALIDATION_EXCEPTION);
            response.setErrorMessage(ErrorMessages.VALIDATION);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("request: {}", request);
            log.error("response: {}", response);
            log.error("bindingResultError: {}", bindingResult.getFieldError().getDefaultMessage());
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNKNOWN_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNKNOWN);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        modifyResponseService.modify(response);
        log.info("time spent: {} milliseconds", System.currentTimeMillis() - request.getCurrentTime());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
