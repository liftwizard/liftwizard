package com.example.helloworld.health;

import java.util.Optional;

import com.codahale.metrics.health.HealthCheck;
import com.example.helloworld.core.Template;

public class TemplateHealthCheck extends HealthCheck {
    private final Template template;

    public TemplateHealthCheck(Template template) {
        this.template = template;
    }

    @Override
    protected Result check() throws Exception {
        template.render(Optional.of("woo"));
        template.render(Optional.empty());
        return Result.healthy();
    }
}
