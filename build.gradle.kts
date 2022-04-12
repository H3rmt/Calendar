plugins {
	java
	kotlin("jvm") version "1.6.0"
	id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}


dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.6.10")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
	
	implementation("org.controlsfx:controlsfx:11.1.1")
	implementation("no.tornado:tornadofx:1.7.20")
	
	implementation("com.google.code.gson:gson:2.9.0")
	
	implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
	implementation("com.twelvemonkeys.imageio:imageio-batik:3.8.2")
	
	implementation("org.jetbrains.exposed:exposed-core:0.37.3")
	implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
	implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
	implementation("org.xerial:sqlite-jdbc:3.36.0.3")
	implementation("org.slf4j:slf4j-api:1.7.36") // for logging for exposed
	implementation("org.slf4j:slf4j-simple:1.7.36") // for logging for exposed
}

javafx {
	version = "16"
	modules("javafx.web") // , "javafx.media"
}