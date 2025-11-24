plugins {
    val kotlinVersion = "1.9.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("org.springframework.boot") version "3.4.5"
    application
}

group = "org.quicklybly.dumbmq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    implementation("javax.jms:jms-api:2.0.1")
//    implementation("org.apache.activemq:activemq-broker:6.1.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "8.14.10"
        distributionType = Wrapper.DistributionType.BIN
    }
}

kotlin {
    jvmToolchain(21)
}
