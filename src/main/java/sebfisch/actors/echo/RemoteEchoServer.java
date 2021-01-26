package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;

public class RemoteEchoServer {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 25520;
    public static final String NAME = "echo-server";
    public static final String PATH =
        "akka://" + NAME + "@" + HOST + ":" + PORT + "/user";

    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {        
        ActorSystem<EchoServer.Request> echoServer =
            ActorSystem.create(EchoServer.create(), NAME, remoteConf());
        
        System.out.println("Type 'quit' to exit");
        try (Stream<String> lines = STDIN.lines()) {
            lines.filter("quit"::equals).findFirst();
        } finally {
            echoServer.terminate();
        }
    }

    private static Config remoteConf() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.hostname", HOST);
        overrides.put("akka.remote.artery.canonical.port", PORT);
        return ConfigFactory
            .parseMap(overrides)
            .withFallback(ConfigFactory.load());
    }
}
