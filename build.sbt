ThisBuild / organization := "com.yadavan88"
ThisBuild / scalaVersion := "3.6.2"

//disable scala 3 indentation syntax , i.e braces are mandatory
scalacOptions ++= Seq(
  "-no-indent"
)

lazy val root = (project in file(".")).settings(
  name := "cats-effect3-intro",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.5.7",
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0" % Test
  )
)
