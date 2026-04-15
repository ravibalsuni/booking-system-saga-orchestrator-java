package com.booking.orchestrator.config;

import brave.Tracing;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    @ConditionalOnMissingBean
    public Tracing braveTracing() {
        return Tracing.newBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public io.micrometer.tracing.Tracer tracer(Tracing tracing) {
        BraveCurrentTraceContext braveCurrentTraceContext =
                new BraveCurrentTraceContext(tracing.currentTraceContext());
        return new BraveTracer(tracing.tracer(), braveCurrentTraceContext);
    }
}