/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import java.util.concurrent.CountDownLatch;

import org.truffleruby.core.fiber.RubyFiber.FiberStatus;

import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.TruffleSafepoint;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.DummyNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.SafepointAction;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.language.objects.shared.SharedObjects;

/** Helps managing Ruby {@code Fiber} objects. Only one per {@link RubyContext}. */
public class FiberManager {

    public static final String NAME_PREFIX = "Ruby Fiber";
    public static final Object[] SAFEPOINT_ARGS = new Object[]{ FiberSafepointMessage.class };

    private final RubyLanguage language;
    private final RubyContext context;

    public FiberManager(RubyLanguage language, RubyContext context) {
        this.language = language;
        this.context = context;
    }

    public void initialize(RubyFiber fiber, boolean blocking, RubyProc block, Node currentNode) {
        final SourceSection sourceSection = block.getSharedMethodInfo().getSourceSection();
        fiber.sourceLocation = context.fileLine(sourceSection);
        fiber.blocking = blocking;

        final TruffleContext truffleContext = context.getEnv().getContext();

        context.getThreadManager().leaveAndEnter(truffleContext, currentNode, () -> {
            context.getThreadManager().spawnFiber(fiber, sourceSection,
                    () -> fiberMain(context, fiber, block, currentNode));
            waitForInitialization(context, fiber, currentNode);
            return BlockingAction.SUCCESS;
        });
    }

    /** Wait for full initialization of the new fiber */
    public static void waitForInitialization(RubyContext context, RubyFiber fiber, Node currentNode) {
        final CountDownLatch initializedLatch = fiber.initializedLatch;

        if (context.getEnv().getContext().isEntered()) {
            context.getThreadManager().runUntilResultKeepStatus(currentNode, CountDownLatch::await, initializedLatch);
        } else {
            context.getThreadManager().retryWhileInterrupted(currentNode, CountDownLatch::await, initializedLatch);
        }

        final Throwable uncaughtException = fiber.uncaughtException;
        if (uncaughtException != null) {
            ExceptionOperations.rethrow(uncaughtException);
        }
    }

    private static final BranchProfile UNPROFILED = BranchProfile.getUncached();

    private void fiberMain(RubyContext context, RubyFiber fiber, RubyProc block, Node currentNode) {
        assert !fiber.isRootFiber() : "Root Fibers execute threadMain() and not fiberMain()";
        assertNotEntered("Fibers should start unentered to avoid triggering multithreading");

        final Thread thread = Thread.currentThread();
        final TruffleContext truffleContext = context.getEnv().getContext();

        start(fiber, thread);

        // fully initialized
        fiber.initializedLatch.countDown();

        final FiberMessage message = waitMessage(fiber, currentNode);
        fiber.rubyThread.setCurrentFiber(fiber);

        // enter() polls so we need the current Fiber to be set before enter()
        final Object prev = truffleContext.enter(currentNode);

        FiberMessage lastMessage = null;
        try {
            var descriptorAndArgs = handleMessage(fiber, message, currentNode);
            fiber.status = FiberStatus.RESUMED;
            final Object result = ProcOperations.rootCall(block, descriptorAndArgs.descriptor, descriptorAndArgs.args);

            lastMessage = new FiberResumeMessage(FiberOperation.YIELD, fiber,
                    EmptyArgumentsDescriptor.INSTANCE, new Object[]{ result });

            // Handlers in the same order as in ThreadManager
        } catch (KillException | ExitException | RaiseException e) {
            // Propagate the exception until it reaches the root Fiber
            lastMessage = new FiberExceptionMessage(e);
        } catch (FiberShutdownException e) {
            // Ends execution of the Fiber
            lastMessage = null;
        } catch (BreakException e) {
            final RubyException exception = context.getCoreExceptions().breakFromProcClosure(currentNode);
            lastMessage = new FiberExceptionMessage(new RaiseException(context, exception));
        } catch (DynamicReturnException e) {
            final RubyException exception = context.getCoreExceptions().unexpectedReturn(currentNode);
            lastMessage = new FiberExceptionMessage(new RaiseException(context, exception));
        } catch (Throwable e) {
            final RuntimeException exception = ThreadManager.printInternalError(e);
            lastMessage = new FiberExceptionMessage(exception);
        } finally {
            final RubyFiber returnFiber = lastMessage == null ? null : getReturnFiber(fiber, currentNode, UNPROFILED);

            // Perform all cleanup before resuming the parent Fiber
            // Make sure that other fibers notice we are dead before they gain control back
            fiber.status = FiberStatus.TERMINATED;
            // Leave context before addToMessageQueue() -> parent Fiber starts executing
            truffleContext.leave(currentNode, prev);
            cleanup(fiber, thread);

            if (lastMessage != null) {
                addToMessageQueue(returnFiber, lastMessage);
            }
        }
    }

