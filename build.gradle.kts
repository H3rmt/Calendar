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
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.0")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.6.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
	
	implementation("no.tornado:tornadofx:1.7.20")
	
	implementation("com.google.code.gson:gson:2.8.9")
	
	implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
	implementation("com.twelvemonkeys.imageio:imageio-batik:3.7.0")
	
	//testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
	//testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

javafx {
	version = "16"
	modules("javafx.fxml", "javafx.web", "javafx.media")
}