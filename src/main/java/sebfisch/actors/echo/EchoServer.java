package sebfisch.actors.echo;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.JsonSerializable;
import sebfisch.actors.echo.EchoServer.Request;

public class EchoServer extends AbstractBehavior<Request> {

    public static class Request implements JsonSerializable {
        public final String text;
        public final ActorRef<Response> client;
        public Request(String text, ActorRef<Response> client) {
            this.text = text;
            this.client = client;
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
        msg.client.tell(new Response(msg.text));
        return this;
    }
}
