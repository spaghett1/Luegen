package de.htwg.luegen.View

import de.htwg.luegen.Controller.{GameController, Observer}
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState.*

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, VBox, HBox, GridPane}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Text
import scalafx.application.Platform

class GuiView(controller: GameController) extends Observer with JFXApp3 {

  // --- MEMBER DECLARATIONS ---
  // HINWEIS: ScalaFX deklariert 'stage' implizit, aber der Compiler braucht Klarheit.
  // Die Zuweisung erfolgt in start().
  private lazy val infoLabel = new Label ("Spielinformationen werden geladen...")
  private lazy val logText = new Text("")
  private lazy val centerPane = new VBox {
    alignment = Pos.Center
    padding = Insets(20)
    spacing = 10
  }

  private lazy val inputField = new TextField {
    promptText = "Ihre Eingabe..."
  }
  private lazy val submitButton = new Button("Bestätigen")


  // --- 1. ERFORDERLICHE IMPLEMENTIERUNG: JFXApp3.start() ---
  override def start(): Unit = {
    controller.registerObserver(this) // Beim Controller registrieren

    // Die korrigierte Stage-Definition (mit allen korrekten Klammern)
    stage = new PrimaryStage {
      title = "Lügen - Das Kartenspiel"
      scene = new Scene(700, 500) {
        root = new BorderPane {
          padding = Insets(10)
          top = new VBox(5) {
            children = Seq(infoLabel, logText)
          }
          center = centerPane
          bottom = new HBox(10) {
            padding = Insets(10, 0, 0, 0)
            children = Seq(inputField, submitButton)
          }
        } // Schließt BorderPane
      } // Schließt Scene
    } // Schließt PrimaryStage

    updateDisplay() // Initialer Aufruf
  } // Schließt start(): Unit


  // --- 2. ERFORDERLICHE IMPLEMENTIERUNG: Observer.updateDisplay() ---
  override def updateDisplay(): Unit = {
    Platform.runLater {
      val state = controller.getTurnState
      val players = controller.getCurrentPlayers

      if (players.isEmpty) {
        setupInitialGame()
        return
      }

      val currentPlayer = controller.getCurrentPlayer
      val prevPlayer = controller.getPrevPlayer
      val roundRank = controller.getRoundRank

      infoLabel.text = s"Aktueller Spieler: ${currentPlayer.name} | Angesagter Rang: ${if (roundRank.isEmpty) "Keiner" else roundRank}"
      logText.text = controller.getLog.mkString("\n")

      centerPane.children.clear()

      state match {
        case NeedsRankInput =>
          setupInputAndSubmit(
            prompt = "Rang eingeben (z.B. A):",
            action = handleRankInput,
            display = new Text("Bitte den gewünschten Rang für die Runde eingeben (2-10, B, D, K, A):")
          )

        case NeedsCardInput =>
          displayHandCards(currentPlayer)
          setupInputAndSubmit(
            prompt = "Karten-Indizes (z.B. 1,3):",
            action = handleCardInput(currentPlayer),
            display = new Text("Wähle bis zu drei Karten (Indizes kommasepariert):")
          )

        case NeedsChallengeDecision =>
          centerPane.children.add(new Text(s"${prevPlayer.name} hat Karten gelegt."))
          setupInputAndSubmit(
            prompt = "Lüge aufdecken? (j/n):",
            action = handleChallengeDecision,
            display = new Text(s"Lüge von Spieler ${prevPlayer.name} aufdecken? (j/n):")
          )

        case ChallengedLieWon | ChallengedLieLost =>
          val message = if (state == ChallengedLieWon) s"${prevPlayer.name} hat gelogen!" else s"${prevPlayer.name} zieht alle Karten."
          centerPane.children.add(new Text(message))
          setupNextTurnButton()

        case Played =>
          centerPane.children.add(new Text(s"${currentPlayer.name} hat Karten gelegt."))
          setupNextTurnButton()
      }
    }
  }

  // --- HILFSMETHODEN (Unverändert) ---
  private def setupInitialGame(): Unit = {
    centerPane.children.clear()
    centerPane.children.add(new Text("Willkommen bei Lügen. Starten Sie das Spiel."))
    centerPane.children.add(new Text("Bitte geben Sie die Anzahl der Spieler ein (2-8):"))

    submitButton.text = "Spiel starten"
    inputField.promptText = "Anzahl Spieler (2-8)"

    submitButton.onAction = _ => {
      val num = scala.util.Try(inputField.text.value.toInt).getOrElse(0)
      if (num >= 2 && num <= 8) {
        val playerNames = (1 to num).map(i => s"Spieler $i").toList
        controller.setupGame(num, playerNames)
        inputField.text = ""
      } else {
        infoLabel.text = "Ungültige Spieleranzahl (2-8)! Versuchen Sie es erneut."
      }
    }
  }

  private def setupInputAndSubmit(prompt: String, action: String => Unit, display: Text): Unit = {
    centerPane.children.add(display)
    inputField.promptText = prompt
    submitButton.text = "Bestätigen"
    submitButton.onAction = _ => action(inputField.text.value)
  }

  private def setupNextTurnButton(): Unit = {
    submitButton.text = "Nächster Zug"
    submitButton.onAction = _ => controller.nextTurn()
  }

  private def displayHandCards(player: Player): Unit = {
    val cardGrid = new GridPane {
      hgap = 10
      vgap = 5
      padding = Insets(10)
    }

    player.hand.zipWithIndex.foreach { case (card, index) =>
      cardGrid.add(new Label(s"${index + 1}"), index, 0)
      cardGrid.add(new Button(card.toString), index, 1)
    }
    centerPane.children.add(new Text(s"${player.name}'s Handkarten:"))
    centerPane.children.add(cardGrid)
  }

  private def handleRankInput(input: String): Unit = {
    val rank = input.trim.toUpperCase
    if (controller.isValidRanks.contains(rank)) {
      controller.handleRoundRank(rank)
      inputField.text = ""
    } else {
      infoLabel.text = s"Ungültiger Rang: ${rank}. Gültig: ${controller.isValidRanks.mkString(", ")}"
    }
  }

  private def handleCardInput(player: Player)(input: String): Unit = {
    val indices = scala.util.Try(input.split(",").map(_.trim.toInt).toList).getOrElse(Nil)
    if (indices.nonEmpty && indices.size <= 3 &&
      indices.forall(i => i >= 1 && i <= player.hand.size)) {
      controller.handleCardPlay(indices)
      inputField.text = ""
    } else {
      infoLabel.text = "Ungültige Kartenauswahl. Wähle 1-3 gültige Indizes (z.B. 1,3)."
    }
  }

  private def handleChallengeDecision(input: String): Unit = {
    val decision = input.trim.toLowerCase
    if (decision == "j" || decision == "n") {
      val callsLie = decision == "j"
      controller.handleChallengeDecision(callsLie)
      inputField.text = ""
    } else {
      infoLabel.text = "Bitte nur 'j' oder 'n' eingeben."
    }
  }
}