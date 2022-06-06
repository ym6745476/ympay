package com.ymbok.pay.domain;

import lombok.Data;

@Data
public class PayEntity {

    private int id;
    private String userId;
    private String userName;
    private String subject;

    private String roleId;
    private String roleName;
    private String serverId;

    private String appId;
    private String goodsId;

    private String outTradeNo;
    private String totalAmount;
    private String tradeStatus;

    private String tradeNo;
    private String buyerLogonId;
    private String buyerPayAmount;
    private String sendPayDate;
    private String body;
    private String notifyStatus;
    private String notifyResponse;
    private String createTime;
    private String updateTime;
    private int count;

}
