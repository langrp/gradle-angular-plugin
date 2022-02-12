
plugins {
	id("com.palawanframe.angular")
}

angular {
	group = "@sample"
	version = ""
	node {
		download = true
		workingDir = rootProject.file(".gradle/nodejs")
	}
}
