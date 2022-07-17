package net.dontcode.common.test.mongo;

import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.net.*;

public class EmbeddedMongoHelper {
    /**
     * please store Starter or RuntimeConfig in a static final field
     * if you want to use artifact store caching (or else disable caching)
     */
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    static private MongodExecutable _mongodExe;
    static private MongodProcess _mongod;

    static private MongoClient _mongo;
    static private int port = 27017;

    public static void configureMongo () {
        /*if( _mongo == null) {
                // Run embedded mongo if no databases are running
             if( !serverListening(port)) {

            try {
                _mongodExe = starter.prepare(createMongodConfig());
                _mongod = _mongodExe.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            _mongo = MongoClients.create("mongodb://localhost:" + port);
        }*/
    }

    public static int port() {
        return port;
    }

    public static MongodProcess getMongod () {
        return _mongod;
    }

    public static MongodExecutable getMongodExe () {
        return _mongodExe;
    }

    protected static IMongodConfig createMongodConfig() throws UnknownHostException, IOException {
        return createMongodConfigBuilder().build();
    }

    public static MongoClient getMongoClient () {
        if( _mongo ==null) {
            configureMongo();
        }
        return _mongo;
    }
    protected static MongodConfigBuilder createMongodConfigBuilder() throws UnknownHostException, IOException {
        return new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()));
    }

    public static boolean serverListening( int port)
    {
        boolean ret=true;
        Socket s = null;
        try {
            s = new Socket();
            s.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(InetAddress.getLocalHost(), port);
            s.connect(sa, 1000);
        } catch (IOException e) {
            ret=false;
        } finally {
            if (s != null) {
                if ( s.isConnected()) {
                    ret = true;
                } else {
                    ret = false;
                }
                try {
                    s.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }


    public static void closeMongo() {
        if( _mongod!=null)
            _mongod.stop();
        if( _mongodExe!=null)
            _mongodExe.stop();

    }
}
