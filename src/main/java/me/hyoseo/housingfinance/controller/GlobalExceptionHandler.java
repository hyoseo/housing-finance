package me.hyoseo.housingfinance.controller;

import lombok.extern.slf4j.Slf4j;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.error.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorMessage> handleException(HttpServletRequest req, CommonException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("ERROR_SOURCE : " + ex.getStackTrace()[1].toString());
        printError(req, ex.getCause() != null ? ex.getCause() : ex, errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorMessage(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(HttpServletRequest req, Exception ex) {
        log.error("ERROR_SOURCE : " + ex.getStackTrace()[0].toString());
        printError(req, ex, ErrorCode.UNKNOWN_ERROR);

        return ResponseEntity
                .status(ErrorCode.UNKNOWN_ERROR.getHttpStatus())
                .body(new ErrorMessage(ErrorCode.UNKNOWN_ERROR.getCode(), ErrorCode.UNKNOWN_ERROR.getMessage()));
    }

    public void printError(HttpServletRequest req, Throwable ex, ErrorCode code) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("ERROR_INFO : ")
                .append(req.getMethod()).append(" ").append(req.getRequestURI()).append(", Access-Token : ")
                .append(req.getHeader("Access-Token")).append(" [").append(ex.toString()).append("][")
                .append(ex.getMessage()).append("][").append(code.getMessage()).append("]");
        log.error(errorInfo.toString());
    }
}
