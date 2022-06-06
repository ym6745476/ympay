package com.ymbok.pay.domain.alipay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付宝交易状态
 * @author Mr.Zhao
 * @date 2020-07-03 09:59
 */
@Getter
@AllArgsConstructor
public enum PayStatus {
    /**
     * 交易创建，等待买家付款
     */
    WAIT_BUYER_PAY("WAIT_BUYER_PAY"),

    /**
     * 未付款交易超时关闭，或支付完成后全额退款
     */
    TRADE_CLOSED("TRADE_CLOSED"),

    /**
     * 交易支付成功
     */
    TRADE_SUCCESS("TRADE_SUCCESS"),

    /**
     * 交易结束，不可退款
     */
    TRADE_FINISHED("TRADE_FINISHED"),
    ;
    private String value;


}
