plugins {
    id("java")
    id("maven-publish")
}

group = "org.rabbani"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.squareup:javapoet:1.13.0")
}

tasks.test {
    useJUnitPlatform()
}