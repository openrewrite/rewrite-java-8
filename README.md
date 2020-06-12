![Logo](https://github.com/openrewrite/rewrite/raw/master/doc/logo-oss.png)
### Eliminate Tech-Debt. Automatically.

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
JavaParser.Builder<? extends JavaParser, ?> javaParserBuilder;
if(System.getProperty("java.version").startsWith("1.8")) {
    javaParserBuilder = Java8Parser.builder();
}
else {
    javaParserBuilder = Java11Parser.builder();
}

JavaParser javaParser = javaParserBuilder.build();
```