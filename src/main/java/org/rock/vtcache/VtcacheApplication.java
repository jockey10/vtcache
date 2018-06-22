package org.rock.vtcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"org.rock.vtcache.web"})
@EnableJpaRepositories("org.rock.vtcache.repository")
@EntityScan("org.rock.vtcache.domain")
public class VtcacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtcacheApplication.class, args);
    }
}
