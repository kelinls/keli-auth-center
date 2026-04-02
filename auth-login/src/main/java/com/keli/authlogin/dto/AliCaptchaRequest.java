package com.keli.authlogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliCaptchaRequest {
    String sceneId;
    String captchaVerifyParam;
}
