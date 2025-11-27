package de.htwg.luegen.Controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.luegen.Model._
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*
import de.htwg.luegen.Controller.Observer
import scala.util.Random

// Einfacher Dummy-Observer
class DummyObserver extends Observer {
  var updateCount: Int = 0
  override def updateDisplay(): Unit = updateCount += 1
}

class GameControllerNonMockitoSpec extends AnyWordSpec with Matchers {

  // Setup feste Konfiguration für vorhersagbare Ergebnisse
  val cardA = Card("H", "A")
  val cardK = Card("H", "K")
  val card2 = Card("S", "2")

  // Funktionale Player-Instanzen (Indizes werden vom Model verwaltet)
  val p1 = Player("P1", List(cardA, cardK))
  val p2 = Player("P2", List(card2))
  val p3 = Player("P3", Nil)
  val allPlayers = List(p1, p2, p3)

  // Seed für vorhersagbare setupTurnOrder
  Random.setSeed(42)

  /** * Initialisiert den Controller mit einem vollwertig eingerichteten Model
   * (P1 ist CurrentPlayer) für Tests, die nicht den Setup-Flow beinhalten.
   */
  def setupInitialController(rank: String = "K"): GameController = {
    // Erstelle ein vollwertig initialisiertes Model
    val initialModel = GameModel().copy(
      players = allPlayers,
      playOrder = List(0, 1, 2),
      // KORREKTUR: Setze Indizes für das indexbasierte Model
      currentPlayerIndex = 0, // P1 ist am Zug (Index 0)
      lastPlayerIndex = 2, // P3 war vorher
      roundRank = rank
    )
    val controller = new GameController(initialModel)
    controller.registerObserver(new DummyObserver)
    controller
  }

