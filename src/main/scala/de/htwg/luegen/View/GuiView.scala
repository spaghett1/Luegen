package de.htwg.luegen.View

import scalafx.application.JFXApp3
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import de.htwg.luegen.Controller.{IGameController, Observer}
import de.htwg.luegen.TurnState

class GuiView(controller: IGameController) extends JFXApp3 with Observer {
  
  private var statusLabel: Label = _
  private var playerLabel: Label = _

  override def start(): Unit = {
    controller.registerObserver(this)
    
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
  
  override def updateDisplay(): Unit = {
    Platform.runLater {
      try {
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