package org.openrewrite.java;

public class Main {
    public static void main(String[] args) {
//        File tools = Paths.get(System.getProperty("java.home")).resolve("../lib/tools.jar").toFile();
//
//        System.out.println(tools.getAbsolutePath());
//        System.out.println(tools.exists());

        Java8Parser parser = Java8Parser.builder().build();

        parser.parse("public class A {}");
    }
}
