plugins {
	java
    jacoco
	alias(libs.plugins.springBoot)
	alias(libs.plugins.springDependencyManagement)
	alias(libs.plugins.asciiDoctor)
}

group = "uk.co.emcreations"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification) // report is always generated after tests run
}
jacoco {
    toolVersion = "0.8.13"
    reportsDirectory = layout.buildDirectory.dir("reports")
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = true
        csv.required = false
    }
}
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["springCloudVersion"] = "2025.0.0"
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.cloud:spring-cloud-starter")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("org.springframework.session:spring-session-core")
	implementation(libs.springDoc)
	implementation(libs.auth0)
    implementation(libs.apache.commons.lang)
    implementation(libs.pdfbox)

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly(libs.postgresql)
	annotationProcessor("org.projectlombok:lombok")

	testImplementation(libs.mockito)
	mockitoAgent(libs.mockito) { isTransitive = false }
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs?.add("-javaagent:${mockitoAgent.asPath}")
}

tasks.test {
	outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
	inputs.dir(project.extra["snippetsDir"]!!)
	dependsOn(tasks.test)
}
