package sebfisch.actors.chat;

import java.util.Set;
import java.util.stream.Collectors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.JsonSerializable;
import sebfisch.actors.chat.ChatClient.Event;

public class ChatClient extends AbstractBehavior<Event> {
    public interface Event extends JsonSerializable {}

    public static class Started implements Event {
        public final String path;

        public Started(String path) {
            this.path = path;
        }
    }

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

    public static class StatusChanged implements Event {
        public final String user;
        public final String status;

        public StatusChanged(String user, String status) {
            this.user = user;
            this.status = status;
        }
    }

    public enum Exiting implements Event { INSTANCE }

    public static Behavior<Event> create(String name) {
        return Behaviors.setup(ctx -> new ChatClient(ctx, name));
    }

    private final String name;
    private ActorRef<ChatServer.Request> server;

    private ChatClient(ActorContext<Event> ctx, String name) {
        super(ctx);
        this.name = name;
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder()
            .onMessage(Started.class, this::respond)
            .onMessage(Error.class, this::respond)
            .onMessage(LoggedIn.class, this::respond)
            .onMessage(NewInput.class, this::respond)
            .onMessage(NewMessage.class, this::respond)
            .onMessage(StatusChanged.class, this::respond)
            .onMessage(Exiting.class, this::respond)
            .build();
    }

    private ChatClient respond(Started msg) {
        ActorContext<Event> ctx = getContext();
        ActorRef<Event> self = ctx.getSelf();
        ctx.getSystem().classicSystem().actorSelection(msg.path) //
            .tell(new ChatServer.Login(name, self), Adapter.toClassic(self));
        return this;
    }

    private ChatClient respond(Error error) {
        System.out.println(error.message);
        return this;
    }

    private ChatClient respond(LoggedIn msg) {
        server = msg.server;
        if (msg.users.isEmpty()) {
            System.out.println("There are no other logged in users.");
        } else {
            System.out.printf("Logged in users: %s%n", //
                msg.users.stream().collect(Collectors.joining(", ")));
        }
        return this;
    }

    private ChatClient respond(NewInput msg) {
        if (server == null) {
            getContext().getSelf().tell(Error.NOT_LOGGED_IN);
        } else {
            server.tell(new ChatServer.Send(msg.text, getContext().getSelf()));
        }
        return this;
    }

    private ChatClient respond(NewMessage msg) {
        System.out.printf("%s: %s%n", msg.user, msg.text);
        return this;
    }

    private ChatClient respond(StatusChanged msg) {
        System.out.printf("%s %s the chat.%n", msg.user, msg.status);
        return this;
    }

    private ChatClient respond(Exiting msg) {
        if (server == null) {
            getContext().getSelf().tell(Error.NOT_LOGGED_IN);
        } else {
            server.tell(new ChatServer.Quit(getContext().getSelf()));
        }
        return this;
    }
}
