package com.shop.ecommerce;

import com.shop.ecommerce.cli.EcommerceConsoleMenu;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

/**
 * Standalone entry point for running the Console-Based E-Commerce Menu.
 * Disables the embedded web server so it runs purely in interactive terminal mode.
 */
public class EcommerceCLI {
    public static void main(String[] args) {
        System.setProperty("app.console.enabled", "false"); // manage manually
        ConfigurableApplicationContext context = new SpringApplicationBuilder(EcommerceApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

        DataSource ds = context.getBean(DataSource.class);
        EcommerceConsoleMenu menu = new EcommerceConsoleMenu(ds);
        menu.startMenu();
        context.close();
        System.exit(0);
    }
}
