package com.ymbok.pay.dao;

import com.ymbok.pay.domain.PayEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayDao {

    /**
     * 获取交易信息列表
     * @return
     */
    List<PayEntity> findAllPay();

    /**
     * 获取交易信息列表
     * @return
     */
    List<PayEntity> findWaitPay();


    /**
     * 根据商家订单号,查询交易信息
     * @param outTradeNo
     * @return
     */
    PayEntity findPayById(String outTradeNo);

    /**
     * 新增交易信息
     * @param payEntity
     * @return
     */
    Long savePay(PayEntity payEntity);

    /**
     * 更新交易信息
     * @param payEntity
     * @return
     */
    Long updatePay(PayEntity payEntity);

    /**
     * 取消支付
     * @param outTradeNo
     * @return
     */
    Long cancelPay(String outTradeNo);

    /**
     * 根据商家订单号,删除交易信息
     * @param outTradeNo
     * @return
     */
    Long deletePay(String outTradeNo);
}
