package org.rock.vtcache;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Application entry-point
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.rock.vtcache","org.rock.vtcache.web"})
@EnableJpaRepositories("org.rock.vtcache.repository")
@EntityScan("org.rock.vtcache.domain")
public class VtcacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtcacheApplication.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer() {
        return (registry) -> registry.config().commonTags("application", "vtcache");
    }
}
