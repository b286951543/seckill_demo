package com.example.demo.redisson;

import java.io.Serializable;

public class CallDTO implements Serializable {
    private int id;
    private String name;

    public String getName() {
        return name;
    }

    public CallDTO setName(String name) {
        this.name = name;
        return this;
    }

    public int getId() {
        return id;
    }

    public CallDTO setId(int id) {
        this.id = id;
        return this;
    }
}
