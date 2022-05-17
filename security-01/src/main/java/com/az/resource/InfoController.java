package com.az.resource;

import com.google.code.kaptcha.Producer;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 简单两个接口，实现验证码返回和认证判断
 *
 * @author zy
 * @version 2022/5/16
 */
@RestController
@RequestMapping("/v1/info")
@AllArgsConstructor
public class InfoController {
    private final Producer kaptcha;

    @GetMapping
    public String getInfo() {
        return "hello security";
    }

    @GetMapping("/vc.png")
    public String verifyCode(HttpSession session) throws IOException {
        String text = kaptcha.createText();
        session.setAttribute("kaptcha", text);
        BufferedImage image = kaptcha.createImage(text);

        final FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream();

        ImageIO.write(image, "png", fastByteArrayOutputStream);

        return Base64.encodeBase64String(fastByteArrayOutputStream.toByteArray());
    }
}
