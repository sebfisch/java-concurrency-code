package sebfisch.actors.echo;

import java.time.Duration;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.echo.EchoClient.Request;

public class EchoClient extends AbstractBehavior<Request> {

    public interface Request {}

    public static class Send implements Request {
        public final String text;
        public final ActorRef<EchoServer.Request> server;

        public Send(String text, ActorRef<EchoServer.Request> server) {
            this.text = text;
            this.server = server;
        }
    }

    public static class Log implements Request {
        public final EchoServer.Response response;

        public Log(EchoServer.Response response) {
            this.response = response;
        }
    }

    public static class SendRemote implements Request {
        public final String text;
        public final String path;
        
        public SendRemote(String text, String path) {
            this.text = text;
            this.path = path;
        }
    }

    public static Behavior<Request> create() {
        return Behaviors.setup(EchoClient::new);
    }

    private EchoClient(ActorContext<Request> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
            .onMessage(Send.class, this::receive)
            .onMessage(Log.class, this::receive)
            .onMessage(SendRemote.class, this::receive)
            .build();
    }

    private EchoClient receive(Send msg) {
        final ActorRef<EchoServer.Response> logAdapter =
            getContext().messageAdapter(EchoServer.Response.class, Log::new);
        msg.server.tell(new EchoServer.Request(msg.text, logAdapter));
        return this;
    }

    private EchoClient receive(Log msg) {
        System.out.printf("received: %s%n", msg.response.text);
        return this;
    }

    private EchoClient receive(SendRemote msg) {
        final int timeout = 10;
        final ActorContext<Request> ctx = getContext();

        ctx.getSystem().classicSystem().actorSelection(msg.path) //
            .resolveOne(Duration.ofSeconds(timeout)) //
            .thenApply(this::toTyped) //
            .thenAccept(server -> {
                ctx.getSelf().tell(new Send(msg.text, server));
            });
        
        return this;
    }

    private ActorRef<EchoServer.Request> toTyped(akka.actor.ActorRef ref) {
        return Adapter.toTyped(ref);
    }
}
