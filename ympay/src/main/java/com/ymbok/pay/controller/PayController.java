package com.ymbok.pay.controller;

import com.alipay.api.response.*;
import com.ymbok.pay.config.PayConfig;
import com.ymbok.pay.dao.PayDao;
import com.ymbok.pay.domain.PayCodeEntity;
import com.ymbok.pay.domain.PayEntity;
import com.ymbok.pay.domain.api.ApiItemResult;
import com.ymbok.pay.domain.api.ApiItemsResult;
import com.ymbok.pay.domain.api.ApiResult;
import com.ymbok.pay.service.AliPayOrderService;
import com.ymbok.pay.utils.DateUtil;
import com.ymbok.pay.utils.GsonUtil;
import com.ymbok.pay.utils.StringUtil;
import com.ymbok.pay.utils.ZxingUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * API接口
 * @author ymbok.com(ym6745476)
 * @date 2020-06-30 14:38
 */
@RestController
@Log4j2
public class PayController {

    @Autowired
    private AliPayOrderService aliPayOrderService;

    @Autowired
    private PayDao payDao;

    @Autowired
    private PayConfig payConfig;

    /**
    @RequestMapping(value = "/api/city/{id}", method = RequestMethod.DELETE)
    public void modifyCity(@PathVariable("id") Long id) {
        cityService.deleteCity(id);
    }*/

