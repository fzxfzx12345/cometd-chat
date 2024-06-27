package com.tom.chat.service.cometd.init;


import com.tom.chat.config.CometDConfig;
import com.tom.chat.service.cometd.service.ChatService;
import com.tom.chat.service.cometd.service.SecurityService;
import org.cometd.annotation.AnnotationCometDServlet;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import static org.cometd.server.AbstractServerTransport.*;
import static org.cometd.websocket.server.common.AbstractWebSocketTransport.COMETD_URL_MAPPING_OPTION;


@Component
public class CometDInitiator {

    @Autowired
    private CometDConfig cometDConfig;

    @Autowired
    private ServletContext servletContext;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void start() {
        ServletRegistration.Dynamic cometDServlet = servletContext.addServlet("cometd", AnnotationCometDServlet.class);
        cometDServlet.addMapping(cometDConfig.getUrl());
        cometDServlet.setInitParameter(COMETD_URL_MAPPING_OPTION, cometDConfig.getUrl());
        cometDServlet.setInitParameter(TIMEOUT_OPTION, cometDConfig.getBayeux().getTimeout());
        cometDServlet.setInitParameter(MAX_INTERVAL_OPTION, cometDConfig.getBayeux().getMaxInterval());
        cometDServlet.setInitParameter(INTERVAL_OPTION, "0");
//        cometDServlet.setInitParameter("transports",
//                "org.cometd.websocket.server.WebSocketTransport," +
//                "org.cometd.client.transport.LongPollingTransport");
        cometDServlet.setInitParameter("services", ChatService.class.getName());
//        cometDServlet.setInitParameter("ws.idleTimeout", "259200000"); // 设置WebSocket的空闲超时时间，单位是毫秒
//        cometDServlet.setInitParameter("ws.idleTimeout", "10000"); // 设置WebSocket的空闲超时时间，单位是毫秒
        cometDServlet.setAsyncSupported(true);
        cometDServlet.setLoadOnStartup(1);

        // 注册 CrossOriginFilter 过滤器
        FilterRegistration.Dynamic corsFilter = servletContext.addFilter("CORS", CrossOriginFilter.class);
        corsFilter.addMappingForUrlPatterns(null, false, cometDConfig.getUrl());
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,OPTIONS");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        corsFilter.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, "Content-Length,Content-Range");
        corsFilter.setAsyncSupported(true);
    }

    public void init() {
        BayeuxServer bayeuxServer = (BayeuxServer) servletContext.getAttribute(BayeuxServer.ATTRIBUTE);
        bayeuxServer.setSecurityPolicy(new SecurityService(redisTemplate));
        bayeuxServer.addExtension(new AcknowledgedMessagesExtension());
    }

    @Bean
    public BayeuxServer bayeuxServer() {
        return new BayeuxServerImpl();
    }

}
