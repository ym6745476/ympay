package com.ymbok.pay.service;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ymbok.pay.config.PayConfig;
import com.ymbok.pay.dao.PayDao;
import com.ymbok.pay.domain.PayEntity;
import com.ymbok.pay.utils.GsonUtil;
import com.ymbok.pay.utils.OkHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component注解用于对那些比较中立的类进行注释；
//相对与在持久层、业务层和控制层分别采用 @Repository、@Service 和 @Controller 对分层中的类进行注释
@Component
@EnableScheduling   // 1.开启定时任务
@EnableAsync        // 2.开启多线程
public class ScheduleTask {

    @Autowired
    private AliPayOrderService aliPayOrderService;

    @Autowired
    private PayDao payDao;

    @Autowired
    private PayConfig payConfig;

    @Async
    @Scheduled(fixedDelay = 5000)  //间隔5秒
    public void first() {
        System.out.println("支付查询定时任务开始 : " + LocalDateTime.now().toLocalTime() + "\r\n线程 : " + Thread.currentThread().getName());
        System.out.println();
        try{
            List<PayEntity> payEntityList = payDao.findWaitPay();
            if(payEntityList != null){
                for(PayEntity payEntityOld : payEntityList){
                    AlipayTradeQueryResponse  response =  aliPayOrderService.getTradesQuery(payEntityOld.getOutTradeNo(), payEntityOld.getTradeNo());
                    if (response != null && response.isSuccess()) {
                        System.out.println("数据更新成功");

                        //更新交易状态
                        payEntityOld.setTradeStatus(response.getTradeStatus());

                        if("TRADE_SUCCESS".equals(response.getTradeStatus())){
                            Map<String, String> params = new HashMap<>();
                            params.put("userId",String.valueOf(payEntityOld.getUserId()));
                            params.put("userName",String.valueOf(payEntityOld.getUserName()));
                            params.put("roleId",String.valueOf(payEntityOld.getRoleId()));
                            params.put("roleName",String.valueOf(payEntityOld.getRoleName()));
                            params.put("serverId",String.valueOf(payEntityOld.getServerId()));

                            params.put("subject",String.valueOf(payEntityOld.getSubject()));
                            params.put("appId",String.valueOf(payEntityOld.getAppId()));
                            params.put("goodsId",String.valueOf(payEntityOld.getGoodsId()));
                            params.put("outTradeNo",String.valueOf(response.getOutTradeNo()));
                            params.put("totalAmount",String.valueOf(response.getTotalAmount()));
                            params.put("tradeStatus",String.valueOf(response.getTradeStatus()));

                            Map<String, String> headers = new HashMap<>();

                            System.out.println("回调参数###################START");
                            System.out.println("userId:" + String.valueOf(payEntityOld.getUserId()));
                            System.out.println("userName:" + String.valueOf(payEntityOld.getUserName()));

                            System.out.println("roleId:" + String.valueOf(payEntityOld.getRoleId()));
                            System.out.println("roleName:" + String.valueOf(payEntityOld.getRoleName()));
                            System.out.println("serverId:" + String.valueOf(payEntityOld.getServerId()));

                            System.out.println("subject:" + payEntityOld.getSubject());
                            System.out.println("appId:" + String.valueOf(payEntityOld.getAppId()));
                            System.out.println("goodsId:" + payEntityOld.getGoodsId());
                            System.out.println("outTradeNo:" + String.valueOf(response.getOutTradeNo()));
                            System.out.println("totalAmount:" + String.valueOf(response.getTotalAmount()));
                            System.out.println("tradeStatus:" + String.valueOf(response.getTradeStatus()));
                            System.out.println("回调参数###################END");

                            String text = payConfig.getOutSign() + payEntityOld.getAppId() + payEntityOld.getUserId() + response.getOutTradeNo() + response.getTotalAmount();
                            System.out.println("回调Header参数明文：" + text);

                            String sign = DigestUtils.md5DigestAsHex(text.getBytes());
                            System.out.println("回调Header参数密文：" + sign);

                            headers.put("sign",sign);

                            HashMap<String,String> urlMap = GsonUtil.fromJson(payConfig.getOutNotifyUrl(),new HashMap<String,String>().getClass());
                            String notifyUrl = urlMap.get(payEntityOld.getAppId());
                            System.out.println("支付成功回调地址：" + notifyUrl);
                            String responseNotify = OkHttpUtil.post(notifyUrl,headers,params);

                            System.out.println(responseNotify);
                            payEntityOld.setNotifyResponse(responseNotify);

                            if(responseNotify.contains("success")){
                                payEntityOld.setNotifyStatus("1");
                                System.out.println("回调通知成功：" + responseNotify);
                            }else{
                                payEntityOld.setNotifyStatus("0");
                                System.out.println("回调通知失败：" + responseNotify);
                            }
                            payDao.updatePay(payEntityOld);
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /*@Async
    @Scheduled(fixedDelay = 3600000)
    public void second() {
        System.out.println("第二个定时任务开始 : " + LocalDateTime.now().toLocalTime() + "\r\n线程 : " + Thread.currentThread().getName());
        System.out.println();
    }*/

}
