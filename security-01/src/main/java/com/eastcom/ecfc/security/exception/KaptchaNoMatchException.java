package com.eastcom.ecfc.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * KaptchaException
 *
 * @author zy
 * @version 2022/5/16
 */
public class KaptchaNoMatchException extends AuthenticationException {
    // 这是认证异常, 对应的还有授权异常: AccessDeniedException
    public KaptchaNoMatchException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KaptchaNoMatchException(String msg) {
        super(msg);
    }
}
