package com.retailhub.ordermanagementservice.util;

import java.util.concurrent.atomic.AtomicInteger;

public class OrderIdGenerator {

    private static AtomicInteger currentId = new AtomicInteger(1);

    public static int generateOrderId() {
        int orderId = currentId.getAndIncrement();
        if (orderId > 9999) {
            currentId.set(1);  // Reset to 1 when it reaches 9999
            orderId = 1;       // Reset orderId as well
        }
        return orderId;
    }
}

