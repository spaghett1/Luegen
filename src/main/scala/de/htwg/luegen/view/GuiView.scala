package de.htwg.luegen.view

import de.htwg.luegen.model.impl1.Player
import scalafx.application.JFXApp3
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, MenuBar, MenuButton, MenuItem, Separator, SeparatorMenuItem, Slider, TextField}
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, BorderPane, HBox, Pane, StackPane, VBox}
import scalafx.geometry.{Insets, Pos}
import de.htwg.luegen.controller.{IGameController, Observer}
import de.htwg.luegen.TurnState
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.collections.ObservableBuffer
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.Includes.*
import scalafx.scene.image.Image
import scalafx.geometry.Side

import java.io.InputStream

class GuiView(using controller: IGameController) extends JFXApp3 with Observer {

  private var statusLabel: Label = _
  private var playerLabel: Label = _
  private var mainLayout: VBox = _
  private var gameBoard: VBox = _

  override def start(): Unit = {
    controller.registerObserver(this)
    val menuBar = new MenuBar {
      menus = List(
        new scalafx.scene.control.Menu("Datei") {
          items = List(
            new MenuItem("Speichern") {
              onAction = _ => controller.save
            },
            new MenuItem("Laden") {
              onAction = _ => controller.load
            },
            new MenuItem("Undo") {
              onAction = _ => controller.undo()
            },
            new MenuItem("Redo") {
              onAction = _ => controller.redo()
            },
            new SeparatorMenuItem(),
            new MenuItem("Hauptmenü") {
              onAction = _ => mainLayout.children = createMenuLayout()
            },
            new MenuItem("Beenden") {
              onAction = _ => sys.exit(0)
            }
          )
        }
      )
    }

    stage = new JFXApp3.PrimaryStage {
      title = "Lügen"
      maximized = true
      fullScreen = false
      onCloseRequest = _ => System.exit(0)
      scene = new Scene {
        val css = getClass.getResource("/styles.css")
        if (css != null) stylesheets.add(css.toExternalForm)

        mainLayout = new VBox {
          padding = Insets(20)
          spacing = 15
          alignment = Pos.Center
          background = createGameBackground()
          children = createMenuLayout()
        }
        root = new BorderPane {
          top = menuBar
          left = null
          center = mainLayout
          style = "-fx-background-color: transparent;"
        }
      }
    }
  }

