plugins {
    kotlin("jvm") version "1.9.20"
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
