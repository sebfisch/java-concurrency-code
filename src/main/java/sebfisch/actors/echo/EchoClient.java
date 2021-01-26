package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import akka.actor.ActorSelection;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.echo.EchoClient.Request;

public class EchoClient extends AbstractBehavior<Request> {
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));
    
    private static final String SERVER =
        "akka://echo-server@127.0.0.1:" + EchoServer.PORT + "/user";

    public static void main(String[] args) {
        ActorSystem<Request> echoClient = 
            ActorSystem.create(EchoClient.create(), "echo-client");
        
        System.out.println("Type 'quit' to exit, something else to send");
        try (Stream<String> lines = STDIN.lines()) {
            lines
                .takeWhile(Predicate.not("quit"::equals))
                .forEach(line -> echoClient.tell(new Send(line)));
        } finally {
            echoClient.terminate();
        }
    }

    interface Request {}

    private static class Send implements Request {
        final String text;
        Send(String text) {
            this.text = text;
        }
    }

    private static class Log implements Request {
        final EchoServer.Response response;
        Log(EchoServer.Response response) {
            this.response = response;
        }
    }

    private final ActorSelection server;

    public static Behavior<Request> create() {
        return Behaviors.setup(EchoClient::new);
    }

    private EchoClient(ActorContext<Request> ctx) {
        super(ctx);
        this.server = ctx.getSystem().classicSystem().actorSelection(SERVER);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
            .onMessage(Send.class, this::respond)
            .onMessage(Log.class, this::respond)
            .build();
    }

    private Behavior<Request> respond(Send msg) {
        final ActorRef<EchoServer.Response> logAdapter =
            getContext().messageAdapter(EchoServer.Response.class, Log::new);
        server.tell(
            new EchoServer.Request(logAdapter, msg.text),
            Adapter.toClassic(getContext().getSelf()));
        return this;
    }

    private Behavior<Request> respond(Log msg) {
        System.out.printf("received: %s%n", msg.response.text);
        return this;
    }
}
