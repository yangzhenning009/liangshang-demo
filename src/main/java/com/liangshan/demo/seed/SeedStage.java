package com.liangshan.demo.seed;

/**
 * 一段可重复执行的种子。WP-13、WP-14 各自再提供一个实现，用更大的 {@link #order()} 接在后面。
 * 写入必须走应用层服务，不能直接插表。
 */
public interface SeedStage {

    String code();

    int order();

    String label();

    void run();
}
