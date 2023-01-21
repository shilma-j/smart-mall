package smart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_user_address")
public class UserAddressEntity extends BaseEntity {
    private String address;
    // 默认地址
    private long dft;
    @Id
    private long id;
    // 收货人
    private String consignee;
    private String phone;
    private long region;

    @JsonIgnore
    private long userId;


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDft() {
        return dft;
    }

    public void setDft(long dft) {
        this.dft = dft;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getRegion() {
        return region;
    }

    public void setRegion(long region) {
        this.region = region;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
