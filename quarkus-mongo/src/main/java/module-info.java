module net.dontcode.common.quarkus.mongo {
    exports net.dontcode.common.mongo;
    exports net.dontcode.common.session;
    exports net.dontcode.common.websocket;

    requires net.dontcode.core;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.ws.rs;
    requires java.xml.bind;
    requires jakarta.websocket.api;
    requires quarkus.mongodb.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires io.smallrye.mutiny;
    requires microprofile.config.api;
    requires org.slf4j;
    requires jakarta.enterprise.cdi.api;
    requires jakarta.inject.api;
}
