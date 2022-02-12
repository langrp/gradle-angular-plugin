/*
 * Copyright (c) 2022 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("java")
	id("groovy")
	id("java-gradle-plugin")
	id("idea")
	id("com.gradle.plugin-publish") version "0.20.0"
	id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
	id("signing")
}

apply( from = "gradle/publishing.gradle.kts" )
apply( from = "gradle/functional-tests.gradle.kts" )

repositories {
    mavenCentral()
	gradlePluginPortal()
}


group = "com.palawanframe.build"
description = "Gradle plugin enabling build of angular application/components along side with gradle backend build."

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        register("angular") {
            id = "com.palawanframe.angular"
            displayName = "Angular Plugin"
            description = """
                Build your angular frontend application along with your gradle base backend application. Plugin manages
                versioning, publishing and dependency management of your angular application and/or components.
            """
            implementationClass = "com.palawan.gradle.AngularPlugin"
        }
        register("angularBase") {
            id = "com.palawanframe.angular-base"
            displayName = "Angular Base Plugin"
            description = """
                Creates new angular project from only gradle initialized project. Simply apply plugin into your gradle
                project and execute :angularInit task and new angular project will be created.
            """
            implementationClass = "com.palawan.gradle.AngularBasePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/langrp/${rootProject.name}"
    vcsUrl = "https://github.com/langrp/${rootProject.name}"
    description =project.description
    tags = listOf("angular", "node", "nodejs")

//    mavenCoordinates {
//        groupId = project.group as String
//        artifactId = project.name
//        version = project.version as String
//    }
}

dependencies {
	implementation(gradleApi())
	implementation(localGroovy())

    implementation("com.palawanframe.build:gradle-node-plugin:0.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")

	testImplementation(gradleTestKit())
	testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
	testImplementation("org.spockframework:spock-core")
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
		showCauses = true
		showStandardStreams = false
	}
}
