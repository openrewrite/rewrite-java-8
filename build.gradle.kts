import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.info.InfoBrokerPlugin
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing

    id("nebula.maven-resolved-dependencies") version "17.3.2"
    id("nebula.release") version "15.3.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"

    id("com.github.hierynomus.license") version "0.15.0"
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("com.github.jk1.dependency-license-report") version "1.16"

    id("nebula.maven-publish") version "17.3.2"
    id("nebula.contacts") version "5.1.0"
    id("nebula.info") version "9.3.0"

    id("nebula.javadoc-jar") version "17.3.2"
    id("nebula.source-jar") version "17.3.2"
    id("nebula.maven-apache-license") version "17.3.2"
}

group = "org.openrewrite"
description = "Eliminate technical debt. Automatically (for Java 8)."

repositories {
    mavenLocal()
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME"))
            password.set(project.findProperty("ossrhToken") as String? ?: System.getenv("OSSRH_TOKEN"))
        }
    }
}

signing {
    setRequired({
        !project.hasProperty("skipSigning")
    })
    val signingKey = project.findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
    val signingPassword = project.findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["nebula"])
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

val rewriteVersion = "latest.release"
dependencies {
    compileOnly(files("${System.getProperty("java.home")}/../lib/tools.jar"))

    implementation("org.openrewrite:rewrite-java:$rewriteVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
    // This project needs to be able to co-exist with rewrite-java-11 also on the classpath
    // So run tests with that circumstance in mind
    testRuntimeOnly("org.openrewrite:rewrite-java-11:$rewriteVersion")

    testImplementation("org.openrewrite:rewrite-test:$rewriteVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.0.13")
}

tasks.withType(KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.named<JavaCompile>("compileJava") {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames")
}

configure<ContactsExtension> {
    val j = Contact("jkschneider@gmail.com")
    j.moniker("Jonathan Schneider")

    people["jkschneider@gmail.com"] = j
}

configure<LicenseExtension> {
    ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
    skipExistingHeaders = true
    header = project.rootProject.file("gradle/licenseHeader.txt")
    mapping(mapOf("kt" to "SLASHSTAR_STYLE", "java" to "SLASHSTAR_STYLE"))
    strictCheck = true
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")

            pom.withXml {
                (asElement().getElementsByTagName("dependencies").item(0) as org.w3c.dom.Element).let { dependencies ->
                    dependencies.getElementsByTagName("dependency").let { dependencyList ->
                        var i = 0
                        var length = dependencyList.length
                        while (i < length) {
                            (dependencyList.item(i) as org.w3c.dom.Element).let { dependency ->
                                if ((dependency.getElementsByTagName("scope")
                                                .item(0) as org.w3c.dom.Element).textContent == "provided") {
                                    dependencies.removeChild(dependency)
                                    i--
                                    length--
                                }
                            }
                            i++
                        }
                    }
                }
            }
        }
    }
}

tasks.withType<GenerateMavenPom> {
    doLast {
        // because pom.withXml adds blank lines
        destination.writeText(
                destination.readLines().filter { it.isNotBlank() }.joinToString("\n")
        )
    }

    doFirst {
        val runtimeClasspath = configurations.getByName("runtimeClasspath")

        val gav = { dep: ResolvedDependency ->
            "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}"
        }

        val observedDependencies = TreeSet<ResolvedDependency> { d1, d2 ->
            gav(d1).compareTo(gav(d2))
        }

        fun reduceDependenciesAtIndent(indent: Int):
                (List<String>, ResolvedDependency) -> List<String> =
                { dependenciesAsList: List<String>, dep: ResolvedDependency ->
                    dependenciesAsList + listOf(" ".repeat(indent) + dep.module.id.toString()) + (
                            if (observedDependencies.add(dep)) {
                                dep.children
                                        .sortedBy(gav)
                                        .fold(emptyList(), reduceDependenciesAtIndent(indent + 2))
                            } else {
                                // this dependency subtree has already been printed, so skip it
                                emptyList()
                            }
                            )
                }

        project.plugins.withType<InfoBrokerPlugin> {
            add("Resolved-Dependencies", runtimeClasspath
                    .resolvedConfiguration
                    .lenientConfiguration
                    .firstLevelModuleDependencies
                    .sortedBy(gav)
                    .fold(emptyList(), reduceDependenciesAtIndent(6))
                    .joinToString("\n", "\n", "\n" + " ".repeat(4)))
        }
    }
}
