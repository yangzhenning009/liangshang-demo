package com.liangshan.demo.service;

import java.math.BigDecimal;
import java.util.Objects;

public final class Cmp {

    private Cmp() {
    }

    public static boolean eq(Object left, Object right) {
        if (left instanceof BigDecimal leftNumber && right instanceof BigDecimal rightNumber) {
            return leftNumber.compareTo(rightNumber) == 0;
        }
        return Objects.equals(left, right);
    }
}
