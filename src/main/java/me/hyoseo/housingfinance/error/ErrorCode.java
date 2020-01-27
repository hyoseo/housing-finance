package me.hyoseo.housingfinance.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NOT_FOUND(10, "data does not exist", HttpStatus.NOT_FOUND),
    BAD_REQUEST(20, "invalid argument", HttpStatus.BAD_REQUEST),
    BAD_REQUEST_DATA(21, "invalid data format", HttpStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(40, "no permission", HttpStatus.UNAUTHORIZED),
    SERVICE_UNAVAILABLE(80, "service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_SERVER_ERROR(90, "external server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(98, "internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    ID_OR_PASSWORD_IS_WRONG(300, "Id or Password is wrong", HttpStatus.NOT_FOUND),
    ALREADY_EXIST_ID(300, "The Id is already exists", HttpStatus.BAD_REQUEST),

    UNKNOWN_ERROR(999, "unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    private Integer code;
    private String message;
    private HttpStatus httpStatus;

    public void setMessage(String message) {
        this.message = message;
    }
}