    /**
     * 当面付入口
     * 根据订单信息显示付款二维码
     * 生成二维码后，展示给用户，由用户扫描二维码完成订单支付
     * @return
     */
    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public ModelAndView facePay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject("userId",request.getParameter("userId"));
        mav.addObject("userName",request.getParameter("userName"));
        mav.addObject("roleId",request.getParameter("roleId"));
        mav.addObject("roleName",request.getParameter("roleName"));
        mav.addObject("serverId",request.getParameter("serverId"));
        mav.addObject("appId",request.getParameter("appId"));
        mav.addObject("goodsId",request.getParameter("goodsId"));
        mav.addObject("totalAmount",request.getParameter("totalAmount"));
        mav.setViewName("pay/index");
        return mav;
    }

    /**
     * 流水页面入口
     * @return
     */
    @RequestMapping(value = "/pay/order", method = RequestMethod.GET)
    public ModelAndView trade(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("order/index");
        return mav;
    }

    /**
     * 生成当面付二维码
     * 如果生成的二维码没有扫码,则删除本地支付单,如果已经扫码,则取消支付订单
     * @return
     */
    @PostMapping(value = "/pay/code/create")
    @ResponseBody
    public ApiItemResult payCodeCreate(HttpServletRequest request, HttpServletResponse response) {

        ApiItemResult<PayCodeEntity> result = null;
        log.debug("支付宝扫码支付生成二维码");

        String userId = request.getParameter("userId");
        String userName = request.getParameter("userName");
        String subject = request.getParameter("subject");
        String appId = request.getParameter("appId");
        String goodsId = request.getParameter("goodsId");
        String totalAmount = request.getParameter("totalAmount");

        String roleId = request.getParameter("roleId");
        String roleName = request.getParameter("roleName");
        String serverId = request.getParameter("serverId");

        try {
            result = new ApiItemResult();

            /** 控制使用时间 */
            long packTime = DateUtil.getDateByFormat("2023-06-01 00:00:01",DateUtil.dateFormatYMDHMS).getTime();
            long currentTime = System.currentTimeMillis();

            PayCodeEntity payCodeEntity = new PayCodeEntity();
            payCodeEntity.setTotalAmount(totalAmount);
            if(StringUtil.isEmpty(subject)){
                subject = totalAmount + "元充值";
            }
            if(StringUtil.isEmpty(appId)){
                result.setResultCode(ApiResult.RESULT_ERROR);
                result.setResultMessage("参数错误");
                return result;
            }
            payCodeEntity.setSubject(subject);
            if((currentTime - packTime)/1000.0f < 3600 * 24 * 90){   //90天 3个月
                AlipayTradePrecreateResponse aliResponse = aliPayOrderService.createOrderPayQrCode(userId,userName,roleId,roleName,serverId,subject,appId,goodsId,totalAmount);
                payCodeEntity.setOutTradeNo(aliResponse.getOutTradeNo());
                result.setResultCode(ApiResult.RESULT_OK);
                payCodeEntity.setQrCode(aliResponse.getQrCode());

                if("10027".equals(appId)){
                    // 需要修改为运行机器上的路径
                    String filePath = String.format("/www/wwwroot/bxcq/pay/qrcode/qr-%s.png",aliResponse.getOutTradeNo());
                    log.info("filePath:" + filePath);
                    ZxingUtil.getQrCodeImage(aliResponse.getQrCode(), 256, filePath);
                    payCodeEntity.setQrCodeImage("http://g.ymbok.com/pay/qrcode/" + String.format("qr-%s.png",aliResponse.getOutTradeNo()));
                }

            }else{
                result.setResultCode(ApiResult.RESULT_ERROR);
                result.setResultMessage("软件使用时间到期");
            }
            result.setItem(payCodeEntity);
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultCode(ApiResult.RESULT_ERROR);
            result.setResultMessage(e.getMessage());
        }
        return result;
    }

    @GetMapping("/pay/query")
    @ResponseBody
    public ApiItemResult tradeQuery(String outTradeNo) {
        ApiItemResult<PayEntity> result = null;
        try {
            result = new ApiItemResult();
            PayEntity payEntity = payDao.findPayById(outTradeNo);
            if(payEntity != null){
                result.setItem(payEntity);
                result.setResultCode(ApiResult.RESULT_OK);
            }
            result.setResultCode(ApiResult.RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultCode(ApiResult.RESULT_ERROR);
            result.setResultMessage(e.getMessage());
        }
        return result;
    }

    @GetMapping("/pay/queryAll")
    @ResponseBody
    public ApiItemsResult tradeQueryAll() {
        ApiItemsResult<PayEntity> result = null;
        try {
            result = new ApiItemsResult();
            List<PayEntity> payEntityList = payDao.findAllPay();
            if(payEntityList != null){
                result.setItems(payEntityList);
                result.setResultCode(ApiResult.RESULT_OK);
            }
            result.setResultCode(ApiResult.RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultCode(ApiResult.RESULT_ERROR);
            result.setResultMessage(e.getMessage());
        }
        return result;
    }


    /**
     * 取消二维码
     * 如果生成的二维码没有扫码,则删除本地支付单,如果已经扫码,则取消支付订单
     * @param outTradeNo
     * @return
     */
    @GetMapping(value = "/pay/cancel")
    public ApiResult tradeCancelQrCodeByOutTradeNo(@RequestParam("outTradeNo") String outTradeNo) {
        ApiResult result = null;
        try {
            result = new ApiResult();
            String message  = aliPayOrderService.tradeCancelQrCodeByOutTradeNo(outTradeNo);
            if("success".equals(message)){
                result.setResultCode(ApiResult.RESULT_OK);
            }else{
                result.setResultCode(ApiResult.RESULT_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultCode(ApiResult.RESULT_ERROR);
            result.setResultMessage(e.getMessage());
        }
        return result;
    }

    /**
     * 交易关闭
     * 用于交易创建后，用户在一定时间内未进行支付
     * 可调用该接口直接将未付款的交易进行关闭。
     * @param outTradeNo
     * @return
     */
    @PostMapping("/pay/close")
    public boolean tradeClose(String outTradeNo) {
        return aliPayOrderService.tradeClose(outTradeNo);
    }

    /**
     * 交易回调(现在只处理支付成功的)
     * 服务器异步通知页面特性
     * 必须保证服务器异步通知页面（notify_url）上无任何字符，如空格、HTML标签、开发系统自带抛出的异常提示信息等，并且异步通知地址必须为用户外网可访问，异步通知地址不能重定向；
     * 支付宝是用 POST 方式发送通知信息，因此该页面中获取参数的方式，如：request.Form(“out_trade_no”)、$_POST[‘out_trade_no’]；
     * 支付宝主动发起通知，该方式才会被启用；
     * 只有在支付宝的交易管理中存在该笔交易，且发生了交易状态的改变，支付宝才会通过该方式发起服务器通知（即时到账交易状态为“等待买家付款”的状态默认是不会发送通知的）；
     * 服务器间的交互，不像页面跳转同步通知可以在页面上显示出来，这种交互方式是不可见的；
     * 第一次交易状态改变（即时到账中此时交易状态是交易完成）时，不仅会返回同步处理结果，而且服务器异步通知页面也会收到支付宝发来的处理结果通知；
     * 程序执行完后必须打印输出“success”（不包含引号）。如果商户反馈给支付宝的字符不是success这7个字符，支付宝服务器会不断重发通知，直到超过24小时22分钟。一般情况下，25小时以内完成8次通知（通知的间隔频率一般是：4m,10m,10m,1h,2h,6h,15h）；
     * 程序执行完成后，该页面不能执行页面跳转。如果执行页面跳转，支付宝会收不到success字符，会被支付宝服务器判定为该页面程序运行出现异常，而重发处理结果通知；
     * cookies、session等在此页面会失效，即无法获取这些数据；
     * 该方式的调试与运行必须在服务器上，即互联网上能访问；
     * 该方式的作用主要防止订单丢失，即页面跳转同步通知没有处理订单更新，它则去处理；
     * 当商户收到服务器异步通知并打印出success时，服务器异步通知参数notify_id才会失效。也就是说在支付宝发送同一条异步通知时（包含商户并未成功打印出success导致支付宝重发数次通知），服务器异步通知参数notify_id是不变的。
     * @param request
     */
    @PostMapping("/pay/notify")
    public String tradeNotify(HttpServletRequest request) {
        log.debug("支付宝异步回调开始...");
        aliPayOrderService.payCallBack(request);
        //success告诉支付宝回调成功
        //return "success";
        log.debug("支付宝异步回调结束...");
        return "failure";
    }

    /**
     * 支付宝同步回调接口GET
     * @param request
     * @return
     */
    @GetMapping("/pay/return")
    public String tradeReturn(HttpServletRequest request) {
        log.debug("支付宝说同步回调不可靠...是否支付成功,需要根据返回的订单号查询订单状态确定");
        log.debug("支付宝侧同步回调开始...");
        Map<String, String[]> parmMap = request.getParameterMap();
        log.debug("同步回调参数是:{}", GsonUtil.toJson(parmMap));
        log.debug("接受到同步回调信息:{},开始处理相关业务", GsonUtil.toJson(parmMap));
        //return "success";
        log.debug("支付宝侧同步回调结束...");
        return "failure";
    }


}
