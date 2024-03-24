package net.dontcode.common.test.mongo;

import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.transitions.Mongod;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Starts a local mongo instance for unit test. Not needed anymore.
 */
public class EmbeddedMongoHelper {
    static private Mongod _mongod;

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

    public static Mongod getMongod () {
        return _mongod;
    }

    public static MongoClient getMongoClient () {
        if( _mongo ==null) {
            configureMongo();
        }
        return _mongo;
    }
    /*protected static MongodConfigBuilder createMongodConfigBuilder() throws UnknownHostException, IOException {
        return new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()));
    }*/

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
        /*if( _mongod!=null)
            _mongod.stop();
*/
    }
}
