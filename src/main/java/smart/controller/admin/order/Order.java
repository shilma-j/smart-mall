package smart.controller.admin.order;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.ExpressCache;
import smart.entity.OrderEntity;
import smart.entity.OrderGoodsEntity;
import smart.lib.AdminHelper;
import smart.util.Helper;
import smart.lib.JsonResult;
import smart.lib.Pagination;
import smart.repository.OrderGoodsRepository;
import smart.repository.OrderRepository;
import smart.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.List;

@Controller(value = "admin/order/order")
@RequestMapping(path = "admin/order/order")
@Transactional
public class Order {

    @Resource
    OrderGoodsRepository orderGoodsRepository;

    @Resource
    OrderRepository orderRepository;

    @Resource
    OrderService orderService;

    /**
     * 取消订单
     *
     * @param request http request
     * @return json
     */
    @PostMapping(value = "cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody()
    public String postCancel(HttpServletRequest request) {
        long orderNo = Helper.longValue(request.getParameter("orderNo"));
        JsonResult jsonResult = new JsonResult();
        String err = orderService.cancelOrder(orderNo);
        if (err != null) {
            jsonResult.setMsg(err);
        }
        return jsonResult.toString();
    }

    /**
     * 确认收货
     *
     * @param request http request
     * @return json
     */
    @PostMapping(value = "confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody()
    public String postConfirm(HttpServletRequest request) {
        long orderNo = Helper.longValue(request.getParameter("orderNo"));
        JsonResult jsonResult = new JsonResult();
        String err = orderService.confirmOrder(orderNo);
        if (err != null) {
            jsonResult.setMsg(err);
        }
        return jsonResult.toString();
    }


    /**
     * 编辑订单
     *
     * @param request request
     * @return view
     */
    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        long no = Helper.longValue(request.getParameter("no"));
        OrderEntity orderEntity = orderRepository.findByNo(no);
        if (orderEntity == null) {
            return AdminHelper.msgPage("订单不存在", "/admin/order/order/list", request);
        }
        List<OrderGoodsEntity> orderGoodsEntities = orderGoodsRepository.findAllByOrderNo(orderEntity.getNo());
        ModelAndView modelAndView = Helper.newModelAndView("admin/order/order/edit", request);
        modelAndView.addObject("expressList", ExpressCache.getCompanies());
        modelAndView.addObject("order", orderEntity);
        modelAndView.addObject("orderGoods", orderGoodsEntities);
        modelAndView.addObject("title", "订单信息");
        return modelAndView;
    }

    /**
     * 订单列表
     *
     * @param request request
     * @return view
     */
    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        String sql = """
                SELECT
                    o.consignee,
                    o.create_time,
                    o.id,
                    o.no,
                    o.pay_time,
                    o.pay_amount,
                    o.pay_no,
                    o.amount,
                    o.status,
                    o.user_id,
                    u.name as user_name
                FROM t_order o
                LEFT JOIN t_user u ON o.user_id = u.id
                ORDER BY o.id DESC
                """;
        Pagination pagination = Pagination.newBuilder(sql).page(request).build();
        ModelAndView modelAndView = Helper.newModelAndView("admin/order/order/list", request);
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", "订单列表");
        return modelAndView;
    }

    @PostMapping(value = "pay", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody()
    @Transactional
    public String postPay(HttpServletRequest request) {
        long orderNo = Helper.longValue(request.getParameter("orderNo"));
        var orderEntity = orderRepository.findByNoForUpdate(orderNo);
        JsonResult jsonResult = new JsonResult();
        if (orderEntity == null) {
            return jsonResult.setMsg("订单不存在").toString();
        }
        String err = orderService.pay(orderNo, "admin", orderEntity.getAmount(), "");
        if (err != null) {
            jsonResult.setMsg(err);
        }
        return jsonResult.toString();
    }

    /**
     * 取消订单
     *
     * @param request http request
     * @return json
     */
    @PostMapping(value = "refund", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody()
    public String postRefund(HttpServletRequest request) {
        long orderNo = Helper.longValue(request.getParameter("orderNo"));
        JsonResult jsonResult = new JsonResult();
        String err = orderService.refundOrder(orderNo);
        if (err != null) {
            jsonResult.setMsg(err);
        }
        return jsonResult.toString();
    }

    @PostMapping(value = "ship", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody()
    public String postShip(HttpServletRequest request) {
        long orderNo = Helper.longValue(request.getParameter("orderNo"));
        int expressId = Helper.intValue(request.getParameter("expressId"));
        String expressNo = request.getParameter("expressNo");
        JsonResult jsonResult = new JsonResult();
        String err = orderService.ship(orderNo, expressId, expressNo);
        if (err != null) {
            jsonResult.setMsg(err);
        }
        return jsonResult.toString();
    }
}

