/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java;

import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.openrewrite.Parser;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.J;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.java.tree.Space;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class ReloadableJava8Parser implements JavaParser {
    @Nullable
    private final Collection<Path> classpath;

    /**
     * When true, enables a parser to use class types from the in-memory type cache rather than performing
     * a deep equality check. Useful when deep class types have already been built from a separate parsing phase
     * and we want to parse some code snippet without requiring the classpath to be fully specified, using type
     * information we've already learned about in a prior phase.
     */
    private final boolean relaxedClassTypeMatching;

    private final boolean suppressMappingErrors;

    private final JavacFileManager pfm;

    private final Context context = new Context();
    private final Collection<NamedStyles> styles;
    private final JavaCompiler compiler;
    private final ResettableLog compilerLog = new ResettableLog(context);
    @Nullable
    private final LoggingHandler loggingHandler;

    ReloadableJava8Parser(@Nullable Collection<Path> classpath,
                          Charset charset,
                          boolean relaxedClassTypeMatching,
                          boolean suppressMappingErrors,
                          boolean logCompilationWarningsAndErrors,
                          Collection<NamedStyles> styles,
                          @Nullable LoggingHandler loggingHandler) {
        this.classpath = classpath;
        this.styles = styles;
        this.loggingHandler = loggingHandler;
        this.relaxedClassTypeMatching = relaxedClassTypeMatching;
        this.suppressMappingErrors = suppressMappingErrors;
        this.pfm = new JavacFileManager(context, true, charset) {
            @Override
            public boolean isSameFile(FileObject fileObject, FileObject fileObject1) {
                return fileObject.equals(fileObject1);
            }
        };

        // otherwise, consecutive string literals in binary expressions are concatenated by the parser, losing the original
        // structure of the expression!
        Options.instance(context).put("allowStringFolding", "false");

        // MUST be created (registered with the context) after pfm and compilerLog
        compiler = new JavaCompiler(context);

        // otherwise the JavacParser will use EmptyEndPosTable, effectively setting -1 as the end position
        // for every tree element
        compiler.genEndPos = true;
        compiler.keepComments = true;

        compilerLog.setWriters(new PrintWriter(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                String log = new String(Arrays.copyOfRange(cbuf, off, len));
                if (logCompilationWarningsAndErrors && !StringUtils.isBlank(log) && loggingHandler != null) {
                    loggingHandler.onWarn(log);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        }));
    }

    @Override
    public List<J.CompilationUnit> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo) {
        if (classpath != null) { // override classpath
            if (context.get(JavaFileManager.class) != pfm) {
                throw new IllegalStateException("JavaFileManager has been forked unexpectedly");
            }

            try {
                pfm.setLocation(StandardLocation.CLASS_PATH, classpath.stream().map(Path::toFile).collect(toList()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        Map<Parser.Input, JCTree.JCCompilationUnit> cus = acceptedInputs(sourceFiles).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        input -> Timer.builder("rewrite.parse")
                                .description("The time spent by the JDK in parsing and tokenizing the source file")
                                .tag("file.type", "Java")
                                .tag("step", "JDK parsing")
                                .register(Metrics.globalRegistry)
                                .record(() -> {
                                    try {
                                        return compiler.parse(new Java8ParserInputFileObject(input));
                                    } catch (IllegalStateException e) {
                                        if (e.getMessage().equals("endPosTable already set")) {
                                            throw new IllegalStateException("Call reset() on JavaParser before parsing another" +
                                                    "set of source files that have some of the same fully qualified names", e);
                                        }
                                        throw e;
                                    }
                                }),
                        (e2, e1) -> e1, LinkedHashMap::new));

        try {
            enterAll(cus.values());
            compiler.attribute(new TimedTodo(compiler.todo));
        } catch (Throwable t) {
            // when symbol entering fails on problems like missing types, attribution can often times proceed
            // unhindered, but it sometimes cannot (so attribution is always a BEST EFFORT in the presence of errors)
            if (loggingHandler != null) {
                loggingHandler.onWarn("Failed symbol entering or attribution", t);
            }
        }

        return cus.entrySet().stream()
                .map(cuByPath -> {
                    Timer.Sample sample = Timer.start();
                    Input input = cuByPath.getKey();

                    try {
                        ReloadableJava8ParserVisitor parser = new ReloadableJava8ParserVisitor(
                                input.getRelativePath(relativeTo),
                                StringUtils.readFully(input.getSource()),
                                relaxedClassTypeMatching,
                                styles,
                                new HashMap<>(),
                                loggingHandler);
                        J.CompilationUnit cu = (J.CompilationUnit) parser.scan(cuByPath.getValue(), Space.EMPTY);
                        sample.stop(Timer.builder("rewrite.parse")
                                .description("The time spent mapping the OpenJDK AST to Rewrite's AST")
                                .tag("file.type", "Java")
                                .tag("outcome", "success")
                                .tag("exception", "none")
                                .tag("step", "Map to Rewrite AST")
                                .register(Metrics.globalRegistry));
                        return cu;
                    } catch (Throwable t) {
                        sample.stop(Timer.builder("rewrite.parse")
                                .description("The time spent mapping the OpenJDK AST to Rewrite's AST")
                                .tag("file.type", "Java")
                                .tag("outcome", "error")
                                .tag("exception", t.getClass().getSimpleName())
                                .tag("step", "Map to Rewrite AST")
                                .register(Metrics.globalRegistry));

                        if (!suppressMappingErrors) {
                            throw t;
                        }

                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public ReloadableJava8Parser reset() {
        compilerLog.reset();
        pfm.flush();
        Check.instance(context).compiled.clear();
        return this;
    }

    /**
     * Enter symbol definitions into each compilation unit's scope
     */
    private void enterAll(Collection<JCTree.JCCompilationUnit> cus) {
        Enter enter = Enter.instance(context);
        com.sun.tools.javac.util.List<JCTree.JCCompilationUnit> compilationUnits = com.sun.tools.javac.util.List.from(
                cus.toArray(new JCTree.JCCompilationUnit[0]));
        enter.main(compilationUnits);
    }

    private static class ResettableLog extends Log {
        protected ResettableLog(Context context) {
            super(context);
        }

        public void reset() {
            sourceMap.clear();
        }
    }

    private static class TimedTodo extends Todo {
        private final Todo todo;
        private @Nullable Timer.Sample sample;

        private TimedTodo(Todo todo) {
            super(new Context());
            this.todo = todo;
        }

        @Override
        public boolean isEmpty() {
            if (sample != null) {
                sample.stop(Timer.builder("rewrite.parse")
                        .description("The time spent by the JDK in type attributing the source file")
                        .tag("file.type", "Java")
                        .tag("step", "Type attribution")
                        .register(Metrics.globalRegistry));
            }
            return todo.isEmpty();
        }

        @Override
        public Env<AttrContext> remove() {
            this.sample = Timer.start();
            return todo.remove();
        }
    }
}
