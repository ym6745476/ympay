package com.ymbok.pay.service;

import com.alipay.api.response.*;

import javax.servlet.http.HttpServletRequest;


public interface AliPayOrderService {

    /**
     * 创建预支付单
     * @param userId
     * @param userName
     * @param subject
     * @param appId
     * @param goodsId
     * @param totalAmount
     * @return 结果
     */
    AlipayTradePrecreateResponse createOrderPayQrCode(String userId,String userName,String roleId,String roleName,
             String serverId,String subject,String appId,String goodsId,String totalAmount);

    /**
     * 通过交易订单号查询支付订单
     * @param outTradeNo
     * @return
     */
    AlipayTradeQueryResponse getTradesQuery(String  outTradeNo,String tradeNo);

    /**
     * 取消支付单
     * @param outTradeNo
     * @return
     */
    String tradeCancelQrCodeByOutTradeNo(String outTradeNo);

    /**
     * 取消交易
     *
     * @param outTradeNo
     * @return
     */
    boolean tradeCancelByOutTradeNo(String outTradeNo);

    /**
     * 交易关闭
     * @param outTradeNo 条件
     * @return 结果
     */
    boolean tradeClose(String outTradeNo);


    /**
     * 交易回调
     * @param request
     * @return
     */
    void payCallBack(HttpServletRequest request);

}
