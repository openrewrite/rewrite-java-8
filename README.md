![Logo](https://github.com/openrewrite/rewrite/raw/master/doc/logo-oss.png)
### Semantic code search and transformation

![ci](https://github.com/openrewrite/rewrite-java-8/actions/workflows/ci.yml/badge.svg)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-java-8.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-java-8)

## What is this?

The Rewrite project is a mass refactoring ecosystem for Java and other source code, designed to eliminate technical debt across an engineering organization. It consists of a platform of prepackaged refactoring recipes for common framework migration and stylistic consistency tasks in Java, ready for you to apply in your build via Maven or Gradle plugins.

Read the full documentation at [docs.openrewrite.org](https://docs.openrewrite.org/).

Feel free to join us on [Slack](https://join.slack.com/t/rewriteoss/shared_invite/zt-kpz9t4hw-oWFbOMy~Kxta28qr2uqSFg)!

## How To Build

Since rewrite interacts with Java compiler internals, this project must be built with a Java 8 JDK.
On more recent JDKs you will run into errors; configuring a more recent JDK to target Java 8 language/bytecode level is not sufficient.

If you, very reasonably, don't want to change your `JAVA_HOME` to point at an old JDK, check out [jenv](https://www.jenv.be/).
On Mac, Linux, and Windows Subsystem for Linux, jenv allows you to configure JDK usage on a per-project basis.
