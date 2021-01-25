package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.function.Predicate;
import java.util.stream.Stream;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class EchoClient extends AbstractBehavior<EchoClient.Cmd> {
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));
    
    private static final String SERVER =
        "akka://echo-server@127.0.0.1:" + EchoServer.PORT + "/user";
    
    private static final int TIMEOUT = 10;

    public static void main(String[] args) {
        ActorSystem<EchoClient.Cmd> echoClient = 
            ActorSystem.create(EchoClient.create(), "echo-client");
        
        System.out.println("Type 'quit' to exit, something else to send");
        try (Stream<String> lines = STDIN.lines()) {
            lines
                .takeWhile(Predicate.not("quit"::equals))
                .forEach(line -> echoClient.tell(new EchoClient.Send(line)));
        } finally {
            echoClient.terminate();
        }
    }

    public interface Cmd {}

    public static class Send implements EchoClient.Cmd {
        public final String text;
        public Send(String text) {
            this.text = text;
        }
    }

    public static class Log implements EchoClient.Cmd {
        public final EchoServer.Response response;
        public Log(EchoServer.Response response) {
            this.response = response;
        }
    }

    private final ActorRef<EchoServer.Request> server;

    public static Behavior<EchoClient.Cmd> create() {
        return Behaviors.setup(EchoClient::new);
    }

    private EchoClient(ActorContext<EchoClient.Cmd> ctx) {
        super(ctx);
        this.server = ctx.getSystem().classicSystem()
            .actorSelection(SERVER)
            .resolveOne(Duration.ofSeconds(TIMEOUT))
            .thenApply(EchoClient::typed)
            .toCompletableFuture()
            .join();
    }

    private static ActorRef<EchoServer.Request> typed(akka.actor.ActorRef ref) {
        return Adapter.toTyped(ref);
    }

    @Override
    public Receive<EchoClient.Cmd> createReceive() {
        return newReceiveBuilder()
            .onMessage(EchoClient.Send.class, this::send)
            .onMessage(EchoClient.Log.class, this::log)
            .build();
    }

    private Behavior<EchoClient.Cmd> send(EchoClient.Send msg) {
        final ActorRef<EchoServer.Response> logAdapter =
            getContext().messageAdapter(EchoServer.Response.class, Log::new);
        server.tell(new EchoServer.Request(logAdapter, msg.text));
        return this;
    }

    private Behavior<EchoClient.Cmd> log(EchoClient.Log msg) {
        System.out.printf("received: %s%n", msg.response.text);
        return this;
    }
}