    public RubyFiber getReturnFiber(RubyFiber currentFiber, Node currentNode, BranchProfile errorProfile) {
        assert currentFiber.isActive();

        final RubyFiber rootFiber = currentFiber.rubyThread.getRootFiber();

        final RubyFiber previousFiber = currentFiber.lastResumedByFiber;
        if (previousFiber != null) {
            currentFiber.lastResumedByFiber = null;
            previousFiber.resumingFiber = null;
            return previousFiber;
        } else {

            if (currentFiber == rootFiber) {
                errorProfile.enter();
                throw new RaiseException(context, context.getCoreExceptions().yieldFromRootFiberError(currentNode));
            }

            RubyFiber fiber = rootFiber;
            while (fiber.resumingFiber != null) {
                fiber = fiber.resumingFiber;
            }
            return fiber;
        }


    }

    @TruffleBoundary
    private void addToMessageQueue(RubyFiber fiber, FiberMessage message) {
        assertNotEntered("should have left context when sending message to fiber");
        fiber.messageQueue.add(message);
    }

    /** Send the Java thread that represents this fiber to sleep until it receives a message. */
    @TruffleBoundary
    private FiberMessage waitMessage(RubyFiber fiber, Node currentNode) {
        assertNotEntered("should have left context while waiting fiber message");

        class State {
            final RubyFiber fiber;
            FiberMessage message;

            State(RubyFiber fiber) {
                this.fiber = fiber;
            }
        }

        final State state = new State(fiber);
        context.getThreadManager().retryWhileInterrupted(
                currentNode,
                s -> s.message = s.fiber.messageQueue.take(),
                state);
        return state.message;
    }

    private void assertNotEntered(String reason) {
        assert !context.getEnv().getContext().isEntered() : reason;
    }

    @TruffleBoundary
    private DescriptorAndArgs handleMessage(RubyFiber fiber, FiberMessage message, Node currentNode) {
        // Written as a loop to not grow the stack when processing guest safepoints
        while (message instanceof FiberSafepointMessage) {
            final FiberSafepointMessage safepointMessage = (FiberSafepointMessage) message;
            safepointMessage.action.run(fiber.rubyThread, currentNode);
            final RubyFiber sendingFiber = safepointMessage.sendingFiber;
            message = resumeAndWait(fiber, sendingFiber, FiberOperation.TRANSFER,
                    EmptyArgumentsDescriptor.INSTANCE, SAFEPOINT_ARGS, currentNode);
        }

        if (message instanceof FiberShutdownMessage) {
            throw new FiberShutdownException(currentNode);
        } else if (message instanceof FiberExceptionMessage) {
            throw ((FiberExceptionMessage) message).getException();
        } else if (message instanceof FiberResumeMessage) {
            final FiberResumeMessage resumeMessage = (FiberResumeMessage) message;
            assert language.getCurrentThread() == resumeMessage.getSendingFiber().rubyThread;
            final FiberOperation operation = resumeMessage.getOperation();

            if (operation == FiberOperation.RESUME) {
                fiber.yielding = false;
            }
            fiber.status = FiberStatus.RESUMED;


            if (operation == FiberOperation.RESUME || operation == FiberOperation.RAISE) {
                fiber.lastResumedByFiber = resumeMessage.getSendingFiber();
            }

            if (operation == FiberOperation.RAISE) {
                throw new RaiseException(context, (RubyException) resumeMessage.getArgs()[0]);
            }

            return resumeMessage.getDescriptorAndArgs();
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }
    }

