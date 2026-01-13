package de.htwg.luegen.view

import de.htwg.luegen.model.impl1.Player
import scalafx.application.JFXApp3
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, Separator, Slider, TextField}
import scalafx.scene.layout.{VBox, HBox, Priority}
import scalafx.geometry.{Insets, Pos}
import de.htwg.luegen.controller.{IGameController, Observer}
import de.htwg.luegen.TurnState
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.{StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.collections.ObservableBuffer

class GuiView(using controller: IGameController) extends JFXApp3 with Observer {

  private var statusLabel: Label = _
  private var playerLabel: Label = _
  private var mainLayout: VBox = _
  private var gameBoard: VBox = _

  override def start(): Unit = {
    controller.registerObserver(this)

    stage = new JFXApp3.PrimaryStage {
      title = "Lügen"
      width = 1200
      height = 800
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
      style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: black;"
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

    Seq(titleLabel, startButton, exitButton)
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

  private val selectedIndices = ObservableBuffer[Int]()

  private def updateGameBoard(state: TurnState, player: Player): Unit = {
    if (gameBoard == null) return
    gameBoard.children.clear()
    
    val centeredTable = new StackPane {
      children = createTableLayout()
      alignment = Pos.Center
    }
    gameBoard.children.add(centeredTable)

    val actionArea = new VBox {
      spacing = 10
      alignment = Pos.Center
      padding = Insets(10)
    }

    state match {
      case TurnState.NeedsRankInput =>
        selectedIndices.clear()
        actionArea.children.addAll(
          new Label("Wähle einen Rang für die Runde:"),
          new HBox(5) {
            alignment = Pos.Center
            children = controller.isValidRanks.map { r =>
              new Button(r) {
                onAction = _ => controller.handleRoundRank(r)
              }
            }
          }
        )

      case TurnState.NeedsCardInput =>
        actionArea.children.add(new Label(s"Wähle 1-3 Karten (Gewählt: ${selectedIndices.size})"))

        val cardsHBox = new HBox(5) {
          alignment = Pos.Center
          children = player.hand.zipWithIndex.map { case (card, idx) =>
            val cardIdx = idx + 1
            new Button(card.toString) {
              style = if (selectedIndices.contains(cardIdx))
                "-fx-background-color: lightblue; -fx-border-color: blue;"
              else "-fx-background-color: white; -fx-border-color: black;"

              onAction = _ => {
                if (selectedIndices.contains(cardIdx)) {
                  selectedIndices -= cardIdx
                } else if (selectedIndices.size < 3) {
                  selectedIndices += cardIdx
                }
                updateDisplay()
              }
            }
          }
        }

        val playBtn = new Button("Karten legen") {
          // Deaktiviert, wenn keine Karte gewählt wurde
          disable = selectedIndices.isEmpty
          style = "-fx-background-color: seagreen; -fx-text-fill: white; -fx-font-weight: bold;"
          onAction = _ => {
            controller.handleCardInput(selectedIndices.toList)
            selectedIndices.clear()
          }
        }
        actionArea.children.addAll(cardsHBox, playBtn)

      case TurnState.NeedsChallengeDecision =>
        val prevPlayer = controller.getPrevPlayer
        actionArea.children.addAll(
          new Label(s"Hat ${prevPlayer.name} gelogen?"),
          new HBox(15) {
            alignment = Pos.Center
            children = Seq(
              new Button("Ja, aufdecken!") {
                style = "-fx-base: crimson;";
                onAction = _ => controller.handleChallengeDecision(true)
              },
              new Button("Nein, weiter") {
                onAction = _ => controller.handleChallengeDecision(false)
              }
            )
          }
        )

      case TurnState.ChallengedLieWon =>
        val prevPlayer = controller.getPrevPlayer
        val resultLabel = new Label(s"${prevPlayer.name} hat GELOGEN!") {
          style = "-fx-text-fill: crimson; -fx-font-size: 18px; -fx-font-weight: bold;"
        }
        val nextBtn = new Button("Nächster Spieler") {
          onAction = _ => controller.setNextPlayer()
        }
        actionArea.children.addAll(resultLabel, nextBtn)

      case TurnState.ChallengedLieLost =>
        val prevPlayer = controller.getPrevPlayer
        val resultLabel = new Label(s"${prevPlayer.name} hat die WAHRHEIT gesagt!") {
          style = "-fx-text-fill: seagreen; -fx-font-size: 18px; -fx-font-weight: bold;"
        }
        val nextBtn = new Button("Nächster Spieler") {
          onAction = _ => controller.setNextPlayer()
        }
        actionArea.children.addAll(resultLabel, nextBtn)

      case TurnState.Played | TurnState.ChallengedLieWon | TurnState.ChallengedLieLost =>
        actionArea.children.add(new Button("Nächster Spieler") {
          onAction = _ => controller.setNextPlayer()
        })

      case _ =>
    }

    gameBoard.children.add(actionArea)
  }

  private def createTableLayout(): Pane = {
    val pane = new Pane {
      prefWidth = 400
      prefHeight = 300
      maxWidth = 400
      maxHeight = 300
    }

    val table = new Rectangle {
      width = 250
      height = 150
      fill = Color.DarkGreen
      stroke = Color.SaddleBrown
      strokeWidth = 5
      arcWidth = 20
      arcHeight = 20
      layoutX = 75
      layoutY = 75
    }

    val discardedCount = controller.getDiscardedCount
    val displayRank = controller.getRoundRank
    val stackLabel = new Label(s"Deck: $discardedCount Rang: $displayRank") {
      style = "-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;"
      layoutX = 150
      layoutY = 135
    }

    pane.children.addAll(table, stackLabel)
    val players = controller.getCurrentPlayers
    val centerX = 200.0
    val centerY = 150.0
    val radiusX = 160.0
    val radiusY = 120.0

    players.zipWithIndex.foreach { case (p, i) =>
      val angle = 2 * math.Pi * i / players.size
      val px = centerX + radiusX * math.cos(angle) - 30
      val py = centerY + radiusY * math.sin(angle)

      val pLabel = new Label(s"${p.name} (${p.cardCount})") {
        style = if (p == controller.getCurrentPlayer)
          "-fx-background-color: yellow; -fx-padding: 2; -fx-font-weight: bold;"
        else "-fx-background-color: whitesmoke; -fx-padding: 2;"
        layoutX = px
        layoutY = py
      }
      pane.children.add(pLabel)
    }
    pane
  }


}