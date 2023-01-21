package smart.controller.user;

import jakarta.annotation.Resource;
import smart.authentication.UserToken;
import smart.cache.RegionCache;
import smart.cache.SystemCache;
import smart.entity.UserAddressEntity;
import smart.lib.Helper;
import smart.lib.Json;
import smart.lib.JsonResult;
import smart.lib.Validate;
import smart.repository.UserAddressRepository;
import smart.service.UserAddressService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping(path = "user/address")
@Transactional
public class Address {

    @Resource
    UserAddressRepository userAddressRepository;

    @Resource
    UserAddressService userAddressService;


    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/address/index", request);
        modelAndView.addObject("addresses", userAddressRepository.findAllByUserId(userToken.getId()));
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 收货地址");
        return modelAndView;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request, UserToken userToken) {
        String back = request.getParameter("back");
        if (back == null) {
            back = "/user/address";
        }
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        String consignee = request.getParameter("consignee");
        if (consignee == null || consignee.trim().length() == 0) {
            jsonResult.setMsg("收货人姓名不得为空");
            return jsonResult.toString();
        }
        consignee = consignee.trim();
        String phone = request.getParameter("phone");
        String msg = Validate.notEmpty(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        msg = Validate.mobile(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        String address = request.getParameter("address");
        msg = Validate.notEmpty(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        address = address.trim();
        msg = Validate.address(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        long code = Helper.longValue(request.getParameter("code"));
        if (RegionCache.getRegion(code) == null) {
            jsonResult.setMsg("请选择 县/区");
            return jsonResult.toString();
        }
        UserAddressEntity userAddressEntity;
        if (id > 0) {
            userAddressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
            if (userAddressEntity == null) {
                jsonResult.setMsg("要修改的地址不存在");
                return jsonResult.toString();
            }
        } else {
            userAddressEntity = new UserAddressEntity();
            userAddressEntity.setUserId(userToken.getId());
        }
        userAddressEntity.setAddress(address);
        userAddressEntity.setConsignee(consignee);
        userAddressEntity.setPhone(phone);
        userAddressEntity.setRegion(code);
        if (Helper.longValue(request.getParameter("dft")) > 0) {
            userAddressEntity.setDft(1);
        }
        if (id > 0) {
            userAddressService.updateAddress(userAddressEntity);
        } else {
            msg = userAddressService.addAddress(userAddressEntity);
            if (msg != null) {
                jsonResult.setMsg(msg);
                return jsonResult.toString();
            }
        }
        // 对有addrId需求的请求回调时带上addrId
        if (request.getParameter("addrId") != null) {
            back += "?addrId=" + userAddressEntity.getId();
        }
        jsonResult.setUrl(back);
        return jsonResult.toString();
    }

    @GetMapping(path = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDelete(HttpServletRequest request, UserToken userToken) {
        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressRepository.delete(addressEntity);
        return null;
    }

    @GetMapping(path = "json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getJson(HttpServletRequest request, UserToken userToken) {
        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        String json = Json.encode(addressEntity);
        if (json == null) {
            return "{\"err\":\"系统错误\"}";
        }
        return json;
    }

    @GetMapping(path = "setDft", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getSetDft(HttpServletRequest request, UserToken userToken) {

        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressService.setDefault(addressEntity);
        return null;
    }
}