    /** Send a resume message to a fiber by posting into its message queue. Doesn't explicitly notify the Java thread
     * (although the queue implementation may) and doesn't wait for the message to be received. */
    private void resume(RubyFiber fromFiber, RubyFiber fiber, FiberOperation operation,
            ArgumentsDescriptor descriptor, Object... args) {
        addToMessageQueue(fiber, new FiberResumeMessage(operation, fromFiber, descriptor, args));
    }

    @TruffleBoundary
    public DescriptorAndArgs transferControlTo(RubyFiber fromFiber, RubyFiber toFiber, FiberOperation operation,
            ArgumentsDescriptor descriptor, Object[] args, Node currentNode) {
        assert fromFiber.resumingFiber == null;
        if (operation == FiberOperation.RESUME) {
            fromFiber.resumingFiber = toFiber;
        }

        assert !fromFiber.yielding;
        if (operation == FiberOperation.YIELD) {
            fromFiber.yielding = true;
        }

        if (fromFiber.status == FiberStatus.RESUMED) {
            fromFiber.status = FiberStatus.SUSPENDED;
        }
        final FiberMessage message = resumeAndWait(fromFiber, toFiber, operation, descriptor, args, currentNode);
        return handleMessage(fromFiber, message, currentNode);
    }

    /** This methods switches from the currently-running fromFiber to toFiber. This method notifies toFiber to start
     * executing again and then just after suspends fromFiber. We only return from this method call for fromFiber once
     * control is passed back to fromFiber. As soon as toFiber is notified, it pops the message from the queue in
     * waitMessage() and then calls handleMessage(). There must be no code between notifying toFiber and suspending
     * fromFiber, as during that time both threads can be running, yet this does not matter semantically since fromFiber
     * will suspend and nothing happens fromFiber until then.
     *
     * @param fromFiber the current fiber which will soon be suspended
     * @param toFiber the fiber we resume or transfer to */
    @TruffleBoundary
    private FiberMessage resumeAndWait(RubyFiber fromFiber, RubyFiber toFiber, FiberOperation operation,
            ArgumentsDescriptor descriptor, Object[] args, Node currentNode) {
        final TruffleContext truffleContext = context.getEnv().getContext();
        final FiberMessage message = context
                .getThreadManager()
                .leaveAndEnter(truffleContext, currentNode, () -> {
                    resume(fromFiber, toFiber, operation, descriptor, args);
                    return waitMessage(fromFiber, currentNode);
                });
        fromFiber.rubyThread.setCurrentFiber(fromFiber);
        return message;
    }

    @TruffleBoundary
    public void safepoint(RubyFiber fromFiber, RubyFiber fiber, SafepointAction action, Node currentNode) {
        final TruffleContext truffleContext = context.getEnv().getContext();
        final FiberResumeMessage returnMessage = (FiberResumeMessage) context
                .getThreadManager()
                .leaveAndEnter(truffleContext, currentNode, () -> {
                    addToMessageQueue(fiber, new FiberSafepointMessage(fromFiber, action));
                    return waitMessage(fromFiber, currentNode);
                });
        fromFiber.rubyThread.setCurrentFiber(fromFiber);

        if (returnMessage.getArgs() != SAFEPOINT_ARGS) {
            throw CompilerDirectives.shouldNotReachHere();
        }
    }

    public void start(RubyFiber fiber, Thread javaThread) {
        fiber.thread = javaThread;

        final RubyThread rubyThread = fiber.rubyThread;

        // share RubyFiber as its fiberLocals might be accessed by other threads with Thread#[]
        SharedObjects.propagate(language, rubyThread, fiber);
        rubyThread.runningFibers.add(fiber);
    }

    public void cleanup(RubyFiber fiber, Thread javaThread) {
        context.getValueWrapperManager().cleanup(context, fiber.handleData);

        fiber.status = FiberStatus.TERMINATED;

        fiber.rubyThread.runningFibers.remove(fiber);

        fiber.thread = null;

        fiber.finishedLatch.countDown();
    }

