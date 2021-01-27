package sebfisch.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.stream.IntStream;

import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import sebfisch.actors.FunctionalActorTests.Bouncer.Ping;
import sebfisch.actors.FunctionalActorTests.Bouncer.Pong;
import sebfisch.actors.FunctionalActorTests.Calculator.AddFraction;
import sebfisch.actors.FunctionalActorTests.Calculator.GetTotal;
import sebfisch.actors.FunctionalActorTests.Computer.Compute;
import sebfisch.actors.FunctionalActorTests.Computer.Forward;
import sebfisch.actors.FunctionalActorTests.Handshake.Self;

public class FunctionalActorTests {
    static final TestKitJunitResource KIT = new TestKitJunitResource();

    static class Terminator {
        static Behavior<Object> create() {
            return Behaviors.receiveMessage(msg -> Behaviors.stopped());
        }
    }

    @Test
    public void testThatTerminatorStopsOnMessage() {
        ActorRef<Object> terminator = KIT.spawn(Terminator.create());
        terminator.tell("");
        KIT.createTestProbe().expectTerminated(terminator);
    }

    static class Bouncer {
        static class Ping {
            final ActorRef<Pong> sender;
            Ping(ActorRef<Pong> sender) {
                this.sender = sender;
            }
        }

        static class Pong {}

        static Behavior<Ping> create() {
            return Behaviors.receiveMessage(ping -> {
                ping.sender.tell(new Pong());
                return Behaviors.same();
            });
        }
    }

    @Test
    public void testThatBouncerBounces() {
        ActorRef<Ping> bouncer = KIT.spawn(Bouncer.create());
        TestProbe<Pong> probe = KIT.createTestProbe();
        IntStream.range(0,5).forEach(i -> {
            bouncer.tell(new Ping(probe.getRef()));
            assertNotNull(probe.receiveMessage());    
        });
    }

    static class Handshake {
        static class Self {
            final ActorRef<Self> sender;

            public Self(ActorRef<Self> sender) {
                this.sender = sender;
            }
        }

        static Behavior<Self> create() {
            return Behaviors.receive((ctx, msg) -> {
                msg.sender.tell(new Self(ctx.getSelf()));
                return Behaviors.same();
            });
        }
    }

    @Test
    public void testThatHandshakeIsAnswered() {
        ActorRef<Self> handshake = KIT.spawn(Handshake.create());
        TestProbe<Self> probe = KIT.createTestProbe();
        handshake.tell(new Self(probe.getRef()));
        assertNotNull(probe.receiveMessage());
    }

    static class Calculator {
        interface Cmd {}

        static class GetTotal implements Cmd {
            final ActorRef<Integer> sender;
            GetTotal(ActorRef<Integer> sender) {
                this.sender = sender;
            }
        }

        static class AddFraction implements Cmd {
            final int numerator;
            final int denominator;
            AddFraction(int numerator, int denominator) {
                this.numerator = numerator;
                this.denominator = denominator;
            }
        }

        static Behavior<Cmd> create(int total) {
            return Behaviors.receive(Cmd.class)
                .onMessage(GetTotal.class, msg -> {
                    msg.sender.tell(total);
                    return Behaviors.same();
                })
                .onMessage(AddFraction.class, msg -> {
                    return create(total + msg.numerator / msg.denominator);
                })
                .build();
        }
    }

    @Test
    public void testThatCalculatorCalculates() {
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(Calculator.create(0));
        TestProbe<Integer> probe = KIT.createTestProbe();

        calculator.tell(new AddFraction(5,2));
        calculator.tell(new AddFraction(1,2));        
        calculator.tell(new GetTotal(probe.getRef()));

        assertEquals(2, (int) probe.receiveMessage());
    }

    @Test
    public void testThatCalculatorStopsOnCrash() {
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(Calculator.create(0));
        calculator.tell(new AddFraction(1,0));
        KIT.createTestProbe().expectTerminated(calculator);
    }

    @Test
    public void testThatResumingCalculatorResumesAfterCrash() {
        Behavior<Calculator.Cmd> resuming = //
            Behaviors.supervise(Calculator.create(0)) //
                .onFailure(SupervisorStrategy.resume());
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(resuming);
        TestProbe<Integer> probe = KIT.createTestProbe();

        calculator.tell(new AddFraction(5,2));
        calculator.tell(new AddFraction(1,0));
        calculator.tell(new GetTotal(probe.getRef()));
        
        assertEquals(2, (int) probe.receiveMessage());
    }

    @Test
    public void testThatRestartingCalculatorRestartsAfterCrash() {
        Behavior<Calculator.Cmd> restarting = //
            Behaviors.supervise(Calculator.create(0)) //
                .onFailure(SupervisorStrategy.restart());
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(restarting);
        TestProbe<Integer> probe = KIT.createTestProbe();

        calculator.tell(new AddFraction(5,2));
        calculator.tell(new AddFraction(1,0));
        calculator.tell(new GetTotal(probe.getRef()));
        
        assertEquals(0, (int) probe.receiveMessage());
    }

    static class Computer {
        interface Request {}

        static class Forward implements Request {
            final Calculator.Cmd cmd;

            public Forward(Calculator.Cmd cmd) {
                this.cmd = cmd;
            }
        }

        static class Compute implements Request {
            final int arg;

            public Compute(int arg) {
                this.arg = arg;
            }
        }

        static Behavior<Request> create() {
            return Behaviors.setup(ctx -> {
                ActorRef<Calculator.Cmd> calc = ctx.spawnAnonymous( //
                    Calculator.create(0));
                ctx.watch(calc); // death pact
                return computer(calc);
            });
        }

        private static Behavior<Request> computer(ActorRef<Calculator.Cmd> calc) {
            return Behaviors.receive(Request.class)
                .onMessage(Forward.class, msg -> {
                    calc.tell(msg.cmd);
                    return Behaviors.same();
                })
                .onMessage(Compute.class, msg -> {
                    calc.tell(new Calculator.AddFraction(100, msg.arg));
                    return Behaviors.same();
                })
                .build();
        }
    }

    @Test
    public void testThatComputerComputes() {
        ActorRef<Computer.Request> computer = KIT.spawn(Computer.create());
        TestProbe<Integer> probe = KIT.createTestProbe();

        computer.tell(new Compute(20));
        computer.tell(new Compute(30));
        computer.tell(new Forward(new GetTotal(probe.getRef())));

        assertEquals(8, (int) probe.receiveMessage());
    }

    @Test
    public void testThatComputerDiesWithCalculator() {
        ActorRef<Computer.Request> computer = KIT.spawn(Computer.create());
        TestProbe<Integer> probe = KIT.createTestProbe();

        computer.tell(new Compute(0));
        probe.expectTerminated(computer);
    }
}
