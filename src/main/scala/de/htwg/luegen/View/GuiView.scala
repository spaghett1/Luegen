package de.htwg.luegen.View

import de.htwg.luegen.Controller.{GameController, Observer}
import scalafx.application.Platform
import scalafx.scene.control.{Label, Button} // NEU: Button importiert
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import scalafx.scene.text.Font
import scalafx.event.ActionEvent
import scalafx.Includes.given

class GuiView(controller: GameController) extends Observer {
  
  val statusLabel = new Label("Initialisiere Spiel...") {
    font = Font("Arial", 16)
  }
  
  val initButton = new Button("Spiel starten (2 Spieler)") {
    onAction = (e: ActionEvent) => {
      controller.handlePlayerCount(2)
    }
  }
  
  val root = new VBox {
    padding = Insets(10)
    spacing = 10
    children = Seq(
      new Label("--- Lügen GUI ---") { font = Font("Arial", 20) },
      initButton, // Button hinzugefügt
      statusLabel
    )
  }
  
  override def updateDisplay(): Unit = {
    val model = controller.model
    
    Platform.runLater {
      val player = model.players.lift(model.currentPlayerIndex).map(_.name).getOrElse("N/A")
      val error = model.lastInputError.getOrElse("")

      statusLabel.text =
        s"Zustand: ${model.turnState}\n" +
          s"Spieler am Zug: $player\n" +
          s"Karten: ${model.discardedCards.size} im Pott\n" +
          s"Fehler: $error"
      
      if (model.turnState.toString == "NeedsPlayerCount") {
        initButton.setDisable(false)
      } else {
        initButton.setDisable(true)
      }
    }
  }
}

//test