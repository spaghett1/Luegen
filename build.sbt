// --- 1. VAL Definitionen (MÜSSEN am Anfang der Datei stehen) ---
val scalaFxVersion = "24.0.2-R36"
val JavaFxVersion = "24.0.2"
val javafxModules = Seq("base", "controls", "fxml", "graphics", "media", "web")
val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown OS name!")
}
// -------------------------------------------------------------------

lazy val root = (project in file("."))
  .settings(
    name := "de/htwg/luegen",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.7.3",

    // KORREKTUR: Erzwingt den Typ des Arguments für libraryDependencies
    libraryDependencies ++= {
      val fixedDependencies = Seq(
        "org.scalatest" %% "scalatest" % "3.2.19" % "test",
        "org.scalatestplus" %% "mockito-5-18" % "3.2.19.0" % Test,
        "org.scalafx" %% "scalafx" % scalaFxVersion
      )

      val platformDependencies = javafxModules.map(m =>
        "org.openjfx" % s"javafx-$m" % "21.0.1" classifier osName
      )

      // Rückgabe der verketteten Sequenz, die durch den äußeren Typ zugewiesen wird.
      // Der Block {} erlaubt es, temporäre Vals zu definieren, die den Typ bestimmen.
      (fixedDependencies ++ platformDependencies): Seq[sbt.librarymanagement.ModuleID]
    },

    Test / parallelExecution := false,

    // Scoverage Settings
    coverageEnabled := true,
    coverageHighlighting := true
  )