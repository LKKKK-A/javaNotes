package com.lkkkk.controller.utils;

/**
 * @program: springboot
 * @description:
 * @author: LKKKK
 * @create: 2023-03-28 13:55
 **/


public class R {

    private Boolean flag;
    private Object data;

    public R(){}


    public R(Boolean flag){
        this.flag = flag;
    }


    public R(Boolean flag,Object data){
        this.flag = flag;
        this.data = data;
    }


    @Override
    public String toString() {
        return "R{" +
                "flag=" + flag +
                ", data=" + data +
                '}';
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
