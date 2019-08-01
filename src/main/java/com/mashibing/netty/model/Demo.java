package com.mashibing.netty.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Demo implements Serializable {

    private Integer id;
    private String des;

    public Demo() {
    }

    public Demo(Integer id, String des) {
        this.id = id;
        this.des = des;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "id=" + id +
                ", des='" + des + '\'' +
                '}';
    }
}
