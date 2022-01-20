package smart.controller.user;

import smart.authentication.UserToken;
import smart.cache.PaymentCache;
import smart.cache.SystemCache;
import smart.entity.OrderEntity;
import smart.entity.OrderGoodsEntity;
import smart.lib.Helper;
import smart.lib.JsonResult;
import smart.lib.Pagination;
import smart.lib.payment.Payment;
import smart.repository.OrderGoodsRepository;
import smart.repository.OrderRepository;
import smart.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(path = "user/order")
@Transactional
public class Order {

    @Resource
    OrderRepository orderRepository;

    @Resource
    OrderService orderService;

    @Resource
    OrderGoodsRepository orderGoodsRepository;

    /**
     * 取消订单
     *
     * @param request request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "cancel")
    public ModelAndView getCancel(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.longValue(request.getParameter("no"));
        String err = orderService.cancelOrder(userToken.getId(), no);
        if (err == null) {
            return Helper.msgPage(String.format("订单 %s 已取消", no), "/user/order", request);
        }
        return Helper.msgPage(String.format("订单 %s 取消失败", no), "/user/order", request);
    }

    /**
     * 确认收货
     *
     * @param request request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "confirm")
    public ModelAndView getConfirm(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.longValue(request.getParameter("no"));
        String err = orderService.confirmOrder(userToken.getId(), no);
        if (err == null) {
            return Helper.msgPage(String.format("订单 %s 已确认收货", no), "/user/order", request);
        }
        return Helper.msgPage(String.format("订单 %s 确认收货失败", no), "/user/order", request);
    }

    /**
     * 移入回收站/删除订单
     *
     * @param request request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "delete")
    public ModelAndView getDelete(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.longValue(request.getParameter("no"));
        int deleted = Helper.intValue(request.getParameter("deleted"));
        String err = orderService.deleteOrder(userToken.getId(), no, deleted);

        String msg = switch (deleted) {
            case 0 -> "订单恢复";
            case 1 -> "订单移入回收站";
            case 2 -> "订单删除";
            default -> "";
        };
        if (err == null) {
            return Helper.msgPage(msg + "已完成 " + no, "/user/order", request);

        }
        return Helper.msgPage(msg + "失败 " + no, "/user/order", request);
    }

    /**
     * 订单详情
     *
     * @param request request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "detail")
    public ModelAndView getDetail(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.longValue(request.getParameter("no"));
        OrderEntity orderEntity = orderRepository.findByNo(no);
        if (orderEntity == null || orderEntity.getUserId() != userToken.getId()) {
            return Helper.msgPage("订单不存在", null, request);
        }
        List<OrderGoodsEntity> goodsList = orderGoodsRepository.findAllByOrderNo(orderEntity.getNo());

        ModelAndView modelAndView = Helper.newModelAndView("user/order/detail", request);
        modelAndView.addObject("goodsList", goodsList);
        modelAndView.addObject("order", orderEntity);
        modelAndView.addObject("title", "订单详情");

        return modelAndView;
    }

    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request, UserToken userToken) {
        int deleted = Helper.intValue(request.getParameter("deleted"));
        if (deleted < 0 || deleted > 1) {
            deleted = 0;
        }
        long page = Helper.longValue(request.getParameter("page"));
        ModelAndView modelAndView = Helper.newModelAndView("user/order/index", request);
        var sql = String.format("select * from t_order where user_id = %d and deleted = %d order by id desc",
                userToken.getId(), deleted);
        Pagination pagination = new Pagination(sql, null, 15, page, null);
        for (var row : pagination.getRows()) {
            row.put("goodsList", orderGoodsRepository.findAllByOrderNo(Helper.longValue(row.get("no"))));
        }
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 我的订单");
        return modelAndView;
    }

    /**
     * 支付订单
     *
     * @param request request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "pay")
    public ModelAndView getPay(HttpServletRequest request, UserToken userToken) {
        var orderNo = Helper.longValue(request.getParameter("orderNo"));
        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || orderEntity.getUserId() != userToken.getId()) {
            return Helper.msgPage("订单不存在 " + orderNo, request);
        }
        if (orderEntity.getStatus() > 0 || orderEntity.getPayTime() != null) {
            return Helper.msgPage("订单状态错误" + orderNo, request);
        }
        Payment payment = null;
        for (var item : PaymentCache.getPayments()) {
            if (item.getName().equals(orderEntity.getPayName())) {
                payment = item;
            }
        }
        if (payment == null) {
            return Helper.msgPage("该订单的支付方式已不被支持，请从新下单。 " + orderNo, request);
        }

        ModelAndView modelAndView = Helper.newModelAndView("user/order/pay", request);
        modelAndView.addObject("order", orderEntity);
        modelAndView.addObject("payment", payment);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 支付订单");
        return modelAndView;
    }

    /**
     * 查询订单支付状态, 用于客户端轮询.
     *
     * @param request http request
     * @return json
     */
    @GetMapping(path = "payCheck", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getPayCheck(HttpServletRequest request, UserToken userToken) {
        JsonResult jsonResult = new JsonResult();
        var orderNo = Helper.longValue(request.getParameter("orderNo"));

        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || orderEntity.getUserId() != userToken.getId()) {
            jsonResult.setMsg("订单不存在").setUrl("/user/order");
            return Helper.msgPage(jsonResult, request);
        }

        if (orderEntity.getStatus() > 0 && orderEntity.getPayTime() != null) {
            jsonResult.setMsg("订单支付成功").setUrl("/user/order");
            return Helper.msgPage(jsonResult, request);
        }
        return jsonResult.toString();
    }

    /**
     * 获取支付二维码
     *
     * @param request  http request
     * @param response http response
     * @param userToken user token
     */
    @GetMapping("payQr")
    @ResponseBody
    public String getPayQR(HttpServletRequest request, HttpServletResponse response, UserToken userToken) {
        var orderNo = Helper.longValue(request.getParameter("orderNo"));
        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || orderEntity.getUserId() != userToken.getId()) {
            return "订单不存在 " + orderNo;
        }
        if (orderEntity.getAmount() <= 0) {
            return "订单金额错误 " + orderNo;
        }
        Payment payment = PaymentCache.getPaymentByName(orderEntity.getPayName());
        if (payment == null) {
            return "该订单的支付方式已不被支持，请从新下单";
        }
        // 图片宽度
        int width = Helper.intValue(request.getParameter("w"));


        if (width < 300) {
            width = 300;
        }
        String qrStr;
        try {
            qrStr = payment.getQRCode("订单:" + orderNo, Long.toString(orderNo), orderEntity.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return "出错了，请联系管理员";
        }
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");
        byte[] buffer = Helper.getQRCodePng(qrStr, width);
        assert buffer != null;
        try {
            response.getOutputStream().write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
