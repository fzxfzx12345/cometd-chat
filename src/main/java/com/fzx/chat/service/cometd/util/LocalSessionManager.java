package com.fzx.chat.service.cometd.util;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;

@Service
@Scope("singleton")
public class LocalSessionManager {

    private final Logger logger = LoggerFactory.getLogger(LocalSessionManager.class);

    private LocalSession localSession;

    @Autowired
    private ServletContext servletContext;

    public LocalSession getLocalSession() {
        if (localSession == null) {
            BayeuxServer bayeuxServer = (BayeuxServer) servletContext.getAttribute(BayeuxServer.ATTRIBUTE);
            localSession = bayeuxServer.newLocalSession("LocalSessionManager");
            localSession.handshake();
        }
        logger.info("localSession:{}", localSession);
        return localSession;
    }
}
