package com.github.thomasdarimont.keycloak.events;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MyEventListener implements EventListenerProvider {

    private static final Logger LOG = Logger.getLogger(MyEventListener.class);

    @Override
    public void onEvent(Event event) {
        LOG.infof("CustomUserEvent: %s", event.getType());
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        LOG.infof("CustomAdminEvent: %s %s", event.getOperationType(), event.getResourceType());
    }

    @Override
    public void close() {
    }

    @AutoService(EventListenerProviderFactory.class)
    public static class Factory implements EventListenerProviderFactory {

        private static final MyEventListener INSTANCE = new MyEventListener();

        @Override
        public String getId() {
            return "custom-eventlistener";
        }

        @Override
        public EventListenerProvider create(KeycloakSession session) {
            return INSTANCE;
        }

        @Override
        public void init(Config.Scope config) {
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
        }
    }

}
