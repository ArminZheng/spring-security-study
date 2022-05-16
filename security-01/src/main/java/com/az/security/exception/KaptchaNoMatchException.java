package com.az.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * KaptchaException
 *
 * @author zy
 * @version 2022/5/16
 */
public class KaptchaNoMatchException extends AuthenticationException {

    public KaptchaNoMatchException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KaptchaNoMatchException(String msg) {
        super(msg);
    }

}
