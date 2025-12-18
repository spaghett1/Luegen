package de.htwg.luegen.View

import scalafx.application.JFXApp3
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, Separator, Slider, TextField}
import scalafx.scene.layout.{VBox, HBox, Priority}
import scalafx.geometry.{Insets, Pos}
import de.htwg.luegen.Controller.{IGameController, Observer}
import de.htwg.luegen.TurnState
import de.htwg.luegen.Model.Player

class GuiView(controller: IGameController) extends JFXApp3 with Observer {

  private var statusLabel: Label = _
  private var playerLabel: Label = _
  private var mainLayout: VBox = _
  private var gameBoard: VBox = _

  override def start(): Unit = {
    controller.registerObserver(this)

    stage = new JFXApp3.PrimaryStage {
      title = "Lügen"
      width = 900
      height = 700
      scene = new Scene {
        mainLayout = new VBox {
          padding = Insets(20)
          spacing = 15
          alignment = Pos.Center
          children = createMenuLayout()
        }
        root = mainLayout
      }
    }
  }

  private def createMenuLayout(): Seq[scalafx.scene.Node] = {
    val titleLabel = new Label("Willkommen bei Lügen") {
      style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    }

    val startButton = new Button("Neues Spiel") {
      prefWidth = 250
      style = "-fx-font-size: 16px;"
      onAction = _ => showConfigLayout()
    }

    val exitButton = new Button("Beenden") {
      prefWidth = 250
      onAction = _ => System.exit(0)
    }

    Seq(titleLabel, new Separator(), startButton, exitButton)
  }
  
  private def showConfigLayout(): Unit = {
    val nameFieldsContainer = new VBox {
      spacing = 10
      alignment = Pos.Center
    }

    def updateNameFields(count: Int): Unit = {
      nameFieldsContainer.children = (1 to count).map { i =>
        new TextField {
          promptText = s"Name Spieler $i"
          text = s"Spieler $i"
          maxWidth = 200
        }
      }
    }

    val playerSlider = new Slider(2, 8, 3) {
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 1
      minorTickCount = 0
      snapToTicks = true
      prefWidth = 200
    }

    val sliderLabel = new Label(s"Spieler: ${playerSlider.value.value.intValue()}")
    updateNameFields(playerSlider.value.value.intValue())

    playerSlider.value.onChange { (_, _, newValue) =>
      sliderLabel.text = s"Spieler: ${newValue.intValue()}"
      updateNameFields(newValue.intValue())
    }

    val confirmBtn = new Button("Spiel starten") {
      style = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
      prefWidth = 200
      onAction = _ => {
        val count = playerSlider.value.value.intValue()
        val names = nameFieldsContainer.children.collect {
          case tf: javafx.scene.control.TextField => tf.getText
        }.toList

        controller.handlePlayerCount(count)
        controller.handlePlayerNames(names)
        showMainGameLayout() // WECHSEL ZUM SPIEL
      }
    }

    mainLayout.children = Seq(
      new Label("Konfiguration") { style = "-fx-font-size: 24px;" },
      new HBox(15, sliderLabel, playerSlider) { alignment = Pos.Center },
      new Label("Namen:"),
      nameFieldsContainer,
      confirmBtn,
      new Button("Abbrechen") { onAction = _ => mainLayout.children = createMenuLayout() }
    )
  }
  
  private def showMainGameLayout(): Unit = {
    statusLabel = new Label("Spiel beginnt...") { style = "-fx-font-size: 18px; -fx-font-weight: bold;" }
    playerLabel = new Label("")

    gameBoard = new VBox {
      spacing = 20
      alignment = Pos.Center
      padding = Insets(20)
    }

    val controls = new HBox(10) {
      alignment = Pos.Center
      children = Seq(
        new Button("Undo") { onAction = _ => controller.undo() },
        new Button("Redo") { onAction = _ => controller.redo() }
      )
    }

    mainLayout.children = Seq(
      statusLabel,
      playerLabel,
      new Separator(),
      gameBoard,
      new Separator(),
      controls,
      new Button("Hauptmenü") { onAction = _ => mainLayout.children = createMenuLayout() }
    )
  }

  override def updateDisplay(): Unit = {
    Platform.runLater {
      try {
        val state = controller.getTurnState

     
        state match {
          case TurnState.NeedsPlayerCount | TurnState.NeedsPlayerNames =>
            
            showConfigLayout()

          case _ =>
            if (statusLabel == null) showMainGameLayout()

            val currentPlayer = controller.getCurrentPlayer
            statusLabel.text = s"Status: $state"
            playerLabel.text = s"Am Zug: ${currentPlayer.name}"
            updateGameBoard(state, currentPlayer)
        }
      } catch {
        case e: Exception => println(s"GUI Update Fehler: ${e.getMessage}")
      }
    }
  }

  private def updateGameBoard(state: TurnState, player: Player): Unit = {
    if (gameBoard == null) return
    gameBoard.children.clear()

    state match {
      case TurnState.NeedsRankInput =>
        val instructionLabel = new Label("Wähle einen Rang für die Runde:") {
          style = "-fx-font-weight: bold; -fx-padding: 10;"
        }
        
        val rankButtons = new HBox(5) {
          alignment = Pos.Center
          children = controller.isValidRanks.map { r =>
            new Button(r) {
              prefWidth = 40
              onAction = _ => controller.handleRoundRank(r)
            }
          }
        }
        gameBoard.children.addAll(instructionLabel, rankButtons)
        
      case TurnState.NeedsCardInput =>
        val cardsLabel = new Label(s"Deine Karten (${player.cardCount}):")
        val cardsHBox = new HBox(5) {
          alignment = Pos.Center
          children = player.hand.zipWithIndex.map { case (card, idx) =>
            new Button(card.toString) {
              style = "-fx-background-color: white; -fx-border-color: black; -fx-min-width: 50;"
              onAction = _ => controller.handleCardInput(List(idx + 1))
            }
          }
        }
        gameBoard.children.addAll(cardsLabel, cardsHBox)
        
      case TurnState.NeedsChallengeDecision =>
        val prevPlayer = controller.getPrevPlayer
        val challengeLabel = new Label(s"Hat ${prevPlayer.name} gelogen?")
        val decisionBox = new HBox(15) {
          alignment = Pos.Center
          children = Seq(
            new Button("Ja, aufdecken!") {
              style = "-fx-base: #e74c3c;"
              onAction = _ => controller.handleChallengeDecision(true)
            },
            new Button("Nein, weiter") {
              onAction = _ => controller.handleChallengeDecision(false)
            }
          )
        }
        gameBoard.children.addAll(challengeLabel, decisionBox)
        
      case TurnState.Played | TurnState.ChallengedLieWon | TurnState.ChallengedLieLost =>
        val nextBtn = new Button("Nächster Spieler") {
          prefWidth = 150
          onAction = _ => controller.setNextPlayer()
        }
        gameBoard.children.add(nextBtn)

      case _ =>
        gameBoard.children.add(new Label("Warte auf Eingabe..."))
    }
  }
}