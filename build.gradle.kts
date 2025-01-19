plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

application.mainClass = "org.example.discord.Bot"
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:${rootProject.property("jda_version")}")

    implementation("mysql:mysql-connector-java:${rootProject.property("jdbc_version")}")

    implementation("io.github.cdimascio:java-dotenv:5.2.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}