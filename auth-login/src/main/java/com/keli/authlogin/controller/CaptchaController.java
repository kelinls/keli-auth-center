package com.keli.authlogin.controller;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import com.keli.authlogin.dto.AliCaptchaRequest;
import com.keli.authlogin.dto.AliCaptchaResponse;
import com.keli.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class CaptchaController {
    @Autowired
    private Client client;

    @PostMapping("/alicaptcha")
    public R aliCaptcha(@RequestBody AliCaptchaRequest request) throws Exception {
      VerifyIntelligentCaptchaRequest verifyIntelligentCaptchaRequest = new VerifyIntelligentCaptchaRequest()
              .setSceneId(request.getSceneId())
             .setCaptchaVerifyParam(request.getCaptchaVerifyParam());

        try {
            VerifyIntelligentCaptchaResponse verifyIntelligentCaptchaResponse = client.verifyIntelligentCaptchaWithOptions(verifyIntelligentCaptchaRequest, new RuntimeOptions());
            log.info(new Gson().toJson(verifyIntelligentCaptchaResponse.body.result));

            return  R.success(AliCaptchaResponse.builder()
                    .bizResult(verifyIntelligentCaptchaResponse.body.result.verifyResult)
                    .build());

        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        }
        return R.error(null);
    }
}
