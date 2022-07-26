package net.dontcode.common.session;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.quarkus.mongodb.FindOptions;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.mongodb.reactive.ReactiveMongoDatabase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import net.dontcode.core.Change;
import net.dontcode.core.MapOrString;
import net.dontcode.core.Models;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class SessionService {
    private static Logger log = LoggerFactory.getLogger(SessionService.class);

    @Inject
    @MongoClientName("projects")
    ReactiveMongoClient mongoClient;

    @ConfigProperty(name = "projects-database-name")
    String projectDbName;

    public Uni<Session> createNewSession (String id,String srcInfo) {
        Session session = new Session(id, ZonedDateTime.now(), SessionActionType.CREATE, srcInfo, null);
        return getSession().insertOne(session).map(insertOneResult -> {
            return session;
        }).onFailure().invoke(throwable -> {
            log.error("Error InsertingMongo {}", throwable.getMessage());
        });
    }

    public Uni<Session> findSessionCreationEvent (String id) {
        log.debug("Querying for {}", id);
        return getSession().find(and(Filters.eq("id", id), Filters.eq( "type",SessionActionType.CREATE.name()))).toUni();
    }

    public Uni<Session> updateSession (String id, Change change) {
        Session session = new Session(id, ZonedDateTime.now(), SessionActionType.UPDATE, null, change);
        return getSession().insertOne(session).map(insertOneResult -> {
            return session;
        }).onFailure().invoke(throwable -> {
            log.error("Error InsertingMongo {}", throwable.getMessage());
        });
    }

    public Uni<Session> updateSessionStatus (String id, SessionActionType newAction) {
        Session session = new Session(id, ZonedDateTime.now(), newAction, null,null);
        return getSession().insertOne(session).map(insertOneResult -> {
            return session;
        }).onFailure().invoke(throwable -> {
            log.error("Error InsertingMongo {}", throwable.getMessage());
        });
    }

    public Multi<Session> listSessionsInOrder (String id) {

        return getSession().find(new FindOptions().filter(eq("id", id)).sort(Sorts.ascending("time"))).onFailure().invoke(throwable -> {
            log.error("Error Listing Session from Mongo {}", throwable.getMessage());
        });
    }

    /**
     * Returns the content of a session, with the complete content merged from all updates
     * @param id
     * @return
     */
    public Uni<SessionDetail> getSession (String id) {
        Map<String, MapOrString> stackedContent = new LinkedHashMap<>();
        SessionDetail[] stackedDetail = {null};
        return listSessionsInOrder(id).onItem().transform(session -> {
            mergeChange(stackedContent, session.change());
            if (stackedDetail[0] == null) {
                stackedDetail[0] = new SessionDetail(id, session.time(), null, session.srcInfo().equals("demo"), 1, stackedContent);
            } else {
                SessionDetail oldDessionDetail = stackedDetail[0];
                stackedDetail[0] = new SessionDetail(id, oldDessionDetail.startTime(), session.time(), oldDessionDetail.isDemo(),
                        oldDessionDetail.elementsCount() + 1, stackedContent);
            }
            return stackedDetail[0];
        }).collect().last();
    }

    public void mergeChange (Map<String, MapOrString> content, Change toMerge) {
        Models.applyChange(content, toMerge);
    }

    /**
     * Pipeline query:
     $match:
     {
         and: [ {
             time: {
                 $gte: ISODate("2021-09-14T07:56:31.861+00:00"),
                 $lt: ISODate("2022-09-14T18:00:20.049+00:00")
                }
            }
         ]
     }

     $sort:
     {
         time: 1
     }

     $group:
     {
         _id: "$id",
         eltCount: {
             $sum: 1
         },
         startDate: {
             $min: "$time"
         },
         endDate: {
             $max: "$time"
         },
         isDemo: {
             $first: {
                $eq: [
                 "$srcInfo",
                 "demo"
                ]
            }
        }
     }

     $match: {
        isDemo: true
     }
     * @param from
     * @param to
     * @return
     */
    public Multi<SessionOverview> listSessionOverview (ZonedDateTime from, ZonedDateTime to, String srcInfo) {
        ArrayList<Bson> pipeline=new ArrayList();
        if (from!=null || to!=null) {
            String match ="""
              {$match: {
              $and: [ {
                time: {""";
            if (from != null) {
                match = match + "$gte: ISODate(\"" + from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\")";
                if (to != null) match = match + ",\n";
            }
            if (to != null) {
                match = match + "$lt: ISODate(\"" + to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\")";
            }
            match=match+"""
                }}
              ]
            }}""";
            pipeline.add(Document.parse(match));
        }
        String querySrcInfo = srcInfo;
        if (querySrcInfo==null) {
            querySrcInfo = "demo";
        }
        String group="""
            {$group: {
              _id: "$id",
              eltCount: {
                $sum: 1
              },
              startTime: {
                $min: "$time"
              },
              endTime: {
                $max: "$time"
              },
              isDemo: {
                $first: {$eq: [
                   "$srcInfo",
                   """;
        group = group+"\""+querySrcInfo+"\"";
        group = group+"""
                ]
                }
              }
            }}]""";
        pipeline.add(Document.parse(group));
        String sort="""
            {$sort: {
              startTime: -1
            }}""";
        pipeline.add(Document.parse(sort));
        if (srcInfo!=null) {
            String filter = """
                    {$match: {
                        isDemo: true
                        }
                    }""";
            pipeline.add(Document.parse(filter));
        }
        return getSession().aggregate(pipeline, SessionOverview.class).onFailure().invoke(throwable -> {
            log.error("Error Listing Sessions from/to using Mongo {}", throwable.getMessage());
        });
    }

    protected ReactiveMongoCollection<Session> getSession() {
        return getDatabase().getCollection("sessions", Session.class);
    }

    protected ReactiveMongoDatabase getDatabase () {
        return mongoClient.getDatabase(projectDbName);
    }
}
