package de.htwg.luegen

import de.htwg.luegen.Controller.{GameController, HistoryEntry, Observer}
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

// Einfacher Dummy-Observer
class DummyObserver extends Observer {
  var updateCount: Int = 0
  override def updateDisplay(): Unit = updateCount += 1
}

class GameControllerSpec extends AnyWordSpec with Matchers {

  val cardA = Card("H", "A")
  val cardK = Card("H", "K")
  val card2 = Card("S", "2")

  val p1 = Player("P1", List(cardA, cardK))
  val p2 = Player("P2", List(card2))
  val p3 = Player("P3", Nil)
  val allPlayers = List(p1, p2, p3)

  Random.setSeed(42)

  /** * Initialisiert den Controller mit einem vollwertig eingerichteten Model.
   * (P1 ist CurrentPlayer) für Tests, die nicht den Setup-Flow beinhalten.
   */
  def setupInitialController(rank: String = "K", state: TurnState = NeedsCardInput): GameController = {
    val initialModel = GameModel().copy(
      players = allPlayers,
      playOrder = List(0, 1, 2),
      currentPlayerIndex = 0, // P1 ist am Zug (Index 0)
      lastPlayerIndex = 2, // P3 war vorher
      roundRank = rank,
      turnState = state,
      logHistory = List("Initialisierung abgeschlossen")
    )
    val controller = new GameController(initialModel)
    controller.registerObserver(new DummyObserver)
    controller
  }

