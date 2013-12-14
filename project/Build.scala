import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName = "lobster"
  val appVersion = "1.0"

  val appDependencies = Seq(
    javaCore,
    javaJdbc,
    javaJpa,
    cache,
    filters,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.hibernate" % "hibernate-core" % "4.2.3.Final",
    "org.hibernate" % "hibernate-entitymanager" % "4.2.7.Final",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "joda-time" % "joda-time" % "2.3"
  )

  val main = play.Project(appName, appVersion, appDependencies)
}