![Logo](https://github.com/openrewrite/rewrite/raw/master/doc/logo-oss.png)
### Semantic code search and transformation

[![Build Status](https://circleci.com/gh/openrewrite/rewrite-java-8.svg?style=shield)](https://circleci.com/gh/openrewrite/rewrite-java-8)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-java-8.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-java-8)

## What is this?

This project provides a Java 8 language parser for [Rewrite](https://github.com/openrewrite/rewrite).
A `JavaParser` can be built for Java 8 with:

```java
JavaParser javaParser = Java8Parser.builder()
    // additional options like classpath, etc.
    .build();
```

To conditionalize the use of the Java 8 or Java 11 parsers on the version of Java detectable at runtime:

```java
JavaParser.fromJavaVersion()
```

## How To Build
Since rewrite interacts with Java compiler internals, this project must be built with a Java 8 JDK.
On more recent JDKs you will run into errors; configuring a more recent JDK to target Java 8 language/bytecode level is not sufficient.

If you, very reasonably, don't want to change your `JAVA_HOME` to point at an old JDK, check out [jenv](https://www.jenv.be/).
On Mac, Linux, and Windows Subsystem for Linux, jenv allows you to configure JDK usage on a per-project basis.