  "A GameController (Functional)" should {

    "initGame should initialize and return the model" in {
      val model = GameModel()
      val controller = new GameController(model)
      controller.initGame() shouldBe model
    }

    // --- Memento / Undo / Redo Tests ---

    "handleRawInput(undo) sollte den Zustand korrekt zurücksetzen" in {
      val controller = setupInitialController()

      // 1. Erster Zug (wird auf den undoStack gelegt)
      controller.handleRawInput("A") // NeedsRankInput -> NeedsCardInput

      // 2. Zweiter Zug (wird auf den undoStack gelegt)
      controller.handleRawInput("1") // NeedsCardInput -> Played (wechselt zu P2)

      // Zustand nach 2 Zügen
      val modelAfterTwoMoves = controller.model
      modelAfterTwoMoves.currentPlayerIndex shouldBe 1
      modelAfterTwoMoves.discardedCards.size shouldBe 1
      modelAfterTwoMoves.logHistory.size shouldBe 2 // Init + 2 Commands

      // 3. UNDO ausführen
      val modelAfterUndo = controller.handleRawInput("undo")

      // Zustand nach einem UNDO
      modelAfterUndo.currentPlayerIndex shouldBe 0 // Zurückgesetzt auf P1
      modelAfterUndo.discardedCards.size shouldBe 0 // Zurückgesetzt
      modelAfterUndo.logHistory.last should include ("UNDO: Undid HandleCardPlay") // Log sollte persistent sein

      // 4. REDO ausführen
      val modelAfterRedo = controller.handleRawInput("redo")

      // Zustand nach REDO
      modelAfterRedo.currentPlayerIndex shouldBe 1 // Wieder auf P2
      modelAfterRedo.discardedCards.size shouldBe 1 // Wiederhergestellt
      modelAfterRedo.logHistory.last should include ("REDO: Redid HandleCardPlay")

      // 5. UNDO Stack sollte korrekt HistoryEntrys speichern
      // Zugriff auf private undoStack ist hier schwierig, daher nur über public Schnittstelle prüfen
    }

    // --- Validierung & handleRawInput Tests ---

    "handleRawInput should validate PlayerCount" in {
      val controller = new GameController(GameModel())

      // Erfolg
      val modelSuccess = controller.handleRawInput("4")
      modelSuccess.playerCount shouldBe 4
      modelSuccess.turnState shouldBe NeedsPlayerNames
      modelSuccess.lastInputError shouldBe None

      // Fehler (zu groß)
      val modelFailure = controller.handleRawInput("10")
      modelFailure.playerCount shouldBe 4 // Bleibt beim letzten gültigen Zustand
      modelFailure.lastInputError shouldBe Some("Gebe eine gueltige Zahl ein!")
    }

    "handleRawInput should validate PlayerNames" in {
      val controller = new GameController(GameModel().copy(playerCount = 2, turnState = NeedsPlayerNames))

      // Erfolg
      val modelSuccess = controller.handleRawInput("Anna, Bernd")
      modelSuccess.players.size shouldBe 2
      modelSuccess.turnState shouldBe NeedsRankInput
      modelSuccess.lastInputError shouldBe None

      // Fehler (zu wenige Namen)
      val modelFailureCount = controller.handleRawInput("Anna")
      modelFailureCount.players shouldBe empty // Bleibt beim letzten gültigen Zustand
      modelFailureCount.lastInputError.get should include ("2 Namen durch Komma")

      // Fehler (Name zu lang)
      val modelFailureLength = controller.handleRawInput("Anna, MaximilianIstZuLang")
      modelFailureLength.lastInputError.get should include ("max 10 Zeichen")
    }

    "handleRawInput should validate Rank" in {
      val controller = setupInitialController(rank = "", state = NeedsRankInput)

      // Erfolg
      val modelSuccess = controller.handleRawInput("A")
      modelSuccess.roundRank shouldBe "A"
      modelSuccess.lastInputError shouldBe None

      // Fehler
      val modelFailure = controller.handleRawInput("Z")
      modelFailure.roundRank shouldBe "" // Bleibt beim letzten gültigen Zustand
      modelFailure.lastInputError shouldBe Some("Gebe einen gueltigen Rang ein!")
    }

    "handleRawInput should validate CardInput" in {
      val controller = setupInitialController(rank = "A", state = NeedsCardInput) // P1 Hand: A, K

      // Erfolg
      val modelSuccess = controller.handleRawInput("1")
      modelSuccess.lastPlayedCards.size shouldBe 1
      modelSuccess.lastInputError shouldBe None

      // Fehler (zu viele Indizes)
      val modelFailureQuantity = controller.handleRawInput("1,2,3,4")
      modelFailureQuantity.lastInputError.get should include ("max 3 Indices")

      // Fehler (ungültiger Index)
      val modelFailureRange = controller.handleRawInput("5")
      modelFailureRange.lastInputError.get should include ("max 3 Indices") // Indirekt über 'max 3'

      // Fehler (keine Zahl)
      val modelFailureFormat = controller.handleRawInput("a,b")
      modelFailureFormat.lastInputError.get should include ("gueltige Zahlen ein!")
    }

    "handleRawInput should validate ChallengeDecision" in {
      val controller = setupInitialController(state = NeedsChallengeDecision)

      // Erfolg 'j'
      val modelSuccessJ = controller.handleRawInput("j")
      modelSuccessJ.turnState shouldBe ChallengedLieWon // Annahme, dass die Logik LieWon ergibt
      modelSuccessJ.lastInputError shouldBe None

      // Erfolg 'n'
      val modelSuccessN = setupInitialController(state = NeedsChallengeDecision)
      val modelAfterN = modelSuccessN.handleRawInput("n")
      modelAfterN.turnState shouldBe NeedsCardInput
      modelAfterN.lastInputError shouldBe None

      // Fehler
      val modelFailure = setupInitialController(state = NeedsChallengeDecision)
      val modelAfterFailure = modelFailure.handleRawInput("vielleicht")
      modelAfterFailure.lastInputError shouldBe Some("Gebe 'j' oder 'n' ein!")
    }

    // --- Bestehende Tests anpassen ---

    "Executing a command should update logHistory" in {
      val controller = setupInitialController(rank = "A")
      val modelAfterPlay = controller.handleRawInput("1")

      modelAfterPlay.logHistory.last should include ("HandleCardPlayCommand")
      modelAfterPlay.logHistory.size shouldBe 2 // Init + Command
    }

    "handleRoundRank (via raw input) sollte den Rang setzen" in {
      val initialModel = GameModel()
        .setupPlayers(List("A", "B"))
        .setupTurnOrder() // setzt currentPlayerIndex etc.
        .copy(turnState = NeedsRankInput)
      val controller = new GameController(initialModel)

      val finalModel = controller.handleRawInput("K")

      finalModel.roundRank shouldBe "K"
      finalModel.turnState shouldBe NeedsCardInput
    }

  }
}