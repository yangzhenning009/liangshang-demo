package com.liangshan.demo.seed;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SeedRunner {

    private final List<SeedStage> stages;

    public SeedRunner(List<SeedStage> stages) {
        this.stages = stages.stream().sorted(Comparator.comparingInt(SeedStage::order)).toList();
    }

    public List<StageView> stages() {
        return stages.stream()
                .map(stage -> new StageView(stage.code(), stage.order(), stage.label()))
                .toList();
    }

    public List<String> run() {
        List<String> codes = new ArrayList<>();
        for (SeedStage stage : stages) {
            stage.run();
            codes.add(stage.code());
        }
        return codes;
    }

    public record StageView(String code, int order, String label) {
    }
}
