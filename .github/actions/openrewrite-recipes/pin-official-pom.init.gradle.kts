import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

// Pins every dependency to its version in the officially published POM, passed as -PofficialPom=<path>. Without this, the recipe builds resolve latest.integration snapshots instead of the versions the release used.

// Newer jars of these language modules are only downloadable with a Moderne subscription, so they stay on the last version on Maven Central, which build-openrewrite-recipes.sh puts into ~/.m2 first. Recipe modules only compile against them, as provided dependencies, so they never reach a consumer's classpath.
val subscriptionOnly = setOf("rewrite-javascript", "rewrite-csharp", "rewrite-scala", "rewrite-python", "rewrite-go")
val subscriptionOnlyVersion = "8.90.4"

allprojects {
    val officialPom = File(findProperty("officialPom") as String)
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(officialPom)
    val pins = mutableMapOf<String, String>()
    var rewriteBomVersion: String? = null

    val dependencies = document.getElementsByTagName("dependency")
    for (i in 0 until dependencies.length) {
        val dependency = dependencies.item(i) as Element
        fun text(tag: String) = dependency.getElementsByTagName(tag).item(0)?.textContent
        val groupId = text("groupId")
        val artifactId = text("artifactId")
        val version = text("version")!!
        if (groupId == "org.openrewrite" && artifactId == "rewrite-bom") {
            rewriteBomVersion = version
        }
        if (artifactId !in subscriptionOnly) {
            pins["$groupId:$artifactId"] = version
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.openrewrite" && requested.name in subscriptionOnly) {
                useVersion(subscriptionOnlyVersion)
                return@eachDependency
            }
            val pinned = pins["${requested.group}:${requested.name}"]
            if (pinned != null) {
                useVersion(pinned)
            } else if (requested.group == "org.openrewrite" && rewriteBomVersion != null) {
                useVersion(rewriteBomVersion!!)
            }
        }
    }
}
