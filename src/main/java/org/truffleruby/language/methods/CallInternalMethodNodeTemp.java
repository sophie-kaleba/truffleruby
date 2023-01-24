// CheckStyle: start generated
package org.truffleruby.language.methods;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.profiles.BranchProfile;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.Lock;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.LiteralCallNode;

@GeneratedBy(CallInternalMethodNode.class)
public final class CallInternalMethodNodeTemp extends CallInternalMethodNode {

    private static final Uncached UNCACHED = new Uncached();

    @CompilationFinal private volatile int state_0_;
    @CompilationFinal private volatile int exclude_;
    @Child private CallCachedData callCached_cache;
    @Child private IndirectCallNode callUncached_indirectCallNode_;
    @Child private AlwaysInlinedData alwaysInlined_cache;

    private CallInternalMethodNodeTemp() {
    }

    @ExplodeLoop
    @Override
    public Object execute(Frame frameValue, InternalMethod arg0Value, Object arg1Value, Object[] arg2Value, LiteralCallNode arg3Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) || callUncached(InternalMethod, Object, Object[], LiteralCallNode, IndirectCallNode) || alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) || alwaysInlinedUncached(Frame, InternalMethod, Object, Object[], LiteralCallNode) */) {
            if ((state_0 & 0b1) != 0 /* is-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */) {
                assert (isSingleContext());
                CallCachedData s0_ = this.callCached_cache;
                while (s0_ != null) {
                    if (!Assumption.isValidAssumption(s0_.assumption0_)) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        removeCallCached_(s0_);
                        return executeAndSpecialize(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
                    }
                    if ((arg0Value.getCallTarget() == s0_.cachedCallTarget_)) {
                        assert (!(s0_.cachedMethod_.alwaysInlined()));
                        s0_.callNode_.updateUserArgs(RubyArguments.getRawArguments(arg2Value));
                        return callCached(arg0Value, arg1Value, arg2Value, arg3Value, s0_.cachedCallTarget_, s0_.cachedMethod_, s0_.callNode_);
                    }
                    s0_ = s0_.next_;
                }
            }
            if ((state_0 & 0b10) != 0 /* is-state_0 callUncached(InternalMethod, Object, Object[], LiteralCallNode, IndirectCallNode) */) {
                if ((!(arg0Value.alwaysInlined()))) {
                    return callUncached(arg0Value, arg1Value, arg2Value, arg3Value, this.callUncached_indirectCallNode_);
                }
            }
            if ((state_0 & 0b100) != 0 /* is-state_0 alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */) {
                assert (isSingleContext());
                AlwaysInlinedData s2_ = this.alwaysInlined_cache;
                while (s2_ != null) {
                    if (!Assumption.isValidAssumption(s2_.assumption0_)) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        removeAlwaysInlined_(s2_);
                        return executeAndSpecialize(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
                    }
                    if ((arg0Value.getCallTarget() == s2_.cachedCallTarget_)) {
                        assert (s2_.cachedMethod_.alwaysInlined());
                        return alwaysInlined(frameValue, arg0Value, arg1Value, arg2Value, arg3Value, s2_.cachedCallTarget_, s2_.cachedMethod_, s2_.alwaysInlinedNode_, s2_.cachedArity_, s2_.checkArityProfile_, s2_.exceptionProfile_);
                    }
                    s2_ = s2_.next_;
                }
            }
            if ((state_0 & 0b1000) != 0 /* is-state_0 alwaysInlinedUncached(Frame, InternalMethod, Object, Object[], LiteralCallNode) */) {
                if ((arg0Value.alwaysInlined())) {
                    return alwaysInlinedUncached(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
    }

    private Object executeAndSpecialize(Frame frameValue, InternalMethod arg0Value, Object arg1Value, Object[] arg2Value, LiteralCallNode arg3Value) {
        Lock lock = getLock();
        boolean hasLock = true;
        lock.lock();
        try {
            int state_0 = this.state_0_;
            int exclude = this.exclude_;
            int oldState_0 = state_0;
            int oldExclude = exclude;
            int oldCacheCount = countCaches();
            try {
                if (((exclude & 0b1)) == 0 /* is-not-exclude callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */ && (isSingleContext())) {
                    int count0_ = 0;
                    CallCachedData s0_ = this.callCached_cache;
                    if ((state_0 & 0b1) != 0 /* is-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */) {
                        while (s0_ != null) {
                            if ((arg0Value.getCallTarget() == s0_.cachedCallTarget_)) {
                                assert (!(s0_.cachedMethod_.alwaysInlined()));
                                if (Assumption.isValidAssumption(s0_.assumption0_)) {
                                    break;
                                }
                            }
                            s0_ = s0_.next_;
                            count0_++;
                        }
                    }
                    if (s0_ == null) {
                        {
                            RootCallTarget cachedCallTarget__ = (arg0Value.getCallTarget());
                            if ((arg0Value.getCallTarget() == cachedCallTarget__)) {
                                InternalMethod cachedMethod__ = (arg0Value);
                                if ((!(cachedMethod__.alwaysInlined()))) {
                                    Assumption assumption0 = (getMethodAssumption(cachedMethod__));
                                    if (Assumption.isValidAssumption(assumption0)) {
                                        if (count0_ < (getCacheLimit())) {
                                            s0_ = super.insert(new CallCachedData(callCached_cache));
                                            s0_.cachedCallTarget_ = cachedCallTarget__;
                                            s0_.cachedMethod_ = cachedMethod__;
                                            s0_.callNode_ = s0_.insertAccessor((createCall(cachedMethod__.getName(), cachedCallTarget__)));
                                            s0_.assumption0_ = assumption0;
                                            VarHandle.storeStoreFence();
                                            this.callCached_cache = s0_;
                                            this.state_0_ = state_0 = state_0 | 0b1 /* add-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (s0_ != null) {
                        lock.unlock();
                        hasLock = false;
                        return callCached(arg0Value, arg1Value, arg2Value, arg3Value, s0_.cachedCallTarget_, s0_.cachedMethod_, s0_.callNode_);
                    }
                }
                if ((!(arg0Value.alwaysInlined()))) {
                    this.callUncached_indirectCallNode_ = super.insert((IndirectCallNode.create()));
                    this.exclude_ = exclude = exclude | 0b1 /* add-exclude callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */;
                    this.callCached_cache = null;
                    state_0 = state_0 & 0xfffffffe /* remove-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */;
                    this.state_0_ = state_0 = state_0 | 0b10 /* add-state_0 callUncached(InternalMethod, Object, Object[], LiteralCallNode, IndirectCallNode) */;
                    lock.unlock();
                    hasLock = false;
                    return callUncached(arg0Value, arg1Value, arg2Value, arg3Value, this.callUncached_indirectCallNode_);
                }
                if (((exclude & 0b10)) == 0 /* is-not-exclude alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */ && (isSingleContext())) {
                    int count2_ = 0;
                    AlwaysInlinedData s2_ = this.alwaysInlined_cache;
                    if ((state_0 & 0b100) != 0 /* is-state_0 alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */) {
                        while (s2_ != null) {
                            if ((arg0Value.getCallTarget() == s2_.cachedCallTarget_)) {
                                assert (s2_.cachedMethod_.alwaysInlined());
                                if (Assumption.isValidAssumption(s2_.assumption0_)) {
                                    break;
                                }
                            }
                            s2_ = s2_.next_;
                            count2_++;
                        }
                    }
                    if (s2_ == null) {
                        {
                            RootCallTarget cachedCallTarget__1 = (arg0Value.getCallTarget());
                            if ((arg0Value.getCallTarget() == cachedCallTarget__1)) {
                                InternalMethod cachedMethod__1 = (arg0Value);
                                if ((cachedMethod__1.alwaysInlined())) {
                                    Assumption assumption0 = (getMethodAssumption(cachedMethod__1));
                                    if (Assumption.isValidAssumption(assumption0)) {
                                        if (count2_ < (getCacheLimit())) {
                                            s2_ = super.insert(new AlwaysInlinedData(alwaysInlined_cache));
                                            s2_.cachedCallTarget_ = cachedCallTarget__1;
                                            s2_.cachedMethod_ = cachedMethod__1;
                                            s2_.alwaysInlinedNode_ = s2_.insertAccessor((createAlwaysInlinedMethodNode(cachedMethod__1)));
                                            s2_.cachedArity_ = (cachedMethod__1.getSharedMethodInfo().getArity());
                                            s2_.checkArityProfile_ = (BranchProfile.create());
                                            s2_.exceptionProfile_ = (BranchProfile.create());
                                            s2_.assumption0_ = assumption0;
                                            VarHandle.storeStoreFence();
                                            this.alwaysInlined_cache = s2_;
                                            this.state_0_ = state_0 = state_0 | 0b100 /* add-state_0 alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (s2_ != null) {
                        lock.unlock();
                        hasLock = false;
                        return alwaysInlined(frameValue, arg0Value, arg1Value, arg2Value, arg3Value, s2_.cachedCallTarget_, s2_.cachedMethod_, s2_.alwaysInlinedNode_, s2_.cachedArity_, s2_.checkArityProfile_, s2_.exceptionProfile_);
                    }
                }
                if ((arg0Value.alwaysInlined())) {
                    this.exclude_ = exclude = exclude | 0b10 /* add-exclude alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */;
                    this.alwaysInlined_cache = null;
                    state_0 = state_0 & 0xfffffffb /* remove-state_0 alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */;
                    this.state_0_ = state_0 = state_0 | 0b1000 /* add-state_0 alwaysInlinedUncached(Frame, InternalMethod, Object, Object[], LiteralCallNode) */;
                    lock.unlock();
                    hasLock = false;
                    return alwaysInlinedUncached(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
                }
                throw new UnsupportedSpecializationException(this, new Node[] {null, null, null, null}, arg0Value, arg1Value, arg2Value, arg3Value);
            } finally {
                if (oldState_0 != 0 || oldExclude != 0) {
                    checkForPolymorphicSpecialize(oldState_0, oldExclude, oldCacheCount);
                }
            }
        } finally {
            if (hasLock) {
                lock.unlock();
            }
        }
    }

    private void checkForPolymorphicSpecialize(int oldState_0, int oldExclude, int oldCacheCount) {
        int newState_0 = this.state_0_;
        int newExclude = this.exclude_;
        if (((oldState_0 ^ newState_0) != 0) || (oldExclude ^ newExclude) != 0 || oldCacheCount < countCaches()) {
            this.reportPolymorphicSpecialize(true);
        }
    }

    private int countCaches() {
        int cacheCount = 0;
        CallCachedData s0_ = this.callCached_cache;
        while (s0_ != null) {
            cacheCount++;
            s0_= s0_.next_;
        }
        AlwaysInlinedData s2_ = this.alwaysInlined_cache;
        while (s2_ != null) {
            cacheCount++;
            s2_= s2_.next_;
        }
        return cacheCount;
    }

    @Override
    public NodeCost getCost() {
        int state_0 = this.state_0_;
        if (state_0 == 0) {
            return NodeCost.UNINITIALIZED;
        } else {
            if ((state_0 & (state_0 - 1)) == 0 /* is-single-state_0  */) {
                CallCachedData s0_ = this.callCached_cache;
                AlwaysInlinedData s2_ = this.alwaysInlined_cache;
                if ((s0_ == null || s0_.next_ == null) && (s2_ == null || s2_.next_ == null)) {
                    return NodeCost.MONOMORPHIC;
                }
            }
        }
        return NodeCost.POLYMORPHIC;
    }

    void removeCallCached_(Object s0_) {
        Lock lock = getLock();
        lock.lock();
        try {
            CallCachedData prev = null;
            CallCachedData cur = this.callCached_cache;
            while (cur != null) {
                if (cur == s0_) {
                    if (prev == null) {
                        this.callCached_cache = cur.next_;
                        this.adoptChildren();
                    } else {
                        prev.next_ = cur.next_;
                        prev.adoptChildren();
                    }
                    break;
                }
                prev = cur;
                cur = cur.next_;
            }
            if (this.callCached_cache == null) {
                this.state_0_ = this.state_0_ & 0xfffffffe /* remove-state_0 callCached(InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, DirectCallNode) */;
            }
        } finally {
            lock.unlock();
        }
    }

    void removeAlwaysInlined_(Object s2_) {
        Lock lock = getLock();
        lock.lock();
        try {
            AlwaysInlinedData prev = null;
            AlwaysInlinedData cur = this.alwaysInlined_cache;
            while (cur != null) {
                if (cur == s2_) {
                    if (prev == null) {
                        this.alwaysInlined_cache = cur.next_;
                        this.adoptChildren();
                    } else {
                        prev.next_ = cur.next_;
                        prev.adoptChildren();
                    }
                    break;
                }
                prev = cur;
                cur = cur.next_;
            }
            if (this.alwaysInlined_cache == null) {
                this.state_0_ = this.state_0_ & 0xfffffffb /* remove-state_0 alwaysInlined(Frame, InternalMethod, Object, Object[], LiteralCallNode, RootCallTarget, InternalMethod, AlwaysInlinedMethodNode, Arity, BranchProfile, BranchProfile) */;
            }
        } finally {
            lock.unlock();
        }
    }

    public static CallInternalMethodNode create() {
        return new CallInternalMethodNodeTemp();
    }

    public static CallInternalMethodNode getUncached() {
        return CallInternalMethodNodeTemp.UNCACHED;
    }

    @GeneratedBy(CallInternalMethodNode.class)
    private static final class CallCachedData extends Node {

        @Child CallCachedData next_;
        @CompilationFinal RootCallTarget cachedCallTarget_;
        @CompilationFinal InternalMethod cachedMethod_;
        @Child DirectCallNode callNode_;
        @CompilationFinal Assumption assumption0_;

        CallCachedData(CallCachedData next_) {
            this.next_ = next_;
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.NONE;
        }

        <T extends Node> T insertAccessor(T node) {
            return super.insert(node);
        }

    }
    @GeneratedBy(CallInternalMethodNode.class)
    private static final class AlwaysInlinedData extends Node {

        @Child AlwaysInlinedData next_;
        @CompilationFinal RootCallTarget cachedCallTarget_;
        @CompilationFinal InternalMethod cachedMethod_;
        @Child AlwaysInlinedMethodNode alwaysInlinedNode_;
        @CompilationFinal Arity cachedArity_;
        @CompilationFinal BranchProfile checkArityProfile_;
        @CompilationFinal BranchProfile exceptionProfile_;
        @CompilationFinal Assumption assumption0_;

        AlwaysInlinedData(AlwaysInlinedData next_) {
            this.next_ = next_;
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.NONE;
        }

        <T extends Node> T insertAccessor(T node) {
            return super.insert(node);
        }

    }
    @GeneratedBy(CallInternalMethodNode.class)
    @DenyReplace
    private static final class Uncached extends CallInternalMethodNode {

        @Override
        public Object execute(Frame frameValue, InternalMethod arg0Value, Object arg1Value, Object[] arg2Value, LiteralCallNode arg3Value) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            if ((!(arg0Value.alwaysInlined()))) {
                return callUncached(arg0Value, arg1Value, arg2Value, arg3Value, (IndirectCallNode.getUncached()));
            }
            if ((arg0Value.alwaysInlined())) {
                return alwaysInlinedUncached(frameValue, arg0Value, arg1Value, arg2Value, arg3Value);
            }
            throw new UnsupportedSpecializationException(this, new Node[] {null, null, null, null}, arg0Value, arg1Value, arg2Value, arg3Value);
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }

    }
}
