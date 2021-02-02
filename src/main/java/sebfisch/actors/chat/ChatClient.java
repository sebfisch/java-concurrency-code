package sebfisch.actors.chat;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.JsonSerializable;
import sebfisch.actors.chat.ChatClient.Event;

public class ChatClient extends AbstractBehavior<Event> {
    public interface Event extends JsonSerializable {}

    private enum Created implements Event { INSTANCE }

    public enum Error implements Event { 
        NOT_LOGGED_IN("You are not logged in."),
        NAME_ALREADY_TAKEN("Your name is already taken.");

        public final String message;

        Error(String message) {
            this.message = message;
        }
    }

    public static class LoggedIn implements Event {
        public final Set<String> users;
        public final ActorRef<ChatServer.Request> server;

        public LoggedIn(Set<String> users, ActorRef<ChatServer.Request> server) {
            this.users = users;
            this.server = server;
        }
    }

    public static class NewInput implements Event {
        public final String text;

        public NewInput(String text) {
            this.text = text;
        }
    }

    public static class NewMessage implements Event {
        public final String user;
        public final String text;

        public NewMessage(String user, String text) {
            this.user = user;
            this.text = text;
        }
    }

    public static class UserJoined implements Event {
        public final String user;

        public UserJoined(@JsonProperty("user") String user) {
            this.user = user;
        }
    }

    public static class UserLeft implements Event {
        public final String user;

        public UserLeft(@JsonProperty("user") String user) {
            this.user = user;
        }
    }

    public static Behavior<Event> create(String name, String path) {
        return Behaviors.setup(ctx -> new ChatClient(ctx, name, path));
    }

    private String name;
    private final String path;
    private ActorRef<ChatServer.Request> server;

    private ChatClient(ActorContext<Event> ctx, String name, String path) {
        super(ctx);
        this.name = name;
        this.path = path;
        ctx.getSelf().tell(Created.INSTANCE);
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder()
            .onMessage(Created.class, this::receive)
            .onMessage(Error.class, this::receive)
            .onMessage(LoggedIn.class, this::receive)
            .onMessage(NewInput.class, this::receive)
            .onMessage(NewMessage.class, this::receive)
            .onMessage(UserJoined.class, this::receive)
            .onMessage(UserLeft.class, this::receive)
            .onSignal(PreRestart.class, signal -> quit())
            .onSignal(PostStop.class, signal -> quit())
            .build();
    }

    private ChatClient receive(Created msg) {
        ActorContext<Event> ctx = getContext();
        ActorRef<Event> self = ctx.getSelf();
        ctx.getSystem().classicSystem().actorSelection(path) //
            .tell(new ChatServer.Login(name, self), Adapter.toClassic(self));
        return this;
    }

    private ChatClient receive(Error error) {
        System.out.println(error.message);
        if (!Error.NAME_ALREADY_TAKEN.equals(error)) {
            throw new IllegalStateException(error.message);
        }
        return this;
    }

    private ChatClient receive(LoggedIn msg) {
        server = msg.server;
        if (msg.users.isEmpty()) {
            System.out.println("There are no other logged in users.");
        } else {
            System.out.printf("Logged in users: %s%n", //
                msg.users.stream().collect(Collectors.joining(", ")));
        }
        return this;
    }

    private ChatClient receive(NewInput msg) {
        server.tell(new ChatServer.Send(msg.text, getContext().getSelf()));
        return this;
    }

    private ChatClient receive(NewMessage msg) {
        System.out.printf("%s: %s%n", msg.user, msg.text);
        return this;
    }

    private ChatClient receive(UserJoined msg) {
        System.out.printf("%s joined the chat.%n", msg.user);
        return this;
    }

    private ChatClient receive(UserLeft msg) {
        System.out.printf("%s left the chat.%n", msg.user);
        return this;
    }

    private ChatClient quit() {
        if (server != null) {
            server.tell(new ChatServer.Quit(getContext().getSelf()));
        }
        return this;
    }
}
