plugins {
    val kotlinVersion = "1.9.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.0"
    application
}

group = "org.quicklybly.dumbmq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-amqp")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    val stanfordNlpVersion = "4.5.4"
    implementation("edu.stanford.nlp:stanford-corenlp:$stanfordNlpVersion")
    implementation("edu.stanford.nlp:stanford-corenlp:$stanfordNlpVersion:models")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    test {
        jvmArgs("-Xmx8g")
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
