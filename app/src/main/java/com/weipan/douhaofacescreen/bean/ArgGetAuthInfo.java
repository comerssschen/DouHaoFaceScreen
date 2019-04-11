package com.weipan.douhaofacescreen.bean;

/**
 * 作者：create by comersss on 2019/4/11 10:57
 * 邮箱：904359289@qq.com
 */
public class ArgGetAuthInfo {


    /**
     * Code : 200
     * Msg : 授权成功
     * Data : {"authinfo":"3acpp+EynZ8tiVxBjPDdPPxNAwYZijd+G4ZXI28X/Gni+ESxzso93ptb0LXhJnkXUBtYFUXYkx8IS/5uit5chG1aza1wAFeaFXnq+Q9qKFmUkGi+j5ijTAB92yIrnBvV1dCXdZdyMTcwSnXkgGmOV5oPED7l8UGj9D1x+ZSdeuoXHaUYTGO9t4g/2SFHVaEh6Te7fNEbptxgNPNgyx6zI7oCbgQH4FGk9RGPHOA0fvhRmNqIXZ3z92Sn9J+SHFkFDKvWhXyYVYJA6REDGla4JTA/QcVn1TipC+kxc4A309BZGHklAlXUdgRwYcrdoVIMG3C/jkoYmQQlyerK7+LxPNfBxuHL7ep/H0E1kOh2C16V7qIpSxRJ9UKtqYOpOW8ybyuHz1WB5ZOAgzKSKYZGmk4I7DJdsaudjDQwPWd1LLn4lpXq9H9Z8zEopwv/4ht3xrf/TWr2181GMGaw0Q8Arm4sf6iLzwibu35DfhP2gx3XKt04giR/Zt+rDlAOwuRjOWFpFDgCpHthZXDLt+B//ZhNcs8cGTmmLaCxfByDZ7ti8XyftjscXCZRw28x51bf3ep3iWxVFOKcx7XjSR+1Vhaa8I/q8H1Ug5hhElhOcffbj/1OdkuejaxCBy1waYL8XuH2Xiq0jgxlXg==","expires_in":"3600"}
     */

    private String Code;
    private String Msg;
    private DataBean Data;

    public String getCode() {
        return Code;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String Msg) {
        this.Msg = Msg;
    }

    public DataBean getData() {
        return Data;
    }

    public void setData(DataBean Data) {
        this.Data = Data;
    }

    public static class DataBean {
        /**
         * authinfo : 3acpp+EynZ8tiVxBjPDdPPxNAwYZijd+G4ZXI28X/Gni+ESxzso93ptb0LXhJnkXUBtYFUXYkx8IS/5uit5chG1aza1wAFeaFXnq+Q9qKFmUkGi+j5ijTAB92yIrnBvV1dCXdZdyMTcwSnXkgGmOV5oPED7l8UGj9D1x+ZSdeuoXHaUYTGO9t4g/2SFHVaEh6Te7fNEbptxgNPNgyx6zI7oCbgQH4FGk9RGPHOA0fvhRmNqIXZ3z92Sn9J+SHFkFDKvWhXyYVYJA6REDGla4JTA/QcVn1TipC+kxc4A309BZGHklAlXUdgRwYcrdoVIMG3C/jkoYmQQlyerK7+LxPNfBxuHL7ep/H0E1kOh2C16V7qIpSxRJ9UKtqYOpOW8ybyuHz1WB5ZOAgzKSKYZGmk4I7DJdsaudjDQwPWd1LLn4lpXq9H9Z8zEopwv/4ht3xrf/TWr2181GMGaw0Q8Arm4sf6iLzwibu35DfhP2gx3XKt04giR/Zt+rDlAOwuRjOWFpFDgCpHthZXDLt+B//ZhNcs8cGTmmLaCxfByDZ7ti8XyftjscXCZRw28x51bf3ep3iWxVFOKcx7XjSR+1Vhaa8I/q8H1Ug5hhElhOcffbj/1OdkuejaxCBy1waYL8XuH2Xiq0jgxlXg==
         * expires_in : 3600
         */

        private String authinfo;
        private String expires_in;

        public String getAuthinfo() {
            return authinfo;
        }

        public void setAuthinfo(String authinfo) {
            this.authinfo = authinfo;
        }

        public String getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(String expires_in) {
            this.expires_in = expires_in;
        }
    }
}
