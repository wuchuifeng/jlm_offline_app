pluginManagement {
    repositories {
        maven {
            url = uri("https://packages.aliyun.com/maven/repository/2418478-release-GhmPUt/")
            credentials {
                username = "609399173a10edbf367f5264"
                password = "=RTs0bvMruGT"
            }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://packages.aliyun.com/maven/repository/2418478-release-GhmPUt/") {
            credentials {
                username = "609399173a10edbf367f5264"
                password = "=RTs0bvMruGT"
            }
        }
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "极力米离线讲解器"
include(":app")
include(":base")
include(":common")
