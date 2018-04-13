package com.example.dell.imeidemo.Beans;

import java.util.Date;

/**
 * Created by dell on 2018/4/11.
 */

public class AppAuthBean {
    private int errcode;
    private String errmsg;
    private content content;//嵌套内容
    private class content{
        String did;
        String expire_date;
    }

    public AppAuthBean(int errcode, String errmsg, String did, String expire_date) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.content.did = did;
        this.content.expire_date = expire_date;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getDid(){
        return content.did;
    }

    public void setDid(String did){
        this.content.did = did;
    }

    public String getExpire_data(){return this.content.expire_date;}

    public void setExpire_data(String expire_date){this.content.expire_date = expire_date;}

    @Override
    public String toString() {
        return "返回码：" + errcode + "\n"
                + "返回状态：" + errmsg + "\n"
                + "设备号：" + content.did + "\n"
                + "失效时间：" + content.expire_date + "\n";
    }
}
