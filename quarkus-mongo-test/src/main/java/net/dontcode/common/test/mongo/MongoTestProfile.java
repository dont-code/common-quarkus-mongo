package net.dontcode.common.test.mongo;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MongoTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        //System.out.println("Database is"+ EmbeddedMongoHelper.serverListening(2017));
        return Map.of(/*"quarkus.mongodb.projects.connection-string","mongodb://localhost:27017",*/ "projects-database-name","unitTestProjectDb");
    }
}
