version = "4.0.4"

project.extra["PluginName"] = "PFighter AIO"
project.extra["PluginDescription"] = "Fully configurable all-in-one fighter - Premium version"

dependencies {
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.12")
    annotationProcessor(group = "org.pf4j", name = "pf4j", version = "3.2.0")
    compileOnly(group = "com.squareup.okhttp3", name = "okhttp", version = "3.7.0")
    compileOnly(group = "com.openosrs.externals", name = "paistisuite", version = "+")
}

tasks {
    register<proguard.gradle.ProGuardTask>("proguard") {
        configuration("${rootProject.projectDir}/config/proguard/proguard.txt")

        injars("${project.buildDir}/libs/${project.name}-${project.version}.jar")
        outjars("${project.buildDir}/libs/${project.name}-${project.version}-proguard.jar")

        target("11")

        adaptresourcefilenames()
        adaptresourcefilecontents()
        optimizationpasses(9)
        allowaccessmodification()
        mergeinterfacesaggressively()
        renamesourcefileattribute("SourceFile")
        keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

        libraryjars(System.getProperty("java.home") + "/jmods")
        libraryjars(configurations.compileClasspath.get())
    }

    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Dependencies" to nameToId("PaistiSuite"),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}