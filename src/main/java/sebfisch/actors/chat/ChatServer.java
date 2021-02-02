package sebfisch.actors.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import sebfisch.actors.JsonSerializable;

public class ChatServer {

    public interface Request extends JsonSerializable {}

    public static class Login implements Request {
        public final String name;
        public final ActorRef<ChatClient.Event> client;

        public Login(String name, ActorRef<ChatClient.Event> client) {
            this.name = name;
            this.client = client;
        }
    }

    public static class Send implements Request {
        public final String text;
        public final ActorRef<ChatClient.Event> client;

        public Send(String text, ActorRef<ChatClient.Event> client) {
            this.text = text;
            this.client = client;
        }
    }

    public static class Quit implements Request {
        public final ActorRef<ChatClient.Event> client;

        public Quit(@JsonProperty("client") ActorRef<ChatClient.Event> client) {
            this.client = client;
        }
    }

    public static Behavior<Request> create() {
        return null; // TODO [Task 6, Actors] complete chat server
    }
}
