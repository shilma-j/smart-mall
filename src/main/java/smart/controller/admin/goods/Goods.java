package smart.controller.admin.goods;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.BrandCache;
import smart.cache.CategoryCache;
import smart.cache.GoodsCache;
import smart.cache.SpecCache;
import smart.config.AppConfig;
import smart.entity.CategoryEntity;
import smart.entity.GoodsEntity;
import smart.entity.GoodsSpecEntity;
import smart.lib.*;
import smart.lib.status.GoodsStatus;
import smart.repository.GoodsRepository;
import smart.repository.GoodsSpecRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Controller(value = "admin/goods/goods")
@RequestMapping(path = "/admin/goods/goods")
@Transactional
public class Goods {

    @Resource
    private GoodsRepository goodsRepository;

    @Resource
    private GoodsSpecRepository goodsSpecRepository;

    private List<String> getItemsDesc(String spec) {
        List<String> desc = new ArrayList<>();
        if (spec.length() < 20) {
            return desc;
        }
        List<Spec> rows = Json.toList(spec, Spec.class);
        if (rows == null) {
            return List.of();
        }
        if (rows.size() == 0) {
            return desc;
        }
        for (Spec row : rows) {
            if (row.getList().size() == 0) {
                return desc;
            }
        }
        int[] index = new int[rows.size()];
        while (true) {
            for (int i = rows.size() - 1; i > 0; i--) {
                if (index[i] >= rows.get(i).getList().size()) {
                    index[i] = 0;
                    index[i - 1]++;
                }
            }
            if (index[0] >= rows.get(0).getList().size()) {
                return desc;
            }
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < index.length; i++) {
                if (i > 0) {
                    str.append(" ");
                }
                str.append(rows.get(i).getList().get(index[i]).val);
            }
            desc.add(str.toString());
            index[rows.size() - 1]++;
        }
    }

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public String postDelete(HttpServletRequest request) {

        JsonResult jsonResult = new JsonResult();
        jsonResult.setUrl("list");
        long id = Helper.longValue(request.getParameter("id"));
        var goodsEntity = goodsRepository.findById(id).orElse(null);
        if (goodsEntity == null) {
            jsonResult.setMsg("商品不存在");
            return AdminHelper.msgPage(jsonResult, request);
        }
        AppConfig.getJdbcTemplate().update("delete from t_goods_spec where goods_id = ?", id);
        AppConfig.getJdbcTemplate().update("delete from t_goods where id = ?", id);
        GoodsCache.init();
        jsonResult.setMsg("删除成功");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        long id = Helper.longValue(request.getParameter("id"));
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/goods/edit", request);
        GoodsEntity goodsEntity;
        int shipping;
        int onSell;
        if (id > 0) {
            goodsEntity = goodsRepository.findById(id).orElse(null);
            if (goodsEntity == null) {
                return AdminHelper.msgPage("商品不存在", "list", request);
            }
            if (goodsEntity.getSpec().isEmpty()) {
                goodsEntity.setSpec("[]");
            }
            shipping = (goodsEntity.getStatus() & GoodsStatus.SHIPPING_FEE) > 0 ? 1 : 2;
            onSell = (goodsEntity.getStatus() & GoodsStatus.ON_SELL) > 0 ? 1 : 2;
            modelAndView.addObject("title", "编辑商品");
        } else {
            goodsEntity = new GoodsEntity();
            goodsEntity.setSpec("[]");
            onSell = shipping = 2;
            goodsEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            modelAndView.addObject("title", "新建商品");
        }
        var goodsSpecList = goodsSpecRepository.findAllByGoodsIdOrderByIdxAsc(goodsEntity.getId());
        modelAndView.addObject("brandRows", BrandCache.getRows());
        modelAndView.addObject("cateList", CategoryCache.getList());
        modelAndView.addObject("shipping", shipping);
        modelAndView.addObject("onSell", onSell);
        modelAndView.addObject("goods", goodsEntity);
        modelAndView.addObject("goodsSpecList", goodsSpecList);
        modelAndView.addObject("specJson", SpecCache.getJson());
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public String postEdit(HttpServletRequest request,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String des,
                           @RequestParam(defaultValue = "", name = "released") String releasedStr
    ) {
        JsonResult jsonResult = new JsonResult();
        String msg;
        name = name.trim();
        if (name.length() == 0) {
            jsonResult.setMsg("商品名称不得为空");
            return jsonResult.toString();
        }
        long status = 0L;
        int shipping = Helper.intValue(request.getParameter("shipping"));
        if (shipping < 1 || shipping > 2) {
            jsonResult.setMsg("请选择快递费用");
            return jsonResult.toString();
        }
        if (shipping == 1) {
            status = status | GoodsStatus.SHIPPING_FEE;
        }
        int onSell = Helper.intValue(request.getParameter("onSell"));
        if (onSell < 1 || onSell > 2) {
            jsonResult.setMsg("请选择商品状态");
            return jsonResult.toString();
        }
        if (onSell == 1) {
            status = status | GoodsStatus.ON_SELL;
        }
        Date released;
        releasedStr = releasedStr.replace('T', ' ');
        try {
            released = Helper.parseDate(releasedStr);
        } catch (Exception e) {
            jsonResult.setMsg("请填写正确的发布日期");
            return jsonResult.toString();
        }
        String[] imgs = request.getParameterValues("img");
        if (imgs == null || imgs.length == 0) {
            jsonResult.setMsg("商品相册不得为空");
            return jsonResult.toString();
        }

        String spec = request.getParameter("spec");
        if (spec == null) {
            jsonResult.setMsg("规格值缺失");
            return jsonResult.toString();
        }
        var itemsDesc = getItemsDesc(spec);

        GoodsEntity goodsEntity;
        Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());

        long id = Helper.longValue(request.getParameter("id"));
        if (id < 0) {
            jsonResult.setMsg("参数错误: id");
            return jsonResult.toString();
        }
        if (id > 0) {
            goodsEntity = goodsRepository.findByIdForWrite(id);
            if (goodsEntity == null) {
                jsonResult.setMsg("商品不存在");
                jsonResult.setUrl("list");
                return AdminHelper.msgPage(jsonResult, request);
            }
        } else {
            goodsEntity = new GoodsEntity();
            goodsEntity.setCreateTime(now);
        }
        goodsEntity.setUpdateTime(new Timestamp(released.getTime()));
        goodsEntity.setBrandId(Helper.longValue(request.getParameter("brandId")));
        if (goodsEntity.getBrandId() > 0 && BrandCache.getRows().get(goodsEntity.getBrandId()) == null) {
            jsonResult.setMsg("商品品牌不存在");
            return jsonResult.toString();
        }
        goodsEntity.setImgs(String.join(",", imgs));
        goodsEntity.setName(name);
        goodsEntity.setDes(des);
        goodsEntity.setCateId(Helper.longValue(request.getParameter("cateId")));
        if (goodsEntity.getCateId() <= 0) {
            jsonResult.setMsg("请选择商品类别");
            return jsonResult.toString();
        }
        if (CategoryCache.getEntityById(goodsEntity.getCateId()) == null) {
            jsonResult.setMsg("商品类别不存在");
            return jsonResult.toString();
        }
        goodsEntity.setRecommend(Helper.longValue(request.getParameter("recommend")));
        goodsEntity.setStatus(status);
        if (itemsDesc.size() == 0) {
            goodsEntity.setSpec("");
            String price = request.getParameter("price");
            msg = Validate.price(price, "价格");
            if (msg != null) {
                jsonResult.setMsg(msg);
                return jsonResult.toString();
            }
            goodsEntity.setPrice(Helper.priceToLong(price));
            long stock = Helper.longValue(request.getParameter("stock"));
            if (stock < 0) {
                jsonResult.setMsg("库存不得为负数");
                return jsonResult.toString();
            }
            if (id > 0) {
                long oldStock = Helper.longValue(request.getParameter("oldStock"));
                if (oldStock < 0) {
                    jsonResult.setMsg("参数错误: oldStock");
                    return jsonResult.toString();
                }
                goodsEntity.setStock(goodsEntity.getStock() - oldStock + stock);
                if (goodsEntity.getStock() < 0) {
                    goodsEntity.setStock(0);
                }
            } else {
                goodsEntity.setStock(stock);
                if (goodsEntity.getStock() < 0) {
                    jsonResult.setMsg("库存不得为负数");
                    return jsonResult.toString();
                }
            }
            goodsEntity.setWeight(Helper.longValue(request.getParameter("weight")));
            if (goodsEntity.getWeight() < 1) {
                jsonResult.setMsg("重量不得低于1");
                return jsonResult.toString();
            }
        } else {
            goodsEntity.setSpec(spec);
        }
        /* process specifications data */
        boolean newSpec = true;
        GoodsSpecEntity[] goodsSpecEntities = new GoodsSpecEntity[itemsDesc.size()];
        if (itemsDesc.size() > 0) {
            long minPrice = 0L;
            String[] specIds = request.getParameterValues("specId");
            String[] prices = request.getParameterValues("price");
            String[] oldStocks = request.getParameterValues("oldStock");
            String[] stocks = request.getParameterValues("stock");
            String[] weights = request.getParameterValues("weight");
            if (specIds == null || prices == null || oldStocks == null || stocks == null || weights == null) {
                jsonResult.setMsg("规格值数量错误");
                return jsonResult.toString();
            }
            if (specIds.length != itemsDesc.size() || prices.length != itemsDesc.size()
                    || oldStocks.length != itemsDesc.size()
                    || stocks.length != itemsDesc.size() || weights.length != itemsDesc.size()) {
                jsonResult.setMsg("规格值数量错误");
                return jsonResult.toString();
            }
            newSpec = Helper.priceToLong(specIds[0]) < 1;
            long specId, price1, oldStock1, stock1, weight1;
            String msgPre;
            for (int i = 0; i < itemsDesc.size(); i++) {
                msgPre = "第 " + (i + 1) + " 个";
                specId = Helper.longValue(specIds[i]);
                if (i > 0 && ((newSpec && specId > 0) || (!newSpec && specId < 1))) {
                    jsonResult.setMsg(msgPre + "参数错误: specId");
                    return jsonResult.toString();
                }
                msg = Validate.price(prices[i], msgPre + "价格");
                if (msg != null) {
                    jsonResult.setMsg(msg);
                    return jsonResult.toString();
                }
                price1 = Helper.priceToLong(prices[i]);

                oldStock1 = Helper.longValue(oldStocks[i]);
                stock1 = Helper.longValue(stocks[i]);
                weight1 = Helper.longValue(weights[i]);
                if (price1 < 0) {
                    jsonResult.setMsg(msgPre + "价格错误");
                    return jsonResult.toString();
                }
                if (i == 0) {
                    minPrice = price1;
                } else if (price1 < minPrice) {
                    minPrice = price1;
                }
                if ((newSpec && oldStock1 >= 0) || (!newSpec && oldStock1 < 0)) {
                    jsonResult.setMsg(msgPre + "参数错误: oldStock");
                    return jsonResult.toString();
                }
                if (stock1 < 0) {
                    jsonResult.setMsg(msgPre + "库存不得为负库存");
                    return jsonResult.toString();
                }
                if (weight1 < 1) {
                    jsonResult.setMsg(msgPre + "重量不得低于1");
                    return jsonResult.toString();
                }
                GoodsSpecEntity goodsSpecEntity = new GoodsSpecEntity();
                if (id > 0 && !newSpec) {
                    goodsSpecEntity.setId(specId);
                }
                goodsSpecEntity.setIdx(i);
                goodsSpecEntity.setPrice(price1);
                if (newSpec) {
                    goodsSpecEntity.setStock(stock1);
                } else {
                    goodsSpecEntity.setStock(stock1 - oldStock1);
                }

                goodsSpecEntity.setWeight(weight1);
                goodsSpecEntity.setDes(itemsDesc.get(i));
                goodsSpecEntities[i] = goodsSpecEntity;
            }
            goodsEntity.setPrice(minPrice);
            goodsEntity.setStock(0);
            goodsEntity.setWeight(0);
        }
        if (id == 0 && !newSpec) {
            jsonResult.setMsg("新建商品时不能修改规格: specId不得大于0");
            return jsonResult.toString();
        }
        if (itemsDesc.size() > 0 && !newSpec) {
            var specList = goodsSpecRepository.findAllByGoodsIdForUpdate(id);
            if (specList.size() != itemsDesc.size()) {
                jsonResult.setMsg("要修改的规格数量与原规格数量不一致");
                return jsonResult.toString();
            }
            for (int i = 0; i < itemsDesc.size(); i++) {
                if (goodsSpecEntities[i].getId() != specList.get(i).getId()) {
                    jsonResult.setMsg("规格数据错误");
                    return jsonResult.toString();
                }
                goodsSpecEntities[i].setStock(specList.get(i).getStock() + goodsSpecEntities[i].getStock());
                if (goodsSpecEntities[i].getStock() < 0) {
                    goodsSpecEntities[i].setStock(0);
                }
            }
        }
        goodsRepository.save(goodsEntity);
        for (int i = 0; i < itemsDesc.size(); i++) {
            goodsSpecEntities[i].setGoodsId(goodsEntity.getId());
            goodsSpecRepository.save(goodsSpecEntities[i]);
        }
        if (id > 0) {
            var deleteSpecIds = Arrays.stream(goodsSpecEntities).map(GoodsSpecEntity::getId).collect(Collectors.toList());
            if (deleteSpecIds.size() == 0) {
                deleteSpecIds.add(0L);
            }
            goodsSpecRepository.deleteByGoodsIdAndIdNotIn(id, deleteSpecIds);
        }
        if (id > 0) {
            jsonResult.setMsg("已完成商品修改");
        } else {
            jsonResult.setMsg("已完成新建商品");
        }
        goodsRepository.flush();
        GoodsCache.init();
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        String sql = "select * from t_goods order by recommend desc,update_time desc,id desc";
        Pagination pagination = Pagination.newBuilder(sql)
                .page(request)
                .build();
        for (var row : pagination.getRows()) {
            String cateName = "";
            String img = "";
            String imgs = (String) row.get("imgs");
            if (imgs != null) {
                img = imgs.split(",")[0];
            }
            CategoryEntity categoryEntity = CategoryCache.getEntityById(Helper.longValue(row.get("cateId")));
            if (categoryEntity != null) {
                cateName = categoryEntity.getName();
            }
            row.put("cateName", cateName);
            row.put("img", img);
        }
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/goods/list", request);
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", "商品列表");
        return modelAndView;
    }

    static class Spec {
        private String name;
        private List<SpecItem> list;

        public List<SpecItem> getList() {
            return list;
        }

        public void setList(List<SpecItem> list) {
            this.list = list;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class SpecItem {
        private String val;
        private String hint;
        private String img;

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }

}
