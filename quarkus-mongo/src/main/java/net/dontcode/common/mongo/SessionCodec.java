package net.dontcode.common.mongo;

import com.mongodb.MongoClientSettings;
import net.dontcode.common.session.Session;
import net.dontcode.common.session.SessionActionType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SessionCodec implements Codec<Session> {
    private final Codec<Document> documentCodec;
    private final ChangeCodec changeCodec;

    public SessionCodec() {

        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
        this.changeCodec = new ChangeCodec();
    }

    @Override
    public void encode(BsonWriter writer, Session session, EncoderContext encoderContext) {
        Document doc = new Document();
        doc.put("id", session.id());
        if( session.time() != null)
            doc.put("time", session.time().toInstant());
        if( session.type() != null)
            doc.put("type", session.type().name());
        if( session.srcInfo() != null)
            doc.put("srcInfo", session.srcInfo());
        if( session.change()!=null)
            doc.put("change", changeCodec.toDocument(session.change()));
        documentCodec.encode(writer, doc, encoderContext);
    }


    @Override
    public Class<Session> getEncoderClass() {
        return Session.class;
    }

    @Override
    public Session decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        var changeDoc =document.get("change", Document.class);
        Session session = new Session(document.getString("id"),
                ZonedDateTime.ofInstant(document.getDate("time").toInstant(), ZoneId.systemDefault()),
                SessionActionType.valueOf(document.getString("type")),
                document.getString("srcInfo"),
                changeCodec.fromDocument(changeDoc));
        return session;
    }
}
