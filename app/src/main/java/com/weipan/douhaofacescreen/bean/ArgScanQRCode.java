package com.weipan.douhaofacescreen.bean;

/**
 * 作者：create by comersss on 2018/11/16 09:32
 * 邮箱：904359289@qq.com
 * 扫描二维码
 */
public class ArgScanQRCode {
    private String cash_id;
    private int client;
    private String auth_code;
    private String total_fee;
    private String remark;

    public String getCash_id() {
        return cash_id;
    }

    public void setCash_id(String cash_id) {
        this.cash_id = cash_id;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public String getAuth_code() {
        return auth_code;
    }

    public void setAuth_code(String auth_code) {
        this.auth_code = auth_code;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
