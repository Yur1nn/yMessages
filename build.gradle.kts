plugins {
    java
}

group = "dev.onelimit.ymessages"
version = "1.0.0"

base {
    archivesName.set("ymessages")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    compileOnly("dev.onelimit.ycore:ycore-velocity:1.0.0")

    implementation("org.yaml:snakeyaml:2.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
