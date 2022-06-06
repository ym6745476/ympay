package com.ymbok.pay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.google.gson.Gson;
import com.ymbok.pay.config.PayConfig;
import com.ymbok.pay.dao.PayDao;
import com.ymbok.pay.domain.PayEntity;
import com.ymbok.pay.domain.alipay.PayContent;
import com.ymbok.pay.domain.alipay.PayStatus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@Service
@Log4j2
public class AliPayOrderServiceImpl implements AliPayOrderService {

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private AlipayClient alipayClient;

    private Gson gson = new Gson();

    @Autowired
    private PayDao payDao;

    @Override
    public AlipayTradePrecreateResponse createOrderPayQrCode(String userId,String userName,String roleId,String roleName,
                                                             String serverId,String subject,String appId,String goodsId,String totalAmount) {

        //购买商品2件共15.00元
        String body = userName + "购买" + subject + "共" + totalAmount + "元";

        //创建API对应的request类
        //用户ID后4位
        String userIdTemp = String.valueOf(System.currentTimeMillis()) + userId;
        String outTradeNoTail = userIdTemp.substring(userIdTemp.length()-4,userIdTemp.length());
        String outTradeNo = String.valueOf(System.currentTimeMillis()) + outTradeNoTail;
        PayContent payContent = new PayContent();
        payContent.setOutTradeNo(outTradeNo);
        payContent.setStoreId("");
        payContent.setSubject(subject);
        payContent.setTimeoutExpress(payConfig.getTimeoutExpress());
        payContent.setTotalAmount(totalAmount);

        payContent.setBody(body);
        payContent.setOperatorId("");
        payContent.setTerminalId(outTradeNo);
        String toString = gson.toJson(payContent);
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(payConfig.getNotifyUrl());
        request.setReturnUrl(payConfig.getReturnUrl());
        //订单允许的最晚付款时间
        request.setBizContent(toString);
        //打印
        System.out.println("生成付款码信息:");
        System.out.println(request.getBizContent());
        System.out.println(request.getNotifyUrl());
        System.out.println(request.getReturnUrl());
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            System.out.println(response.getCode());
            if (response.isSuccess()) {
                //将待支付的交易记录保存
                PayEntity payEntity = new PayEntity();
                payEntity.setUserId(userId);
                payEntity.setUserName(userName);
                payEntity.setSubject(subject);
                payEntity.setAppId(appId);
                payEntity.setGoodsId(goodsId);
                payEntity.setRoleId(roleId);
                payEntity.setRoleName(roleName);
                payEntity.setServerId(serverId);

                payEntity.setOutTradeNo(response.getOutTradeNo());
                payEntity.setTotalAmount(totalAmount);
                payEntity.setTradeStatus("WAIT_BUYER_PAY");
                payEntity.setTradeNo("");
                payEntity.setBuyerLogonId("");
                payEntity.setBuyerPayAmount("");
                payEntity.setBody(response.getBody());
                payDao.savePay(payEntity);

                payContent.setTradeStatus("WAIT_BUYER_PAY");

            } else {
                log.error("生成付款码失败");
                throw new RuntimeException("生成付款码失败");
            }
            return response;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public AlipayTradeQueryResponse getTradesQuery(String outTradeNo,String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        if (StringUtils.isNotEmpty(tradeNo)) {
            request.setBizContent("{" +
                    "    \"trade_no\":\"" + tradeNo + "\" }");
        } else if (StringUtils.isNotEmpty(outTradeNo)) {
            request.setBizContent("{" +
                    "    \"out_trade_no\":\"" + outTradeNo + "\" }");
        } else {
            throw new RuntimeException("外部订单号或支付宝订单号不能同时为空");
        }

        try {

            log.debug("查询支付宝订单参数:{}", gson.toJson(request.getBizContent()));
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            log.debug("查询支付宝订单结果:{}", gson.toJson(response.getBody()));
            if (response.isSuccess()) {

                //将查询结果插入到交易表
                PayEntity payEntity = new PayEntity();
                payEntity.setOutTradeNo(response.getOutTradeNo());
                payEntity.setTotalAmount(response.getTotalAmount());
                payEntity.setTradeStatus(response.getTradeStatus());
                payEntity.setTradeNo(response.getTradeNo());
                payEntity.setBuyerLogonId(response.getBuyerLogonId());
                payEntity.setBuyerPayAmount(response.getBuyerPayAmount());
                payEntity.setBody(response.getBody());
                payDao.savePay(payEntity);
                return response;
            } else {
                log.error("支付宝订单("+ outTradeNo + ")状态：等待用户支付...");
                return response;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String tradeCancelQrCodeByOutTradeNo(String outTradeNo) {
        //查询支付宝中是否存在这笔订单
        AlipayTradeQueryResponse queryResponse = getTradesQuery(outTradeNo,null);
        //取消支付宝侧订单
        if (queryResponse.isSuccess()) {
            tradeCancelByOutTradeNo(outTradeNo);
        }
        //更新交易表
        payDao.cancelPay(outTradeNo);
        return "success";
    }

    @Override
    public boolean tradeCancelByOutTradeNo(String outTradeNo) {
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizContent("{" +
            "    \"out_trade_no\":\"" + outTradeNo + "\" }");
        try {
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return true;
            } else {
                log.error("不支持的交易状态，交易返回异常!!!");
                return false;
            }
        } catch (Exception e) {
            log.error("不支持的交易状态，交易返回异常!!!,原因:{}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean tradeClose(String outTradeNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + outTradeNo + "\" }");
        try {
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void payCallBack(HttpServletRequest request) {
        Map<String, String[]> parmMap = request.getParameterMap();
        Map<String, String> paMap = getParams(parmMap);

        log.debug("接受到回调信息:{},开始处理相关业务", gson.toJson(parmMap));

        boolean verify = verifyAliAsyncCallBackParams(paMap);
        if (!verify) {
            log.error("支付宝支付回调发现异常回调,请立即排查!!!");
            return;
        }
        String tradeStatus = paMap.get("trade_status");
        if (PayStatus.WAIT_BUYER_PAY.getValue().equals(tradeStatus)) {
            log.debug("交易待支付业务");
        } else if (tradeStatus.equals(PayStatus.TRADE_CLOSED.getValue())) {
            log.debug("交易关闭业务");
        } else if (tradeStatus.equals(PayStatus.TRADE_SUCCESS.getValue())) {
            log.debug("交易成功业务");
            String outTradeNo = paMap.get("out_trade_no");
            log.debug("交易成功业务,订单号:{}", outTradeNo);

            //修改本地订单状态

        } else if (tradeStatus.equals(PayStatus.TRADE_FINISHED.getValue())) {
            log.debug("交易完成业务");
        } else {
            log.debug("不知名状态");
        }
    }

    private Boolean verifyAliAsyncCallBackParams(Map<String, String> map) {
        String sign = map.get("sign");
        map.remove("sign_type");
        String signContent = AlipaySignature.getSignCheckContentV2(map);
        try {
            return AlipaySignature.rsa256CheckContent(signContent, sign, payConfig.getAliPayPublicKey(), "utf-8");
        } catch (AlipayApiException e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    private Map<String, String> getParams(Map<String, String[]> paramMap) {
        Map<String, String> map = new HashMap<>(24);
        paramMap.keySet().forEach(key -> {
            String[] value = paramMap.get(key);
            map.put(key, value[0]);
        });
        return map;
    }
}
