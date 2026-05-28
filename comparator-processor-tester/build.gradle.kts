plugins {
    id("java")
    id("application")
}

group = "org.tester"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    annotationProcessor("org.rabbani:comparator-processor")
    implementation("org.rabbani:comparator-processor")
}

application{
    mainClass.set("org.tester.Main")
}

tasks.test {
    useJUnitPlatform()
}