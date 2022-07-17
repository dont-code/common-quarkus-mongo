package net.dontcode.common.test.mongo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@QuarkusTest
public class AbstractMongoTest{

    @BeforeAll
    public static void setUp() throws Exception {
        EmbeddedMongoHelper.configureMongo();
    }


    @AfterAll
    protected static void tearDown() throws Exception {
        EmbeddedMongoHelper.closeMongo();
    }

}
