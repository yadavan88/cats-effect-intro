ThisBuild / organization := "com.yadavan88"
ThisBuild / scalaVersion := "3.1.2"

lazy val root = (project in file(".")).settings(
  name := "cats-effect3-intro",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.11"
  )
)
