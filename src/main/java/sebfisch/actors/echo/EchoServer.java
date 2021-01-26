package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.JsonSerializable;
import sebfisch.actors.echo.EchoServer.Request;

public class EchoServer extends AbstractBehavior<Request> {
    public static final int PORT = 25520;

    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", PORT);
        Config config = ConfigFactory
            .parseMap(overrides)
            .withFallback(ConfigFactory.load());
        
        ActorSystem<Request> echoServer =
            ActorSystem.create(EchoServer.create(), "echo-server", config);
        
        System.out.println("Type 'quit' to exit");
        try (Stream<String> lines = STDIN.lines()) {
            lines.filter("quit"::equals).findFirst();
        } finally {
            echoServer.terminate();
        }
    }

    public static class Request implements JsonSerializable {
        public final ActorRef<Response> client;
        public final String text;
        public Request(ActorRef<Response> client, String text) {
            this.client = client;
            this.text = text;
        }
    }

    public static class Response implements JsonSerializable {
        public final String text;
        public Response(@JsonProperty("text") String text) {
            this.text = text;
        }
    }
    
    public static Behavior<Request> create() {
        return Behaviors.setup(EchoServer::new);
    }

    private EchoServer(ActorContext<Request> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder().onAnyMessage(this::respond).build();
    }

    private EchoServer respond(Request msg) {
        System.out.printf("echo: %s%n", msg.text);
        msg.client.tell(new Response(msg.text));
        return this;
    }
}
