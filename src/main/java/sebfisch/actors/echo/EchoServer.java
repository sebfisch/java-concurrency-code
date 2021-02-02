package sebfisch.actors.echo;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import sebfisch.actors.JsonSerializable;

public class EchoServer {

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
        return null; // TODO [Task 2.1, Actors] complete echo server
    }
}
