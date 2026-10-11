package com.liangshan.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Periods {

    private Periods() {
    }

    public static String orderError(LocalDate from, LocalDate to) {
        if (from != null && to != null && !to.isAfter(from)) {
            return "结束日须晚于开始日（有效期按左闭右开）";
        }
        return null;
    }

    public static boolean overlaps(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
        if (fromA == null || fromB == null) {
            return false;
        }
        LocalDate endA = toA == null ? LocalDate.MAX : toA;
        LocalDate endB = toB == null ? LocalDate.MAX : toB;
        return fromA.isBefore(endB) && fromB.isBefore(endA);
    }

    public static String overlapError(List<LocalDate> froms, List<LocalDate> tos, String label) {
        for (int i = 0; i < froms.size(); i++) {
            for (int j = i + 1; j < froms.size(); j++) {
                if (overlaps(froms.get(i), tos.get(i), froms.get(j), tos.get(j))) {
                    return label + "的有效期间不能重叠";
                }
            }
        }
        return null;
    }

    public static <T> List<T> mutable(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
