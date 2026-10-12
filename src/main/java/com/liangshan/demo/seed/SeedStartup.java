package com.liangshan.demo.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeedStartup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedStartup.class);

    private final SeedRunner runner;
    private final Environment environment;

    public SeedStartup(SeedRunner runner, Environment environment) {
        this.runner = runner;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled(args)) {
            return;
        }
        List<String> codes = runner.run();
        log.info("种子已载入：{}", String.join("、", codes));
    }

    private boolean enabled(ApplicationArguments args) {
        if (args.containsOption("seed")) {
            List<String> values = args.getOptionValues("seed");
            if (values == null || values.isEmpty()) {
                return true;
            }
            return values.stream().anyMatch(value -> !"false".equalsIgnoreCase(value));
        }
        return environment.getProperty("liangshan.seed", Boolean.class, false);
    }
}
