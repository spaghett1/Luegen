// 1. Logik zur Bestimmung des Betriebssystems (muss AUSSERHALB von .settings stehen)
val osName = System.getProperty("os.name").toLowerCase
val javafxClassifier = osName match {
  case n if n.contains("win") => "win"
  case n if n.contains("mac") =>
    if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
  case n if n.contains("nux") => "linux"
  case _ => "Fehler" // Fallback
}

lazy val root = (project in file("."))
  .settings(
    name := "de-htwg-luegen",
    version := "0.1.0-SNAPSHOT",

    // Empfehlung: Nutze 3.3.3 (LTS), da 3.7.x noch experimentell ist
    scalaVersion := "3.3.3",

    libraryDependencies ++= Seq(
      // Nutze zusammenpassende Versionen für Scala 3
      "org.scalafx" %% "scalafx" % "20.0.0-R31",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
      "org.scalamock" %% "scalamock" % "6.0.0" % Test,
      "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
      "com.typesafe.play" %% "play-json" % "2.10.3",
    ),

    // JavaFX Module mit dem korrekten Classifier für Apple Silicon (M-Chips)
    libraryDependencies ++= Seq("base", "controls", "fxml", "graphics", "media", "swing", "web").map {
      m => "org.openjfx" % s"javafx-$m" % "20" classifier javafxClassifier
    },

    Test / parallelExecution := false,
    coverageEnabled := true,
    coverageHighlighting := true,
    coverageExcludedFiles := ".*src/main/scala/de/htwg/luegen/view/GuiView.scala"
  )