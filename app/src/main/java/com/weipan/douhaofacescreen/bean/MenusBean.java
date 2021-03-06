package com.weipan.douhaofacescreen.bean;

import java.io.Serializable;

/**
 * Created by highsixty on 2018/3/9.
 * mail  gaolulin@sunmi.com
 */

public class MenusBean implements Serializable {
    private String id;//
    private String name;//名称
    private String money;//总价
    private String code;//商品code
    private String unit;//单位
    private String unitPrice;//单价
    private int count;//数量
    private int imgId;
    private String imgUrl;

    public MenusBean() {
    }

    public MenusBean(String id, String name, String money, String code, String unit, String unitPrice, int count, int imgId, String imgUrl) {
        this.id = id;
        this.name = name;
        this.money = money;
        this.code = code;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.count = count;
        this.imgId = imgId;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }


    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

}
