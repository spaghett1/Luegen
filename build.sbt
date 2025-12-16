lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFxModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
  .map(m => "org.openjfx" % s"javafx-$m" % "16" classifier osName)

lazy val root = (project in file("."))
  .settings(
    name := "de/htwg/luegen",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.7.3",
    
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalatestplus" %% "mockito-5-18" % "3.2.19.0" % Test,
      "org.scalafx" %% "scalafx" % "16.0.0-R24",
    ),
    libraryDependencies ++= javaFxModules,

    Test / parallelExecution := false,
    
    coverageEnabled := true,
    coverageHighlighting := true
  )

//test