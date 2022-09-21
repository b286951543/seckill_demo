package com.example.demo.mock;

import com.example.demo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductMock {
    public static List<Product> getAllProduct(){
        ArrayList<Product> list = new ArrayList<>();
        list.add(Product.builder().id("1").inventory(100L).build());
        list.add(Product.builder().id("2").inventory(100L).build());
        list.add(Product.builder().id("3").inventory(100L).build());
        return list;
    }
}
