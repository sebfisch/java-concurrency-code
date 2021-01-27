package sebfisch.actors.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;

public class RemoteChatServer {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 25520;
    public static final String NAME = "chat-server";
    public static final String PATH =
        "akka://" + NAME + "@" + HOST + ":" + PORT + "/user";

    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {        
        ActorSystem<ChatServer.Request> chatServer =
            ActorSystem.create(ChatServer.create(), NAME, remoteConf());
        
        System.out.println("Type 'quit' to exit");
        try (Stream<String> lines = STDIN.lines()) {
            lines.filter("quit"::equals).findFirst();
        } finally {
            chatServer.terminate();
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
