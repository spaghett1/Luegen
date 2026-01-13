package de.htwg.luegen.view

import scalafx.application.JFXApp3
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import de.htwg.luegen.controller.{IGameController, Observer}
import de.htwg.luegen.TurnState

class GuiView(controller: IGameController) extends JFXApp3 with Observer {

  // Variablen für UI-Elemente (noch nicht initialisiert wegen Toolkit-Fehler)
  private var statusLabel: Label = _
  private var playerLabel: Label = _

  override def start(): Unit = {
    // Registrierung als Beobachter erfolgt erst hier im Toolkit-Thread
    controller.registerObserver(this)

    // Initialisierung der UI-Komponenten [cite: 885, 904]
    statusLabel = new Label("Warte auf Initialisierung...")
    playerLabel = new Label("")

    stage = new JFXApp3.PrimaryStage {
      title = "Lügen - ScalaFX GUI"
      scene = new Scene {
        root = new VBox {
          padding = Insets(20)
          spacing = 15
          children = Seq(
            new Label("Spielstatus:"),
            statusLabel,
            playerLabel,
            new Button("Undo") {
              // Interaktion über das Controller-Interface [cite: 920, 925]
              onAction = _ => controller.undo()
            },
            new Button("Redo") {
              onAction = _ => controller.redo()
            }
          )
        }
      }
    }
  }

  // Diese Methode wird vom Controller bei jeder Änderung getriggert [cite: 1170]
  override def updateDisplay(): Unit = {
    Platform.runLater {
      try {
        // Nur auslesen, nicht blockieren
        val state = controller.getTurnState
        val players = controller.getCurrentPlayers

        if (statusLabel != null) {
          statusLabel.text = s"Status: $state"
        }
        if (playerLabel != null && players.nonEmpty) {
          playerLabel.text = s"Spieler bereit: ${players.size}"
        }
      } catch {
        case e: Exception => println(s"GUI Update Fehler: ${e.getMessage}")
      }
    }
  }
}