    @TruffleBoundary
    public void killOtherFibers(RubyThread thread) {
        if (thread.runningFibers.size() <= 1) {
            return; // Avoid leaveAndEnter() if there are no other Fibers
        }

        // All Fibers except the current one are in waitForResume(),
        // so sending a FiberShutdownMessage is enough to finish them.
        // This also avoids the performance cost of a safepoint.

        // This method might not be executed on the rootFiber Java Thread but possibly on another Java Thread.

        // Disallow side-effecting safepoints, the current thread is cleaning up and terminating.
        // It can no longer process any exception or guest code.
        final TruffleSafepoint safepoint = TruffleSafepoint.getCurrent();
        boolean allowSideEffects = safepoint.setAllowSideEffects(false);
        try {
            final TruffleContext truffleContext = context.getEnv().getContext();
            context.getThreadManager().leaveAndEnter(truffleContext, DummyNode.INSTANCE, () -> {
                doKillOtherFibers(thread);
                return BlockingAction.SUCCESS;
            });
        } finally {
            safepoint.setAllowSideEffects(allowSideEffects);
        }
    }

    private void doKillOtherFibers(RubyThread thread) {
        for (RubyFiber fiber : thread.runningFibers) {
            if (!fiber.isRootFiber()) {
                addToMessageQueue(fiber, new FiberShutdownMessage());

                // Wait for the Fiber to finish so we only run one Fiber at a time
                final CountDownLatch finishedLatch = fiber.finishedLatch;
                context.getThreadManager().retryWhileInterrupted(
                        DummyNode.INSTANCE,
                        CountDownLatch::await,
                        finishedLatch);

                final Throwable uncaughtException = fiber.uncaughtException;
                if (uncaughtException != null) {
                    ExceptionOperations.rethrow(uncaughtException);
                }
            }
        }
    }

    public String getFiberDebugInfo(RubyThread rubyThread) {
        final StringBuilder builder = new StringBuilder();

        for (RubyFiber fiber : rubyThread.runningFibers) {
            builder.append("  fiber @");
            builder.append(ObjectIDNode.getUncached().execute(fiber));

            final Thread thread = fiber.thread;
            if (thread == null) {
                builder.append(" (no Java thread)");
            } else {
                builder.append(" #").append(RubyLanguage.getThreadId(thread)).append(' ').append(thread);
            }

            if (fiber.isRootFiber()) {
                builder.append(" (root)");
            }

            if (fiber.isActive()) {
                builder.append(" (current)");
            }

            builder.append("\n");
        }

        if (builder.length() == 0) {
            return "  no fibers\n";
        } else {
            return builder.toString();
        }
    }

    public static final class DescriptorAndArgs {
        public final ArgumentsDescriptor descriptor;
        public final Object[] args;

        public DescriptorAndArgs(ArgumentsDescriptor descriptor, Object[] args) {
            this.descriptor = descriptor;
            this.args = args;
        }
    }

    public interface FiberMessage {
    }

    private static class FiberResumeMessage implements FiberMessage {

        private final FiberOperation operation;
        private final RubyFiber sendingFiber;
        private final ArgumentsDescriptor descriptor;
        private final Object[] args;

        public FiberResumeMessage(
                FiberOperation operation,
                RubyFiber sendingFiber,
                ArgumentsDescriptor descriptor,
                Object[] args) {
            this.operation = operation;
            this.sendingFiber = sendingFiber;
            this.descriptor = descriptor;
            this.args = args;
        }

        public FiberOperation getOperation() {
            return operation;
        }

        public RubyFiber getSendingFiber() {
            return sendingFiber;
        }

        public ArgumentsDescriptor getDescriptor() {
            return descriptor;
        }

        public Object[] getArgs() {
            return args;
        }

        public DescriptorAndArgs getDescriptorAndArgs() {
            return new DescriptorAndArgs(descriptor, args);
        }
    }

    private static class FiberSafepointMessage implements FiberMessage {
        private final RubyFiber sendingFiber;
        private final SafepointAction action;

        private FiberSafepointMessage(RubyFiber sendingFiber, SafepointAction action) {
            this.sendingFiber = sendingFiber;
            this.action = action;
        }
    }

    /** Used to cleanup and terminate Fibers when the parent Thread dies. */
    @SuppressWarnings("serial")
    public static final class FiberShutdownException extends TerminationException {
        public FiberShutdownException(Node location) {
            super("terminate Fiber", location);
        }
    }

    private static class FiberShutdownMessage implements FiberMessage {
    }

    private static class FiberExceptionMessage implements FiberMessage {

        private final RuntimeException exception;

        public FiberExceptionMessage(RuntimeException exception) {
            this.exception = exception;
        }

        public RuntimeException getException() {
            return exception;
        }

    }

}
