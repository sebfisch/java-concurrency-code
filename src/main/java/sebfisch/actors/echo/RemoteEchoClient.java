package sebfisch.actors.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import akka.actor.typed.ActorSystem;

public class RemoteEchoClient {
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        ActorSystem<EchoClient.Request> echoClient = 
            ActorSystem.create(EchoClient.create(), "echo-client");
        
        System.out.println("Type 'quit' to exit, something else to send");
        try (Stream<String> lines = STDIN.lines()) {
            lines
                .takeWhile(Predicate.not("quit"::equals))
                .forEach(line -> {
                    echoClient.tell(new EchoClient.SendRemote(line,
                        RemoteEchoServer.PATH));
                });
        } finally {
            echoClient.terminate();
        }
    }
}
