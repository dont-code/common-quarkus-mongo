package net.dontcode.common.session;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.dontcode.core.Change;
import net.dontcode.core.DontCodeModelPointer;
import net.dontcode.core.MapOrString;
import net.dontcode.core.Models;
import org.bson.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@QuarkusTest
public class SessionServiceTest {


    @Inject
    SessionService sessionService;

    @Test
    public void testSessionCreateFindUpdate() {
        var sessionId = UUID.randomUUID().toString();
        var createdSession = sessionService.createNewSession(sessionId, "Test Info for "+sessionId).await().indefinitely();
        Assertions.assertEquals(createdSession.id(), sessionId);
        Assertions.assertEquals(createdSession.type(), SessionActionType.CREATE);
        Assertions.assertNotNull(createdSession.time());

        var savedSession = sessionService.findSessionCreationEvent(sessionId).await().indefinitely();
        Assertions.assertNotNull(savedSession.time());

        Change chg = new Change(Change.ChangeType.ADD, "creation/name", "NewAppName");
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        sessionService.updateSessionStatus(sessionId, SessionActionType.ERROR).await().indefinitely();

        var value = new JsonObject ("""
                {
                    "name":"EntityName",
                    "fields": [{
                        "name":"FieldA",
                        "type":"string"
                        }]
                }
                """).toBsonDocument();
        var pointer = new DontCodeModelPointer("creation/entities/a", "creation/entities","creation", "creation", "a", Boolean.TRUE);
        chg = new Change(Change.ChangeType.UPDATE, pointer.getPosition(), value, pointer);
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        sessionService.updateSessionStatus(sessionId, SessionActionType.CLOSE).await().indefinitely();
        var listSessions = sessionService.listSessionsInOrder(sessionId).collect().asList().await().indefinitely();
        Assertions.assertEquals(listSessions.size(), 5);

        AtomicReference<Instant> oldTime=new AtomicReference<>(null);
        int[] counter = new int[1];
        counter[0]=0;
        listSessions.forEach(session -> {
           // Assertions.assertTrue((oldTime.get() ==null) || (session.time().isAfter(oldTime.get())));
            //oldTime.set(session.time());
            counter[0] = counter[0]+1;
            if( counter[0]==4) {    // We test thoroughly the update where we send a value as json
                Map jsonValue = (Map) session.change().getValue();
                Assertions.assertEquals(jsonValue.get("name"), "EntityName");
                Assertions.assertEquals(((Map)((List)jsonValue.get("fields")).get(0)).get("name"),"FieldA");
            }
        });

        Assertions.assertEquals(listSessions.get(listSessions.size()-1).type(), SessionActionType.CLOSE);

    }

    @Test
    public void isCorrectlyLoadingListSessionOverview() throws InterruptedException {
            // Create a first batch of session
        ZonedDateTime firstBatchTime = ZonedDateTime.now();
        createDummySession();
        ZonedDateTime secondBatchTime = ZonedDateTime.now();
        Thread.sleep(10);   // Make sure there are significant time difference between the 2 creation...
        createDummySession();

        var allSessions = sessionService.listSessionOverview(null, null, null).collect().asList().await().indefinitely();
        Assertions.assertTrue(allSessions.size()>=2);

        var testSessions = sessionService.listSessionOverview(firstBatchTime, secondBatchTime, null).collect().asList().await().indefinitely();
        Assertions.assertEquals(1, testSessions.size());
    }

