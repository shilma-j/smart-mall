package smart.controller.admin.sys;

import smart.cache.PaymentCache;
import smart.entity.PaymentEntity;
import smart.lib.*;
import smart.repository.PaymentRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller(value = "admin/sys/payment")
@RequestMapping(path = "/admin/sys/payment")
@Transactional
public class Payment {

    @Resource
    PaymentRepository paymentRepository;

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request,
                                @RequestParam(name = "n", required = false, defaultValue = "") String name) {
        PaymentEntity paymentEntity = paymentRepository.findByName(name);
        if (paymentEntity == null) {
            return AdminHelper.msgPage("支付方式不存在:" + name, null, request);
        }
        String json = Security.decrypt(paymentEntity.getConfig());
        if (json == null || json.length() < 10) {
            json = "{}";
        }
        Map<String, String> config = Json.decode(json);
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/payment/" + name, request);
        modelAndView.addObject("config", config);
        modelAndView.addObject("payment", paymentEntity);
        modelAndView.addObject("title", "支付管理");
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request,
                           @RequestParam(required = false, defaultValue = "") String name) {
        JsonResult jsonResult = new JsonResult();
        Map<String, String> map = new HashMap<>();
        switch (name) {
            case "alipay":
                map.put("appId", request.getParameter("appId"));
                map.put("merchantPrivateKey", request.getParameter("merchantPrivateKey"));
                map.put("alipayPublicKey", request.getParameter("alipayPublicKey"));
                break;
            case "wechat":
                return jsonResult.setMsg("微信支付功能尚未完成").toString();
            //break;
            default:
                return jsonResult.setMsg("该支付方式不存在").toString();
        }
        String config = Json.encode(map);
        paymentRepository.updateByName(
                name,
                Helper.intValue(request.getParameter("enable")) > 0,
                Helper.intValue(request.getParameter("recommend")),
                Security.encrypt(config)
        );
        PaymentCache.init();
        return jsonResult.setMsg("保存成功").setUrl("/admin/sys/payment/list").toString();
    }

    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request) {
        List<PaymentEntity> payments = paymentRepository.findAllByOrderByRecommendDesc();
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/payment/list", request);
        modelAndView.addObject("payments", payments);
        modelAndView.addObject("title", "支付管理");
        return modelAndView;
    }
}
