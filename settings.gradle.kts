plugins { id("com.gradle.enterprise") version "3.11.2" }

rootProject.name = "flix-gradle-plugin"

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

include("modules:packager-shell")
