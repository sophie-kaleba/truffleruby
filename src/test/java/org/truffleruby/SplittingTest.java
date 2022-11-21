/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.junit.Test;
import org.truffleruby.annotations.Split;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyMethodRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.TruffleBootNodesFactory;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.loader.MainLoader;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.parser.RubySource;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;

public abstract class SplittingTest {

    private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

    private final CoreExceptions coreExceptions() {
        return RubyLanguage.getCurrentContext().getCoreExceptions();
    }

    private final RubyString createString(TruffleString tstring, RubyEncoding encoding) {
        final RubyString instance = new RubyString(
                RubyLanguage.getCurrentContext().getCoreLibrary().stringClass,
                RubyLanguage.getCurrentLanguage().stringShape,
                false,
                tstring,
                encoding);
        AllocationTracing.trace(instance, null);
        return instance;
    }

    private final RubyString createString(TruffleString.FromJavaStringNode fromJavaStringNode, String javaString,
                                            RubyEncoding encoding) {
        TruffleString tstring = this.fromJavaStringNode.execute(javaString, encoding.tencoding);
        return createString(tstring, encoding);
    }

    private RubySource loadMainSourceSettingDollarZero(String kind, String toExecute) {
        final RubySource rubySource;
        final Object dollarZeroValue;
        final MainLoader mainLoader = new MainLoader(RubyLanguage.getCurrentContext(), RubyLanguage.getCurrentLanguage());
        Node currentNode = TruffleBootNodesFactory.MainNodeFactory.create(new RubyNode[1]);
        try {
            switch (kind) {
                case "FILE":
                    rubySource = mainLoader.loadFromFile(RubyLanguage.getCurrentContext().getEnv(), currentNode, toExecute);
                    dollarZeroValue = utf8(toExecute);
                    break;

                case "STDIN":
                    rubySource = mainLoader.loadFromStandardIn(currentNode, "-");
                    dollarZeroValue = utf8("-");
                    break;

                case "INLINE":
                    rubySource = mainLoader.loadFromCommandLineArgument(toExecute);
                    dollarZeroValue = utf8("-e");
                    break;

                default:
                    throw CompilerDirectives.shouldNotReachHere(kind);
            }
        } catch (IOException e) {
            throw new RaiseException(RubyLanguage.getCurrentContext(), coreExceptions().ioError(e, null));
        }
        assert RubyLanguage.MIME_TYPE_MAIN_SCRIPT.equals(rubySource.getSource().getMimeType());

        int index = RubyLanguage.getCurrentLanguage().getGlobalVariableIndex("$0");
        RubyLanguage.getCurrentContext().getGlobalVariableStorage(index).setValueInternal(dollarZeroValue);

        return rubySource;
    }

    private RubyString utf8(String string) {
        return createString(fromJavaStringNode, string, Encodings.UTF_8);
    }

    public SourceSection createSourceSection() throws IOException {
        StringReader reader = new StringReader("test\ncode");
        Source source1 = Source.newBuilder("Lang", reader, "testName").build();
        return source1.createSection(1);
    }

    public RubyMethodRootNode createRubyRootNode(RubyNode body, String backtraceName, String parseName) throws IOException {
        return new RubyMethodRootNode(
                RubyLanguage.getCurrentContext().getLanguageSlow(),
                createSourceSection(),
                new FrameDescriptor(),
                createSharedMethodInfo(backtraceName, parseName),
                body.cloneUninitialized(),
                Split.HEURISTIC,
                ReturnID.MODULE_BODY,
                Arity.ANY_ARGUMENTS);
    }

    public SharedMethodInfo createSharedMethodInfo(String backtraceName, String parseName) throws IOException {
        return new SharedMethodInfo(
                createSourceSection(),
                LexicalScope.IGNORE,
                Arity.NO_ARGUMENTS,
                backtraceName,
                0,
                parseName,
                null,
                null);
    }

    @Test
    public void testCloneRootNodeWithoutCalls() {
       final RubySource source = loadMainSourceSettingDollarZero("FILE", "SimpleShout.rb".intern());
        System.out.println("##########################################");
       assertTrue(false);
        // create a RootNode not containing any call-related nodes
        // link it to a call target
        // and link it to a call node
        // clone uninitialize the call node
        // check that the 2 call nodes (original and cloned) have the same contents
    }

    @Test
    public void testCloneRootNodeWithoutCallsReinit() throws IOException {
        // create a RootNode not containing any call-related nodes

        // create a body here
    //    RubyMethodRootNode rootNode =  createRubyRootNode(body, "testBack", "testParse");
        // link it to a call target
        // and link it to a call node
        // keep a copy of the vanilla/uninit version
        // specialize a couple of nodes
        // clone uninitialize the call node
        // check that the 2 call nodes (vanilla and cloned) have the same contents
    }

    @Test
    public void testCloneRootNodeCallsKeepCache() {
        // create a RootNode not containing one call node
        // link it to a call target
        // and link it to a call node (callNode1)
        // keep a copy of the vanilla/uninit version (callNode0)
        // make sure that the cache gets populated (call it once or twice) and keep a copy of it
        // clone uninitialize the call node (callNode3)
        // check that callNode0 (uninit), 1 (specialized), and 2 (uninit with persisted caches) are different
        // check in callNode2 that the cache was indeed persisted (compare this cache and cache0)
    }

    @Test
    public void testCloneRootNodeCallsKeepCacheDepth1() {
        // create a RootNode not containing one call node but deeper in the rootNode (eg within a loopNode)
        // link it to a call target
        // and link it to a call node (callNode1)
        // keep a copy of the vanilla/uninit version (callNode0)
        // make sure that the cache gets populated (call it once or twice) and keep a copy of it
        // clone uninitialize the call node (callNode3)
        // check that callNode0 (uninit), 1 (specialized), and 2 (uninit with persisted caches) are different
        // check in callNode2 that the cache was indeed persisted (compare this cache and cache0)
    }

}
