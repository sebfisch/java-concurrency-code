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
    
    @Test
    public void testThatEchoServerRespondsWithSentMessage() {
        ActorRef<EchoServer.Request> server = KIT.spawn(EchoServer.create());
        
        String text = "hello world";
        TestProbe<EchoServer.Response> client = KIT.createTestProbe();
        server.tell(new EchoServer.Request(text, client.getRef()));

        EchoServer.Response response = client.receiveMessage();
        assertEquals(text, response.text);
    }

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
