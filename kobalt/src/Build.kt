
import com.beust.kobalt.plugin.application.application
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.project

val p = project {

    name = "nnk"
    group = "com.beust"
    artifactId = name
    version = "0.1"

    dependencies {
        compile("com.beust:jcommander:1.48")
    }

    dependenciesTest {
        compile("org.testng:testng:6.9.9")
    }

    assemble {
        jar {
            fatJar = true
            manifest {
                attributes("Main-Class", "com.beust.nnk.MainKt")
            }
        }
    }

    application {
        mainClass = "com.beust.nnk.MainKt"
    }
}
