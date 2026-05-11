package dev.thilanka.beam.entity.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.context = applicationContext;
    }

    public static ApplicationContext getContext() {
        if (context == null) {
            throw new IllegalStateException(
                    "SpringContextHolder not initialised yet — " +
                            "ensure this bean is loaded before any JPA listeners fire.");
        }
        return context;
    }
}
