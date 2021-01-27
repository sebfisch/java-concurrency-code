package sebfisch.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.IntStream;

import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import sebfisch.actors.ObjectOrientedActorTests.Bouncer.Ping;
import sebfisch.actors.ObjectOrientedActorTests.Bouncer.Pong;
import sebfisch.actors.ObjectOrientedActorTests.Calculator.AddFraction;
import sebfisch.actors.ObjectOrientedActorTests.Calculator.GetTotal;
import sebfisch.actors.ObjectOrientedActorTests.Computer.Compute;
import sebfisch.actors.ObjectOrientedActorTests.Computer.Forward;
import sebfisch.actors.ObjectOrientedActorTests.Computer.Request;
import sebfisch.actors.ObjectOrientedActorTests.Handshake.Self;

public class ObjectOrientedActorTests {
    static final TestKitJunitResource KIT = new TestKitJunitResource();

    static class Terminator extends AbstractBehavior<Object> {
        static Behavior<Object> create() {
            return Behaviors.setup(Terminator::new);
        }

        Terminator(ActorContext<Object> ctx) {
            super(ctx);
        }

        @Override
        public Receive<Object> createReceive() {
            return newReceiveBuilder() //
                .onAnyMessage(msg -> Behaviors.stopped()) //
                .build();
        }
    }

    @Test
    public void testThatTerminatorStopsOnMessage() {
        ActorRef<Object> terminator = KIT.spawn(Terminator.create());
        terminator.tell("");
        KIT.createTestProbe().expectTerminated(terminator);
    }

    static class Bouncer extends AbstractBehavior<Ping> {
        static class Ping {
            final ActorRef<Pong> sender;

            Ping(ActorRef<Pong> sender) {
                this.sender = sender;
            }
        }

        static class Pong {
        }

        static Behavior<Ping> create() {
            return Behaviors.setup(Bouncer::new);
        }

        Bouncer(ActorContext<Ping> ctx) {
            super(ctx);
        }

        @Override
        public Receive<Ping> createReceive() {
            return newReceiveBuilder().onAnyMessage(ping -> {
                ping.sender.tell(new Pong());
                return this;
            }).build();
        }
    }

    @Test
    public void testThatBouncerBounces() {
        ActorRef<Ping> bouncer = KIT.spawn(Bouncer.create());
        TestProbe<Pong> probe = KIT.createTestProbe();
        IntStream.range(0, 5).forEach(i -> {
            bouncer.tell(new Ping(probe.getRef()));
            assertNotNull(probe.receiveMessage());
        });
    }

    static class Handshake extends AbstractBehavior<Self> {
        static class Self {
            final ActorRef<Self> sender;

            public Self(ActorRef<Self> sender) {
                this.sender = sender;
            }
        }

        static Behavior<Self> create() {
            return Behaviors.setup(Handshake::new);
        }

        Handshake(ActorContext<Self> ctx) {
            super(ctx);
        }

        @Override
        public Receive<Self> createReceive() {
            return newReceiveBuilder().onAnyMessage(msg -> {
                msg.sender.tell(new Self(getContext().getSelf()));
                return this;
            }).build();
        }
    }

    @Test
    public void testThatHandshakeIsAnswered() {
        ActorRef<Self> handshake = KIT.spawn(Handshake.create());
        TestProbe<Self> probe = KIT.createTestProbe();
        handshake.tell(new Self(probe.getRef()));
        assertNotNull(probe.receiveMessage());
    }

    static class Calculator extends AbstractBehavior<Calculator.Cmd> {
        interface Cmd {
        }

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
            return Behaviors.setup(ctx -> new Calculator(ctx, total));
        }

        private int total;

        Calculator(ActorContext<Cmd> ctx, int total) {
            super(ctx);
            this.total = total;
        }

        @Override
        public Receive<Cmd> createReceive() {
            return newReceiveBuilder().onMessage(GetTotal.class, msg -> {
                msg.sender.tell(total);
                return this;
            }).onMessage(AddFraction.class, msg -> {
                total += msg.numerator / msg.denominator;
                return this;
            }).build();
        }
    }

    @Test
    public void testThatCalculatorCalculates() {
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(Calculator.create(0));
        TestProbe<Integer> probe = KIT.createTestProbe();

        calculator.tell(new AddFraction(5, 2));
        calculator.tell(new AddFraction(1, 2));
        calculator.tell(new GetTotal(probe.getRef()));

        assertEquals(2, (int) probe.receiveMessage());
    }

    @Test
    public void testThatCalculatorStopsOnCrash() {
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(Calculator.create(0));
        calculator.tell(new AddFraction(1, 0));
        KIT.createTestProbe().expectTerminated(calculator);
    }

    @Test
    public void testThatResumingCalculatorResumesAfterCrash() {
        Behavior<Calculator.Cmd> resuming = //
                Behaviors.supervise(Calculator.create(0)) //
                        .onFailure(SupervisorStrategy.resume());
        ActorRef<Calculator.Cmd> calculator = KIT.spawn(resuming);
        TestProbe<Integer> probe = KIT.createTestProbe();

        calculator.tell(new AddFraction(5, 2));
        calculator.tell(new AddFraction(1, 0));
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

        calculator.tell(new AddFraction(5, 2));
        calculator.tell(new AddFraction(1, 0));
        calculator.tell(new GetTotal(probe.getRef()));

        assertEquals(0, (int) probe.receiveMessage());
    }

    static class Computer extends AbstractBehavior<Request> {
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
                return new Computer(ctx, calc);
            });
        }

        private final ActorRef<Calculator.Cmd> calc;

        Computer(ActorContext<Request> ctx, ActorRef<Calculator.Cmd> calc) {
            super(ctx);
            this.calc = calc;
        }

        @Override
        public Receive<Request> createReceive() {
            return newReceiveBuilder()
                .onMessage(Forward.class, msg -> {
                    calc.tell(msg.cmd);
                    return this;
                })
                .onMessage(Compute.class, msg -> {
                    calc.tell(new Calculator.AddFraction(100, msg.arg));
                    return this;
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
