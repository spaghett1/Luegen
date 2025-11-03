lazy val root = (project in file("."))
  .settings(
      name := "luegen",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "3.7.3",

      libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % "3.2.19" % Test
      ),

      // Scoverage Settings
      coverageEnabled := true,
      coverageHighlighting := true
  )


