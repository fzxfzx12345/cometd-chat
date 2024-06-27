package com.tom.chat;


import com.tom.chat.service.cometd.init.CometDInitiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;


import javax.servlet.ServletContext;


@SpringBootApplication
public class TomChatApplication implements ServletContextInitializer, ApplicationRunner {


    @Autowired
    private CometDInitiator cometDInitiator;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TomChatApplication.class);
        application.run(args);
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        cometDInitiator.start();
    }

    @Override
    public void run(ApplicationArguments args) {
        cometDInitiator.init();
    }
}
