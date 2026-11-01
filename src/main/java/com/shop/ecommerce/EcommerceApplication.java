package com.shop.ecommerce;

import com.shop.ecommerce.cli.EcommerceConsoleMenu;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.util.Arrays;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(EcommerceApplication.class, args);

        boolean consoleRequested = Arrays.asList(args).contains("--console") ||
                "true".equalsIgnoreCase(ctx.getEnvironment().getProperty("app.console.enabled"));

        if (consoleRequested) {
            DataSource ds = ctx.getBean(DataSource.class);
            EcommerceConsoleMenu menu = new EcommerceConsoleMenu(ds);
            menu.startMenu();
        }
    }
}
