
plugins {
	id("com.palawanframe.angular-base")
}

angular {
	group = "@sample"
	node {
		download = true
		workingDir = rootProject.file(".gradle/nodejs")
	}
}

