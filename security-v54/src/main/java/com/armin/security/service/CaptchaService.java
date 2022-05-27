package com.armin.security.service;

/**
 * CaptchaService
 *
 * @author zy
 * @version 2022/5/27
 */
public interface CaptchaService {
    boolean sendCaptcha(String phone);

    boolean verifyCaptcha(String phone, String code);
}
