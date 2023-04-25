package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_goods")
public class GoodsEntity extends BaseEntity implements RowMapper<GoodsEntity> {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long brandId;
    private Long cateId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private String name;
    @Column(columnDefinition = "text")
    private String des;
    @Column(columnDefinition = "text")
    private String imgs;
    private Long price;
    private Long stock;
    private Long weight;
    private Long recommend;
    @Column(columnDefinition = "text")
    private String spec;
    private Long status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getRecommend() {
        return recommend;
    }

    public void setRecommend(Long recommend) {
        this.recommend = recommend;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    @Override
    public GoodsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        GoodsEntity entity = new GoodsEntity();
        entity.setId(rs.getLong("id"));
        entity.setBrandId(rs.getLong("brand_id"));
        entity.setCateId(rs.getLong("cate_id"));
        entity.setCreateTime(rs.getTimestamp("create_time"));
        entity.setDes(rs.getString("des"));
        entity.setImgs(rs.getString("imgs"));
        entity.setName(rs.getString("name"));
        entity.setPrice(rs.getLong("price"));
        entity.setRecommend(rs.getLong("recommend"));
        entity.setSpec(rs.getString("spec"));
        entity.setStatus(rs.getLong("status"));
        entity.setStock(rs.getLong("stock"));
        entity.setUpdateTime(rs.getTimestamp("update_time"));
        entity.setWeight(rs.getLong("weight"));
        return entity;
    }
}
