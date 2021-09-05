plugins {
    java
    `maven-publish`
}

group = "dk.brics.automaton"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {}

publishing {
    publications {
        create<MavenPublication>("brics-automaton") {
            artifact(tasks.jar)
        }
    }
}