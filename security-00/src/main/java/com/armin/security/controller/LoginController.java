package com.armin.security.controller;

import com.google.code.kaptcha.Producer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;

/**
 * LoginController
 *
 * @author zy
 * @version 2022/5/5
 */
@RestController
@Slf4j
public class LoginController {

    @Autowired
    Producer kaptcha;

    @GetMapping("/permit")
    public String doLogin() {
        return "success";
    }

    @SneakyThrows
    @GetMapping("/vc.png")
    public void verifyCode(HttpServletResponse response, HttpSession session) {
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        String text = kaptcha.createText();
        session.setAttribute("kaptcha", text);
        BufferedImage image = kaptcha.createImage(text);
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(image, "png", outputStream);
    }
}
