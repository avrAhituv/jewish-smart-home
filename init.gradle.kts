// Temporary init script to help with SSL issues
allprojects {
    buildscript {
        repositories {
            google {
                setUrl("http://dl.google.com/dl/android/maven2/")
            }
            mavenCentral()
            gradlePluginPortal()
        }
    }
    
    repositories {
        google {
            setUrl("http://dl.google.com/dl/android/maven2/")
        }
        mavenCentral()
    }
}


