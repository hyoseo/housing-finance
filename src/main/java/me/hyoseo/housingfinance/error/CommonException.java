package me.hyoseo.housingfinance.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommonException extends RuntimeException {
    public CommonException(ErrorCode errorCode, Exception internalException) {
        super(internalException != null ? internalException.getMessage() : errorCode.getMessage(), internalException);
        this.errorCode = errorCode;
    }

    public static CommonException create(ErrorCode errorCode) {
        return new CommonException(errorCode, null);
    }

    public static CommonException create(ErrorCode errorCode, String errorMsg) {
        errorCode.setMessage(errorMsg);
        return new CommonException(errorCode, null);
    }

    public static CommonException create(ErrorCode errorCode, Exception internalException) {
        return new CommonException(errorCode, internalException);
    }

    public static CommonException create(ErrorCode errorCode, String errorMsg, Exception internalException) {
        errorCode.setMessage(errorMsg);
        return new CommonException(errorCode, internalException);
    }

    private ErrorCode errorCode;
}
