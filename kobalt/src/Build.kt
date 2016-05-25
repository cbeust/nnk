
import com.beust.kobalt.plugin.application.application
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.project
import com.beust.kobalt.repos

val repos = repos()


val p = project {

    name = "nnk"
    group = "com.beust"
    artifactId = name
    version = "0.1"

    dependenciesTest {
        compile("org.testng:testng:6.9.9")
    }

    assemble {
        jar {
        }
    }

    application {
        mainClass = "com.beust.nnk.MainKt"
    }
}
