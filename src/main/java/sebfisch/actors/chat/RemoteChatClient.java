package sebfisch.actors.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;

public class RemoteChatClient {
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "Alice";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 44444;

        ActorSystem<ChatClient.Event> chatClient = 
            ActorSystem.create( //
                Behaviors.supervise(
                    ChatClient.create(name, RemoteChatServer.PATH) //
                ).onFailure(SupervisorStrategy.restart()), //
                "chat-client", //
                remoteConf(port));
        
        System.out.println("Type 'quit' to exit, something else to send");
        try (Stream<String> lines = STDIN.lines()) {
            lines
                .takeWhile(Predicate.not("quit"::equals))
                .forEach(line -> {
                    chatClient.tell(new ChatClient.NewInput(line));
                });
        } finally {
            chatClient.terminate();
        }
    }

    private static Config remoteConf(int port) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        return ConfigFactory
            .parseMap(overrides)
            .withFallback(ConfigFactory.load());
    }
}