    @Test
    public void testMoveChangeIsSavedProperly() {
        var sessionId = UUID.randomUUID().toString();
        var createdSession = sessionService.createNewSession(sessionId, "Test Move Change for "+sessionId).await().indefinitely();
        Assertions.assertEquals(createdSession.id(), sessionId);
        Assertions.assertEquals(createdSession.type(), SessionActionType.CREATE);
        Assertions.assertNotNull(createdSession.time());

        var savedSession = sessionService.findSessionCreationEvent(sessionId).await().indefinitely();
        Assertions.assertNotNull(savedSession.time());

        var value = new JsonObject ("""
                {
                    "name":"EntityName",
                    "fields": {
                        "aa":{
                            "name":"FieldA",
                            "type":"string"
                            }
                        }
                }
                """).toBsonDocument();
        var pointer = new DontCodeModelPointer("creation/entities/a", "creation/entities","creation", "creation", "a", Boolean.TRUE);
        Change chg = new Change(Change.ChangeType.UPDATE, pointer.getPosition(), value, pointer);
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        pointer = new DontCodeModelPointer("creation/entities/b", "creation/entities","creation", "creation", "b", Boolean.TRUE);
        chg = new Change(Change.ChangeType.MOVE, pointer.getPosition(), null, pointer, "creation/entities/a", null);
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        sessionService.updateSessionStatus(sessionId, SessionActionType.CLOSE).await().indefinitely();
        SessionDetail loaded =sessionService.getSession(sessionId).await().indefinitely();

        Assertions.assertNull(Models.findAtPosition((Map<String, Object>) loaded.content(), "creation/entities/a", false));
        Assertions.assertEquals("FieldA", Models.findAtPosition(new MapOrString((Map<String, Object>) loaded.content()), "creation/entities/b/fields/aa/name", false).getString() );

    }

    @Test
    public void isCorrectlyLoadingSessionDetail() throws InterruptedException {
        String sessionID = createComplexSession(4);

        var session = sessionService.getSession(sessionID).await().indefinitely();
        Assertions.assertEquals(4+3, session.elementsCount());

        Assertions.assertEquals(4, Models.findAtPosition(MapOrString.fromObject(session.content()), "creation/entities", false ).getMap().size());
    }

    private void createDummySession() {
        var sessionId = UUID.randomUUID().toString();
        sessionService.createNewSession(sessionId, "Test List Sessions for "+sessionId).await().indefinitely();
        sessionService.findSessionCreationEvent(sessionId).await().indefinitely();

        Change chg = new Change(Change.ChangeType.ADD, "creation/name", "SessionAppName");
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        var value = new JsonObject ("""
                {
                    "name":"EntityName",
                    "fields": [{
                        "name":"FieldA",
                        "type":"string"
                        }]
                }
                """).toBsonDocument();
        var pointer = new DontCodeModelPointer("creation/entities/b", "creation/entities","creation", "creation", "b", Boolean.TRUE);
        chg = new Change(Change.ChangeType.UPDATE, pointer.getPosition(), value, pointer);
        sessionService.updateSession(sessionId, chg).await().indefinitely();
    }

    private String createComplexSession(int elementCount) {
        var sessionId = UUID.randomUUID().toString();
        sessionService.createNewSession(sessionId, "Test Get Sessions for " + sessionId).await().indefinitely();
        sessionService.findSessionCreationEvent(sessionId).await().indefinitely();
        Change chg = new Change(Change.ChangeType.ADD, "creation/name", "SessionAppName");
        sessionService.updateSession(sessionId, chg).await().indefinitely();

        for (int i=0;i<elementCount;i++) {
            var value = new JsonObject("""
                    {
                        "name":"EntityName"""+i+"""
                        ",
                        "fields": [{
                            "name":"FieldA"""+i+"""
                            ",
                            "type":"string"
                            }]
                    }
                    """).toBsonDocument();

            var pointer = new DontCodeModelPointer("creation/entities/a"+i, "creation/entities", "creation", "creation", "a"+i, Boolean.TRUE);
            chg = new Change(Change.ChangeType.UPDATE, pointer.getPosition(), value, pointer);
            sessionService.updateSession(sessionId, chg).await().indefinitely();
        }
        sessionService.updateSessionStatus(sessionId,SessionActionType.CLOSE).await().indefinitely();

        return sessionId;
        }
    }
