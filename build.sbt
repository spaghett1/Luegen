lazy val root = (project in file("."))
  .settings(
    name := "de/htwg/luegen",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.7.3",

    libraryDependencies ++= Seq(
      // ... Ihre anderen Produktions-Abhängigkeiten ...

      // 1. ScalaTest (für die WordSpec-Struktur)
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",

      // 2. Mockito (Integration für ScalaTest)
      // Das % Test stellt sicher, dass diese Abhängigkeiten NUR für Tests verwendet werden.
      "org.scalatestplus" %% "mockito-5-18" % "3.2.19.0" % Test
    ),

    // Scoverage Settings
    coverageEnabled := true,
    coverageHighlighting := true
  )

