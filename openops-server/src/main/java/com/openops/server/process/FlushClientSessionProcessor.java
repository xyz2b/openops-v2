package com.openops.server.process;

import com.openops.common.ProtoInstant;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.server.session.dao.ClientCacheDAO;
import com.openops.server.session.dao.SessionCacheDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("FlushClientSessionProcessor")
public class FlushClientSessionProcessor extends AbstractProcessor {
    @Autowired
    ClientCacheDAO clientCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    public FlushClientSessionProcessor() {
        super(ProtoInstant.ProcessorType.FLUSH_CLIENT_SESSION_PROCESSOR);
    }

    @Override
    public boolean action(Session session, Object message) throws Exception {
        String clientId = session.client().getClientId();
        String sessionId = session.sessionId();

        clientCacheDAO.flush(clientId);
        sessionCacheDAO.flush(sessionId);
        return true;
    }
}
