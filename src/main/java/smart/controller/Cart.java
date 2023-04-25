package smart.controller;

import jakarta.annotation.Resource;
import smart.authentication.UserToken;
import smart.cache.PaymentCache;
import smart.cache.SystemCache;
import smart.entity.UserAddressEntity;
import smart.util.Helper;
import smart.lib.thymeleaf.HelperUtils;
import smart.lib.Json;
import smart.lib.JsonResult;
import smart.repository.UserAddressRepository;
import smart.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@Controller
@RequestMapping(path = "cart")
@Transactional
public class Cart {

    @Resource
    OrderService orderService;

    @Resource
    UserAddressRepository userAddressRepository;

    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        var cart = new smart.lib.Cart(request);
        ModelAndView modelAndView = Helper.newModelAndView("cart/index", request);
        modelAndView.addObject("cartItems", cart.getItems());
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 购物车");
        return modelAndView;
    }

    /**
     * 商品加入购物车
     *
     * @param request req
     * @return 视图
     */
    @GetMapping(path = "add")
    public ModelAndView getAdd(HttpServletRequest request) {
        long goodsId = Helper.longValue(request.getParameter("gid"));
        long specId = Helper.longValue(request.getParameter("specId"));
        long num = Helper.longValue(request.getParameter("num"));
        var cart = new smart.lib.Cart(request);
        cart.add(goodsId, specId, num);
        return Helper.msgPage("已加入购物车", "/cart", request);
    }

    /**
     * 购买页面
     *
     * @param request request
     * @return view
     */
    @GetMapping(path = "buy")
    public ModelAndView getBuy(HttpServletRequest request) {
        var cart = new smart.lib.Cart(request);
        if (cart.sumNum() == 0) {
            return Helper.msgPage("购物车中没有选中的商品", "/cart", request);
        }
        UserToken userToken = (UserToken) request.getAttribute(Helper.camelCase(UserToken.class));
        var addresses = userAddressRepository.findAllByUserId(userToken.getId());
        long addrId = Helper.longValue(request.getParameter("addrId"));
        UserAddressEntity address = null;
        for (var addr : addresses) {
            if (addr.getId() == addrId) {
                address = addr;
                break;
            }
        }
        if (address == null && addresses.size() > 0) {
            address = addresses.get(0);
            addrId = address.getId();
        }
        ModelAndView modelAndView = Helper.newModelAndView("cart/buy", request);
        modelAndView.addObject("addresses", addresses);
        modelAndView.addObject("address", address);

        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), 60, 60));
        }
        modelAndView.addObject("addrId", addrId);
        modelAndView.addObject("cart", cart);
        //运费
        long shippingFee = 0;
        if (address != null) {
            shippingFee = cart.shippingFee(address.getRegion());
        }
        modelAndView.addObject("payments", PaymentCache.getPayments());
        modelAndView.addObject("shippingFee", shippingFee);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 结算");
        return modelAndView;
    }


    /**
     * 提交订单
     *
     * @param request req
     * @return re
     */
    @PostMapping(path = "buy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postBuy(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long addrId = Helper.longValue(request.getParameter("addrId"));
        String payName = request.getParameter("payName");
        // 验证提交时的运费和商品价格
        long shippingFee = Helper.bigDecimalValue(request.getParameter("shippingFee")).multiply(new BigDecimal(100)).longValue();
        long sumPrice = Helper.bigDecimalValue(request.getParameter("sumPrice")).multiply(new BigDecimal(100)).longValue();
        smart.lib.Cart cart = new smart.lib.Cart(request);
        var orderInfo = orderService.addOrder(
                addrId, payName, cart, sumPrice, shippingFee, Helper.isMobileRequest(request) ? 2 : 1
        );
        if (orderInfo.getErr() != null) {
            return jsonResult.setMsg(orderInfo.getErr()).toString();
        }

        if (orderInfo.getAmount() == 0) {
            return jsonResult.setMsg("已完成下单").setUrl("/user/order").toString();
        }
        return jsonResult.setUrl("/user/order/pay?orderNo=" + orderInfo.getOrderNo()).toString();
    }

    @GetMapping(path = "json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getJson(HttpServletRequest request) {
        long goodsId = Helper.longValue(request.getParameter("goodsId"));
        long specId = Helper.longValue(request.getParameter("specId"));
        boolean selected = Helper.longValue(request.getParameter("selected")) > 0;
        long num = Helper.longValue(request.getParameter("num"));
        long imgWidth = Helper.longValue(request.getParameter("w"));
        if (imgWidth <= 0) {
            imgWidth = 60;
        }
        String method = request.getParameter("m");
        if (method == null) {
            method = "";
        }

        var cart = new smart.lib.Cart(request);
        switch (method) {
            case "add" -> cart.add(goodsId, specId, num);
            case "clear" -> cart.clear();
            case "del" -> cart.del(goodsId, specId);
            case "selected" -> cart.setSelected(goodsId, specId, selected);
            case "selectedAll" -> cart.setSelectedAll(selected);
            case "sub" -> cart.sub(goodsId, specId, num);
        }

        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), imgWidth));
        }
        return Json.encode(cart.getItems());
    }
}
