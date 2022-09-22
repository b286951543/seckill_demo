package com.example.demo.model;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
public class ProductInventory {
    // 商品库存
    private long inventory;
    // 销售数量
    private AtomicLong saleNum;
}
