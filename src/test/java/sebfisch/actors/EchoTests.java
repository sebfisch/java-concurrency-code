package sebfisch.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import sebfisch.actors.echo.EchoClient;
import sebfisch.actors.echo.EchoServer;

public class EchoTests {
    static final TestKitJunitResource KIT = new TestKitJunitResource();

    // TODO [Task 2.2, Actors] add test for echo server

    @Test
    public void testThatEchoClientSendsGivenMessage() {
        ActorRef<EchoClient.Request> client = KIT.spawn(EchoClient.create());
        
        String text = "hello world";
        TestProbe<EchoServer.Request> server = KIT.createTestProbe();
        client.tell(new EchoClient.Send(text, server.getRef()));

        EchoServer.Request request = server.receiveMessage();
        assertEquals(text, request.text);
    }
}
