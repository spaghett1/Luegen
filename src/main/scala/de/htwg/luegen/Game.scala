package luegen

import de.htwg.luegen.Model.GameModel
import de.htwg.luegen.View.{GameView, GuiView}
import de.htwg.luegen.Controller.GameController
import scalafx.application.JFXApp3 // Wichtig für den GUI-Start

// Der Hauptstartpunkt muss JFXApp3 erweitern
object Game extends JFXApp3 {

  override def start(): Unit = {
    println("Spiel wird initialisiert...")

    val model = new GameModel()
    val controller = new GameController(model)

    // --- 1. GUI Setup (JFX Thread) ---
    val guiView = new GuiView(controller)
    // Die Stage der JFXApp3 wird konfiguriert
    stage = new JFXApp3.PrimaryStage {
      title = "Lügen GUI (ScalaFX)"
      scene = new scalafx.scene.Scene(800, 600) {
        root = guiView.root // Zugriff auf das VBox-Root-Element
      }
    }
    controller.registerObserver(guiView) // GUI Observer registrieren

    // --- 2. TUI Setup (Hintergrund-Thread) ---
    val tuiView = new GameView(controller)
    controller.registerObserver(tuiView) // TUI Observer (Monitor) registrieren

    // Anonyme Thread-Klasse (keine neue Datei/Klasse) für den TUI-Input
    // Startet die blockierende TUI-Logik auf einem separaten Thread
    new Thread(new Runnable {
      override def run(): Unit = {
        tuiView.runTuiLoop()
      }
    }).start()

    // Initialisierung des Spiels (löst das erste Update aus)
    controller.initGame()
  }
}

//test