  "A GameController (Functional Return Values)" should {

    "initGame and give back the model" in {
      val model = GameModel()
      val controller = new GameController(model)
      controller.initGame() shouldBe model
    }
    "setupGame sollte das korrekte End-Model zurückgeben und den Zustand setzen" in {
      val controller = new GameController(GameModel())
      val finalModel = controller.setupGame(3, List("A", "B", "C"))

      finalModel.players.size shouldBe 3
      finalModel.players.map(_.hand.size).sum shouldBe 52
      finalModel.currentPlayerIndex should be >= 0 // Prüft, ob ein Index gesetzt wurde
      finalModel.turnState shouldBe NeedsRankInput

      // Prüfe, ob das Controller-Model tatsächlich aktualisiert wurde
      controller.getCurrentPlayers.size shouldBe 3
    }

    "Executing a command should use the LoggingCommandDecorator and update logHistory" in {
      val controller = setupInitialController(rank = "A")
      val modelAfterPlay = controller.handleCardPlay(List(1))

      modelAfterPlay.logHistory.last should include ("HandleCardPlayCommand")
      modelAfterPlay.logHistory.size shouldBe 1
    }

    "handleRoundRank sollte den Rang setzen und den TurnState auf NoChallenge setzen" in {
      val initialModel = GameModel()
        .setupPlayers(List("A", "B"))
        .setupTurnOrder() // setzt currentPlayerIndex etc.
      val controller = new GameController(initialModel)

      val finalModel = controller.handleRoundRank("K")

      finalModel.roundRank shouldBe "K"
      finalModel.turnState shouldBe NeedsCardInput
    }

    "handleCardPlay sollte Karten spielen, zum nächsten Spieler wechseln (P2) und Model zurückgeben" in {
      val controller = setupInitialController()

      // P1 (Index 0) ist am Zug, spielt cardA (Index 1 in P1's Hand)
      val modelAfterPlay = controller.handleCardPlay(List(1))

      // 1. Karten gespielt:
      modelAfterPlay.discardedCards.head shouldBe cardA
      controller.getPlayedCards should contain theSameElementsAs List(cardA)

      // 2. Spielerwechsel: P2 sollte nun am Zug sein (Index 1)
      modelAfterPlay.currentPlayerIndex shouldBe 1
      controller.getCurrentPlayer.name shouldBe "P2"
      modelAfterPlay.turnState shouldBe NeedsChallengeDecision

      // 3. P1 Hand sollte reduziert sein
      modelAfterPlay.players.find(_.name == "P1").get.hand.size shouldBe 1

      // 4. Controller-Model muss aktualisiert sein
      controller.getCurrentPlayer.name shouldBe "P2"
    }

    "handleChallengeDecision (callsLie=true) - LieWon sollte P2 am Zug lassen und Model zurückgeben" in {
      // Setup: P1 (Angeklagter) hat gelogen. P2 (Challenger) ist am Zug.
      val initialModel = GameModel().copy(
        players = allPlayers,
        playOrder = List(0, 1, 2),
        currentPlayerIndex = 1, // P2 ist Challenger
        lastPlayerIndex = 0, // P1 ist Angeklagter
        roundRank = "2",
        lastPlayedCards = List(cardK),
        amountPlayed = 1,
        turnState = NoTurn
      )
      val controller = new GameController(initialModel)
      controller.registerObserver(new DummyObserver)

      val modelAfterChallenge = controller.handleChallengeDecision(callsLie = true)

      // 1. Outcome: Lie Won
      modelAfterChallenge.turnState shouldBe ChallengedLieWon

      // 2. Karten: P1 (Angeklagter) zieht Karten.
      controller.getDiscardedCount shouldBe 0

      // 3. Spielerwechsel: P2 (Gewinner) bleibt am Zug.
      modelAfterChallenge.currentPlayerIndex shouldBe 1
      controller.getCurrentPlayer.name shouldBe "P2"

      // 4. Controller-Model muss aktualisiert sein
      controller.getCurrentPlayer.name shouldBe "P2"
    }

    //--------------------------------------------------------------------------------

    "Getter Methods" should {
      val controller = setupInitialController()

      "getCurrentPlayers sollte die korrekte Spielerliste zurückgeben" in {
        controller.getCurrentPlayers should contain theSameElementsInOrderAs allPlayers
      }

      "getCurrentPlayer sollte den aktuellen Spieler (P1) zurückgeben" in {
        // Der Controller verwendet model.players(model.currentPlayerIndex)
        controller.getCurrentPlayer shouldBe p1
      }

      "getPrevPlayer sollte den korrekten vorherigen Spieler (P3) zurückgeben" in {
        // getPrevPlayer delegiert an das Model, das den Index 2 berechnen sollte.
        controller.getPrevPlayer shouldBe p3
      }

      "getRoundRank sollte den gesetzten Rang ('K') zurückgeben" in {
        controller.getRoundRank shouldBe "K"
      }

      "isValidRanks sollte die Liste der gültigen Ränge zurückgeben" in {
        controller.isValidRanks.size shouldBe 13
        controller.isValidRanks should contain("A")
      }

      "isFirstTurn sollte false zurückgeben, wenn roundRank gesetzt ist" in {
        controller.isFirstTurn shouldBe false

        // Test für true (wenn roundRank leer ist)
        val controllerEmpty = setupInitialController(rank = "")
        controllerEmpty.isFirstTurn shouldBe true
      }

      "getTurnState sollte den aktuellen Zustand (NoTurn) zurückgeben" in {
        controller.getTurnState shouldBe NoTurn
      }

      "getDiscardedCount und getPlayedCards sollten nach einem Zug aktualisiert werden" in {
        // Controller ausführen, um den Zustand zu ändern
        val modelAfterPlay = controller.handleCardPlay(List(1))

        // Prüfen, ob der Controller den neuen Zustand verwendet
        controller.getDiscardedCount shouldBe 1
        controller.getPlayedCards should contain theSameElementsAs List(cardA)
        controller.getCurrentPlayer.name shouldBe "P2" // Spieler hat gewechselt
      }

      "getCurrentPlayerType should return the player type" in {
        val model = GameModel()
        val modelWithPlayer = model.copy(
          players = List(Player("test", playerType = Human)),
          currentPlayerIndex = 0
        )
        val received = controller.getCurrentPlayerType
        received shouldBe Human
      }
    }
  }
}