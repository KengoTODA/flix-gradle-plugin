plugins { id("com.gradle.enterprise") version "3.9" }

rootProject.name = "flix-gradle-plugin"

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}
