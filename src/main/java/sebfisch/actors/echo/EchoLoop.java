package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.echo.EchoLoop.Echo;

public class EchoLoop extends AbstractBehavior<Echo> {
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        ActorSystem<Echo> echoLoop = //
            ActorSystem.create(EchoLoop.create(), "echo-loop");
        
        System.out.println("Type 'quit' to exit, something else to send");
        try(Stream<String> lines = STDIN.lines()) {
            lines
                .takeWhile(Predicate.not("quit"::equals))
                .forEach(line -> echoLoop.tell(new Echo(line)));
        } finally {
            echoLoop.terminate();
        }
    }

    public static class Echo {
        public final String text;
        public Echo(String text) {
            this.text = text;
        }
    }

    public static Behavior<Echo> create() {
        return Behaviors.setup(EchoLoop::new);
    }

    private ActorRef<EchoServer.Request> server;
    private ActorRef<EchoClient.Request> client;

    private EchoLoop(ActorContext<Echo> ctx) {
        super(ctx);
        server = ctx.spawn(EchoServer.create(), "echo-server");
        client = ctx.spawn(EchoClient.create(), "echo-client");
    }

    @Override
    public Receive<Echo> createReceive() {
        return newReceiveBuilder()
            .onAnyMessage(this::echo)
            .build();
    }

    private EchoLoop echo(Echo msg) {
        client.tell(new EchoClient.Send(msg.text, server));
        return this;
    }
}
