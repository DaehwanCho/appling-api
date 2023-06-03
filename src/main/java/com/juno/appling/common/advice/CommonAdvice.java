package com.juno.appling.common.advice;

import com.juno.appling.domain.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class CommonAdvice {
    @Value("${docs}")
    private String docs;

    private final String ERRORS = "errors";


    @ExceptionHandler
    public ResponseEntity<ProblemDetail> illegalArgumentException(IllegalArgumentException e, HttpServletRequest request){
        List<ErrorDto> errors = new ArrayList<>();
        errors.add(ErrorDto.builder().point("").detail(e.getMessage()).build());

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()), "입력 값을 확인해주세요.");
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setType(URI.create(docs));
        pb.setTitle(HttpStatus.BAD_REQUEST.name());
        pb.setProperty(ERRORS, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(pb);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request){
        List<ErrorDto> errors = new ArrayList<>();
        errors.add(ErrorDto.builder().point(e.getParameterName()).detail(String.format("please check parameter : %s (%s)", e.getParameterName(), e.getParameterType())).build());

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()), "파라미터 값을 확인해주세요.");
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setType(URI.create(docs));
        pb.setTitle(HttpStatus.BAD_REQUEST.name());
        pb.setProperty(ERRORS, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(pb);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request){
        List<ErrorDto> errors = new ArrayList<>();
        errors.add(ErrorDto.builder().point("").detail("NOT FOUND").build());

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "URL을 찾을 수 없습니다.");
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setType(URI.create(docs));
        pb.setTitle(HttpStatus.NOT_FOUND.name());
        pb.setProperty(ERRORS, errors);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(pb);
    }
}
