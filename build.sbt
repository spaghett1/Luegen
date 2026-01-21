lazy val root = (project in file("."))
  .settings(
    name := "de-htwg-luegen",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := "3.3.3",

    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "20.0.0-R31",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
      "com.typesafe.play" %% "play-json" % "2.10.3",
    ),

    Test / parallelExecution := false,
    coverageEnabled := true,
    coverageHighlighting := true,
    coverageExcludedFiles := ".*GuiView"
  )