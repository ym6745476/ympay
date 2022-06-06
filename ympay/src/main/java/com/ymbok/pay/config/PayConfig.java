package com.ymbok.pay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config.properties")
//@PropertySource("file:/www/wwwroot/ympay/config/config.properties")
@Data
public class PayConfig {
    @Value("${ali.open_api_domain}")
    private String serverUrl;
    @Value("${ali.appid}")
    private String appId;
    @Value("${ali.private_key}")
    private String privateKey;
    @Value("${ali.alipay_public_key}")
    private String aliPayPublicKey;
    @Value("${ali.sign_type}")
    private String signType;
    @Value("${ali.notify_url}")
    private String notifyUrl;
    @Value("${ali.return_url}")
    private String returnUrl;
    @Value("${ali.format}")
    private String format;
    @Value("${ali.charset}")
    private String charset;
    @Value("${ali.timeout_express}")
    private String timeoutExpress;

    @Value("${out_notify_url}")
    private String outNotifyUrl;

    @Value("${out_sign}")
    private String outSign;

    @Bean
    public AlipayClient getAliPayClient() {
        return new DefaultAlipayClient
            (serverUrl, appId, privateKey, format,
                charset, aliPayPublicKey, signType);
    }
}
