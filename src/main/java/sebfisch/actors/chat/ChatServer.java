package sebfisch.actors.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.JsonSerializable;
import sebfisch.actors.chat.ChatClient.Event;
import sebfisch.actors.chat.ChatServer.Request;

public class ChatServer extends AbstractBehavior<Request> {

    public interface Request extends JsonSerializable {}

    public static class Login implements Request {
        public final String name;
        public final ActorRef<ChatClient.Event> client;

        public Login(String name, ActorRef<Event> client) {
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

        public Quit(@JsonProperty("client") ActorRef<Event> client) {
            this.client = client;
        }
    }

    private Map<ActorRef<ChatClient.Event>, String> clients = new HashMap<>();

    public static Behavior<Request> create() {
        return Behaviors.setup(ChatServer::new);
    }

    private ChatServer(ActorContext<Request> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
            .onMessage(Login.class, this::respond)
            .onMessage(Send.class, this::respond)
            .onMessage(Quit.class, this::respond)
            .build();
    }

    private ChatServer respond(Login msg) {
        Set<String> names = getNames();
        if (names.contains(msg.name)) {
            msg.client.tell(ChatClient.Error.NAME_ALREADY_TAKEN);
        } else {
            ActorContext<ChatServer.Request> ctx = getContext();
            ActorRef<ChatServer.Request> self = ctx.getSelf();
            clients.keySet().forEach(other -> {
                other.tell(new ChatClient.StatusChanged(msg.name, "joined"));
            });
            // ctx.watchWith(msg.client, new Quit(msg.client));
            msg.client.tell(new ChatClient.LoggedIn(names, self));
            clients.put(msg.client, msg.name);
        }
        return this;
    }

    private ChatServer respond(Send msg) {
        String name = clients.get(msg.client);
        if (name == null) {
            msg.client.tell(ChatClient.Error.NOT_LOGGED_IN);
        } else {
            clients.keySet().forEach(other -> {
                if (!msg.client.equals(other)) {
                    other.tell(new ChatClient.NewMessage(name, msg.text));
                }
            });
        }
        return this;
    }

    private ChatServer respond(Quit msg) {
        String name = clients.get(msg.client);
        if (name != null) {
            clients.remove(msg.client);
            clients.keySet().forEach(other -> {
                other.tell(new ChatClient.StatusChanged(name, "left"));
            });
        }
        return this;
    }

    private Set<String> getNames() {
        return new HashSet<>(clients.values());
    }
}