  private def createMenuLayout(): Seq[scalafx.scene.Node] = {
    val titleLabel = new Label("Willkommen bei Lügen") {
       style = "-fx-font-size: 34px; -fx-text-fill: white;"
    }

    val startButton = new Button("Neues Spiel") {
      prefWidth = 250
      styleClass.addAll("modern-button", "action-button")
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
      styleClass.add("white-slider")
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 1
      minorTickCount = 0
      snapToTicks = true
      prefWidth = 400
    }

    val sliderLabel = new Label(s"Spieler: ${playerSlider.value.value.intValue()}")
    { style = "-fx-font-size: 18px; -fx-text-fill: white;" }
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
        showMainGameLayout()
      }
    }

    mainLayout.children = Seq(
      new Label("Konfiguration") { style = "-fx-font-size: 24px; -fx-text-fill: white;" },
      new HBox(15, sliderLabel, playerSlider) { alignment = Pos.Center },
      new Label("Namen:") { style = "-fx-font-size: 24px; -fx-text-fill: white;" },
      nameFieldsContainer,
      confirmBtn,
      new Button("Abbrechen") { onAction = _ => mainLayout.children = createMenuLayout() }
    )
  }
  
  private def showMainGameLayout(): Unit = {
    statusLabel = new Label("Spiel beginnt...") { style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;" }
    playerLabel = new Label("") {style = "-fx-text-fill: white; -fx-font-size: 14px" }

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
      gameBoard
    )
  }

  override def updateDisplay(): Unit = {
    Platform.runLater {
      try {
        val state = controller.getTurnState
        val borderPane = stage.scene().root().asInstanceOf[javafx.scene.layout.BorderPane]

     
        state match {
          case TurnState.NeedsPlayerCount | TurnState.NeedsPlayerNames =>
            borderPane.setLeft(null)
            showConfigLayout()

          case _ =>
            if (statusLabel == null) showMainGameLayout()
            borderPane.setLeft(createQuartetDisplay().delegate)
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
      case TurnState.GameOver =>
        val msg = controller.getInputError.getOrElse("Das Spiel ist vorbei!")
        actionArea.children.addAll(
          new Label("Spiel beendet") {
            style = "-fx-font-size: 32px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;"
          },
          new Button("Neues Spiel starten") {
            styleClass.addAll("modern-button", "action-button")
            onAction = _ => showConfigLayout()
          }
        )

      case TurnState.NeedsRankInput =>
        selectedIndices.clear()
        actionArea.children.addAll(
          new Label("Wähle einen Rang für die Runde:") {style = "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"},
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
        actionArea.children.add(new Label(s"Wähle 1-3 Karten (Gewählt: ${selectedIndices.size})")
        { style = "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"})

        // Wir nutzen ein Pane statt einer HBox für manuelle Positionierung
        val cardsPane = new Pane {
          val maxViewWidth = 800.0 // Die maximale Breite, die die Karten einnehmen dürfen
          val cardWidth = 100.0    // Die Breite einer einzelnen Karte (fitHeight ist 100)
          val totalNormalWidth = player.hand.size * (cardWidth + 10) // Normale Breite mit Puffer

          // Berechne den Versatz (Spacing):
          // Entweder normal (110) oder gestaucht, falls nicht genug Platz ist
          val spacing = if (totalNormalWidth > maxViewWidth && player.hand.size > 1) {
            (maxViewWidth - cardWidth) / (player.hand.size - 1)
          } else {
            cardWidth + 10
          }

          // Zentrierung des Panes berechnen
          val actualWidth = if (player.hand.size > 0) (player.hand.size - 1) * spacing + cardWidth else 0
          prefWidth = actualWidth
          maxWidth = actualWidth
          prefHeight = 120

          children = player.hand.zipWithIndex.map { case (card, idx) =>
            val cardIdx = idx + 1
            val isSelected = selectedIndices.contains(cardIdx)

            val imagePath = s"/images/cards/${card.suit}${card.rank}.png"
            val stream = getClass.getResourceAsStream(imagePath)

            val cardNode = if (stream != null) {
              new ImageView(new Image(stream)) {
                fitHeight = 100
                preserveRatio = true
              }
            } else {
              new Label(card.toString) { style = "-fx-background-color: white; -fx-padding: 10;" }
            }

            new StackPane {
              children = Seq(cardNode)
              padding = Insets(5)
              layoutX = idx * spacing

              style = if (isSelected) {
                "-fx-border-color: #3498db; " +
                  "-fx-border-width: 4; " +
                  "-fx-border-radius: 5; " +
                  "-fx-background-radius: 5; " +
                  "-fx-background-color: rgba(52, 152, 219, 0.4); " +
                  "-fx-translate-y: -15;"
            } else {
            "-fx-border-color: black; " +
              "-fx-border-width: 1; " +
              "-fx-border-radius: 5; " +
              "-fx-background-color: white; " +
              "-fx-background-radius: 5;"
          }

              onMouseClicked = (e: MouseEvent) => {
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
          disable = selectedIndices.isEmpty
          styleClass.addAll("modern-button", "action-button")
          onAction = _ => {
            controller.handleCardInput(selectedIndices.toList)
            selectedIndices.clear()
          }
        }

        val centeredCards = new HBox {
          alignment = Pos.Center
          children = Seq(cardsPane)
        }

        actionArea.children.addAll(centeredCards, playBtn)

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

    val centerImage = new ImageView {
      val imgStream = getClass.getResourceAsStream("/images/cards/card_back_red.png")

      if (imgStream != null) {
        image = new Image(imgStream)
      } else {
        println("Fehler: Bild unter /images/cards.png nicht gefunden!")
      }

      fitWidth = 60
      preserveRatio = true
      layoutX = 170
      layoutY = 85

      visible = discardedCount > 0
    }

    //val discardedCount = controller.getDiscardedCount
    val displayRank = controller.getRoundRank
    val stackLabel = new Label(s"Deck: $discardedCount Rang: $displayRank") {
      style = "-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;"
      layoutX = 150
      layoutY = 180
    }

    pane.children.addAll(table,centerImage, stackLabel)

    import de.htwg.luegen.model.impl1.Utils.TurnOrderUtils
    val players = controller.getCurrentPlayers

    val visualOrder = TurnOrderUtils.mapOrderToPlayerCount(players)

    val centerX = 200.0
    val centerY = 150.0
    val radiusX = 160.0
    val radiusY = 120.0

    val count = players.size

    // Wir nutzen hier direkt allPlayers.zipWithIndex, um die logische
    // Reihenfolge 1, 2, 3, 4... einzuhalten
    // Wir nutzen direkt die Liste der Spieler, um die Reihenfolge 1, 2, 3, 4... einzuhalten
    players.zipWithIndex.foreach { case (p, idx) =>
      val count = players.size
      val dynamicLeftOffset = math.max(0, (p.name.length - 4) * 7)

      val (px, py) = idx match {
        // --- SPIELER 1 ---
        case 0 =>
          if (count > 4) (130.0, 40.0) else (170.0, 40.0) // Oben Mitte/Links

        // --- SPIELER 2 ---
        case 1 =>
          if (count <= 2) (170.0, 260.0) // Bei 2 Spielern: Gegenüber (Unten Mitte)
          else if (count <= 4) (330.0, 140.0) // Bei 3-4 Spielern: Rechts Mitte
          else (230.0, 40.0) // Ab 5 Spielern: Oben Rechts (Partner von P1)

        // --- SPIELER 3 ---
        case 2 =>
          if (count <= 3) (170.0, 260.0) // Bei 3 Spielern: Unten Mitte
          else if (count <= 4) (170.0, 260.0) // Bei 4 Spielern: Unten Mitte
          else (330.0, 110.0) // Ab 5 Spielern: Rechts Oben

        // --- SPIELER 4 ---
        case 3 =>
          if (count <= 4) (40.0 - dynamicLeftOffset, 140.0) // Bei 4 Spielern: Links Mitte
          else (330.0, 180.0) // Ab 5 Spielern: Rechts Unten

        // --- SPIELER 5 ---
        case 4 => (230.0, 260.0) // Unten Rechts

        // --- SPIELER 6 ---
        case 5 => (130.0 - dynamicLeftOffset, 260.0) // Unten Links

        // --- SPIELER 7 ---
        case 6 => (40.0 - dynamicLeftOffset, 180.0) // Links Unten

        // --- SPIELER 8 ---
        case 7 => (40.0 - dynamicLeftOffset, 110.0) // Links Oben

        case _ => (200.0, 150.0)
      }

      val pLabel = new Label(s"${p.name} (${p.cardCount})") {
        style = if (p == controller.getCurrentPlayer)
          "-fx-background-color: yellow; -fx-padding: 2; -fx-font-weight: bold; -fx-text-fill: black;"
        else "-fx-background-color: whitesmoke; -fx-padding: 2; -fx-text-fill: black;"
        layoutX = px
        layoutY = py
      }
      pane.children.add(pLabel)
    }
    pane
  }

  private def createGameBackground(): Background = {
    val imgStream = getClass.getResourceAsStream("/images/background.png")
    if (imgStream == null) {
      println("Fehler: Hintergrundbild nicht gefunden!")
      return Background.Empty
    }

    val bgImage = new Image(imgStream)
    val backgroundSize = new BackgroundSize(
      width = 100,
      height = 100,
      widthAsPercentage = true,
      heightAsPercentage = true,
      contain = false,
      cover = true
    )

    new Background(Array(new BackgroundImage(
      bgImage,
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Center,
      backgroundSize
    )))
  }

  private def createQuartetDisplay(): VBox = {
    // Zuerst die Daten vom Controller holen
    val discardedQuartets = controller.getAllDiscardedQuartets

    new VBox {
      spacing = 10
      padding = Insets(20)
      alignment = Pos.TopLeft
      minWidth = 150
      style = "-fx-background-color: #8B4513; " +
        "-fx-border-color: #5D2E0A; " +
        "-fx-border-width: 0 2 2 0;"

      children = Seq(
        new Label("Entfernte\nQuartette:") {
          style = "-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;"
        }
        ) ++ discardedQuartets.map { rank =>
          val imagePath = s"/images/cards/♠$rank.png"
          val stream = getClass.getResourceAsStream(imagePath)

          if (stream != null) {
            new ImageView(new Image(stream)) {
              fitHeight = 60
              preserveRatio = true
            }
          } else {
            new Label(s"[$rank]") {
              style = "-fx-text-fill: white;"
            }
          }
        }
    }
  }
}