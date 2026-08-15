package com.likelion.manyfast.domain.timezone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimezoneConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
