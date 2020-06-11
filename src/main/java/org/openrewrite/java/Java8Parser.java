package org.openrewrite.java;

import io.micrometer.core.instrument.MeterRegistry;
import org.openrewrite.java.tree.J;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Java8Parser implements JavaParser {
    private final JavaParser delegate;

    Java8Parser(JavaParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<J.CompilationUnit> parse(List<Path> list, Path path) {
        return delegate.parse(list, path);
    }

    @Override
    public JavaParser reset() {
        return delegate.reset();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends JavaParser.Builder<Java8Parser, Builder> {
        private static ClassLoader toolsClassLoader;
        private static ClassLoader toolsAwareClassLoader;

        static synchronized void lazyInitClassLoaders() {
            if(toolsClassLoader != null && toolsAwareClassLoader != null) {
                return;
            }

            try {
                File tools = Paths.get(System.getProperty("java.home")).resolve("../lib/tools.jar").toFile();
                if (!tools.exists()) {
                    throw new IllegalStateException("To use Java8Parser, you must run the process with a JDK and not a JRE.");
                }

                toolsClassLoader = new URLClassLoader(new URL[]{tools.toURI().toURL()}, Java8Parser.class.getClassLoader());
                URLClassLoader appClassLoader = (URLClassLoader) Java8Parser.class.getClassLoader();

                toolsAwareClassLoader = new URLClassLoader(appClassLoader.getURLs(), toolsClassLoader) {
                    @Override
                    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                        if (!name.contains("ReloadableJava8Parser") &&
                                !name.startsWith("com.sun.tools") &&
                                !name.startsWith("com.sun.source")) {
                            return toolsClassLoader.loadClass(name);
                        }

                        Class<?> loadedClass = findLoadedClass(name);

                        if (loadedClass == null) {
                            try {
                                loadedClass = findClass(name);
                            } catch (ClassNotFoundException e) {
                                loadedClass = super.loadClass(name, resolve);
                            }
                        }

                        if (resolve) {
                            resolveClass(loadedClass);
                        }

                        return loadedClass;
                    }
                };
            } catch (MalformedURLException e) {
                throw new IllegalStateException("To use Java8Parser, you must run the process with a JDK and not a JRE.", e);
            }
        }

        @Override
        Java8Parser build() {
            lazyInitClassLoaders();

            try {
                // need to reverse this parent/child relationship
                Class<?> reloadableParser = Class.forName("org.openrewrite.java.ReloadableJava8Parser", true,
                        toolsAwareClassLoader);

                Constructor<?> delegateParserConstructor = reloadableParser
                        .getDeclaredConstructor(List.class, Charset.class, Boolean.TYPE, MeterRegistry.class, Boolean.TYPE);

                delegateParserConstructor.setAccessible(true);

                JavaParser delegate = (JavaParser) delegateParserConstructor
                        .newInstance(classpath, charset, relaxedClassTypeMatching, meterRegistry, logCompilationWarningsAndErrors);

                return new Java8Parser(delegate);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to construct Java8Parser.", e);
            }
        }
    }
}
