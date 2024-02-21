plugins {
    id("java")
    id("idea")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.distributor"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.21.5")
    implementation("io.grpc:grpc-protobuf:1.53.0")
    implementation("io.grpc:grpc-stub:1.53.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jctools:jctools-core:4.0.3")
    implementation("net.devh:grpc-client-spring-boot-starter:2.15.0.RELEASE")
    implementation(project(":grpc-lib"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat()
    }
}

//sourceSets {
//	main {
//		java {
//			srcDirs.add(file("build/generated/source/proto/main/java"))
//		}
//	}
//}
