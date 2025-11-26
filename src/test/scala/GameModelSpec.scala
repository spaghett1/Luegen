package de.htwg.luegen.Model

import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.util.Random

class GameModelSpec extends AnyWordSpec with Matchers {

  // Setup Spieler und Karten
  val cardA = Card("H", "A")
  val cardK = Card("H", "K")
  val card2 = Card("S", "2")
  val card3 = Card("C", "3")

  // Funktionale Setup-Helfer
  def setupInitialModel(): GameModel = {
    // Manuelle Player-Instanzen für feste Reihenfolge und Hände
    val p1Hand = Player("P1").addCards(List(cardA, cardK))
    val p2Hand = Player("P2").addCards(List(card2))
    val p3Hand = Player("P3").addCards(List(card3))
    val allPlayers = List(p1Hand, p2Hand, p3Hand)

    // Stellt sicher, dass das Model mit den korrekten Indizes startet:
    // P1 (Index 0) ist am Zug, P3 (Index 2) war zuletzt dran.
    GameModel().copy(
      players = allPlayers,
      playOrder = List(0, 1, 2), // P1, P2, P3
      currentPlayerIndex = 0, // P1
      lastPlayerIndex = 2 // P3 (für getPrevPlayer)
    )
  }

  // Hilfsfunktion zum Abrufen des aktuellen Spielers
  def getCurrentPlayer(model: GameModel): Player = model.players(model.currentPlayerIndex)

  "A GameModel (Functional)" when {
    "initialized" should {
      "dealCards sollte ein NEUES Model mit 52 Karten zurückgeben" in {
        val model = GameModel().setupPlayers(List("A", "B", "C", "D"))
        val newModel = model.dealCards()
        newModel.players.map(_.hand.size).sum shouldBe 52
        newModel should not be model // Prüft Immutability
      }

      "setupTurnOrder sollte currentPlayerIndex und playOrder korrekt setzen" in {
        Random.setSeed(42) // Für vorhersagbaren Start
        val model = GameModel().setupPlayers(List("A", "B", "C"))
        val newModel = model.setupTurnOrder()

        newModel.currentPlayerIndex should be >= 0
        newModel.playOrder should not be empty
        newModel should not be model
      }

      "setupRank sollte den roundRank setzen und ein NEUES Model zurückgeben" in {
        val model = setupInitialModel()
        val newModel = model.setupRank("A")
        newModel.roundRank shouldBe "A"
        newModel.turnState shouldBe NoChallenge
        newModel should not be model
      }
    }

    "a player plays cards (playCards)" should {
      val initialModel = setupInitialModel()

      "Karten aus der Hand entfernen und den Ablagestapel im NEUEN Model hinzufügen" in {
        // P1 (Index 0) spielt Karte A (Index 1 in P1's Hand)
        val newModel = initialModel.playCards(List(1))

        // Prüfen des neuen Models
        newModel.lastPlayedCards should contain theSameElementsAs List(cardA)
        newModel.discardedCards.head shouldBe cardA
        newModel.amountPlayed shouldBe 1
        newModel.turnState shouldBe Played

        // Prüfen, dass der Spieler im neuen Model aktualisiert wurde
        newModel.players(0).hand should contain theSameElementsAs List(cardK)

        // Prüfen, dass das alte Model unverändert ist (Immutability)
        initialModel.players(0).hand should contain theSameElementsAs List(cardA, cardK)
        initialModel.discardedCards shouldBe empty
      }
    }

    "turn order is processed (setNextPlayer)" should {
      val model = setupInitialModel() // P1 ist currentPlayerIndex=0

      "setNextPlayer(Played) sollte zum nächsten Spieler (P2) wechseln" in {
        val modelPlayed = model.copy(turnState = Played)
        val newModel = modelPlayed.setNextPlayer()
        getCurrentPlayer(newModel).name shouldBe "P2"
        newModel.lastPlayerIndex shouldBe 0 // P1 war der letzte
      }

      "setNextPlayer(ChallengedLieLost) sollte zum nächsten Spieler (P2) wechseln und Rank leeren" in {
        val modelWithRank = model.setupRank("K") // Setze Rang
        val modelChallengedLost = modelWithRank.copy(turnState = ChallengedLieLost)
        val newModel = modelChallengedLost.setNextPlayer()
        getCurrentPlayer(newModel).name shouldBe "P2"
        newModel.roundRank shouldBe "" // Rang wird zurückgesetzt
      }

      "setNextPlayer(ChallengedLieWon) sollte den Spieler (P1) beibehalten und Rank leeren" in {
        val modelWithRank = model.setupRank("K")
        val modelChallengedWon = modelWithRank.copy(turnState = ChallengedLieWon)
        val newModel = modelChallengedWon.setNextPlayer()
        getCurrentPlayer(newModel).name shouldBe "P1"
        newModel.roundRank shouldBe "" // Rang wird zurückgesetzt
      }
    }

    "a lie is challenged (evaluateReveal)" should {

      "evaluateReveal sollte LieWon und korrekten Zustand zurückgeben, wenn gelogen wurde" in {
        // Setup: P1 spielt (Angeklagter), P2 deckt auf (Challenger)
        val modelAfterP1Play = setupInitialModel()
          .playCards(List(1)) // P1 spielt A. Hand: K. Discard: [A].
          .copy(roundRank = "2") // P1 lügt (sagt 2 an, spielt A)

        // Simuliere Wechsel zu P2 (Challenger)
        val modelP2Turn = modelAfterP1Play.copy(
          currentPlayerIndex = 1, // P2 am Zug
          lastPlayerIndex = 0, // P1 war vorher
          turnState = NoTurn
        )

        val p1HandSizeBefore = modelP2Turn.players(0).hand.size // 1

        // P2 (Challenger) ruft evaluateReveal auf
        val newModel = modelP2Turn.evaluateReveal()

        // 1. Outcome
        newModel.turnState shouldBe ChallengedLieWon

        // 2. Karten: P1 (Angeklagter/Lügner) zieht Karten.
        // P1 hatte K, zieht A -> Handgröße: 2
        newModel.players(0).hand.size shouldBe p1HandSizeBefore + 1
        newModel.discardedCards shouldBe empty

        // 3. Transienter Zustand
        newModel.lastAccusedIndex shouldBe 0 // P1 wurde angeklagt
      }

      "evaluateReveal sollte LieLost und korrekten Zustand zurückgeben, wenn die Wahrheit gesagt wurde" in {
        // Setup: P1 spielt (Angeklagter), P2 deckt auf (Challenger)
        val modelAfterP1Play = setupInitialModel()
          .playCards(List(1)) // P1 spielt A. Hand: K. Discard: [A].
          .copy(roundRank = "A") // P1 sagt Wahrheit (sagt A an, spielt A)

        // Simuliere Wechsel zu P2 (Challenger)
        val modelP2Turn = modelAfterP1Play.copy(
          currentPlayerIndex = 1, // P2 am Zug
          lastPlayerIndex = 0, // P1 war vorher
          turnState = NoTurn
        )

        val p2HandSizeBefore = modelP2Turn.players(1).hand.size // 1

        // P2 (Challenger) ruft evaluateReveal auf
        val newModel = modelP2Turn.evaluateReveal()

        // 1. Outcome
        newModel.turnState shouldBe ChallengedLieLost

        // 2. Karten: P2 (Challenger/Verlierer) zieht Karten.
        // P2 hatte S2, zieht A -> Handgröße: 2
        newModel.players(1).hand.size shouldBe p2HandSizeBefore + 1
        newModel.discardedCards shouldBe empty

        // 3. Transienter Zustand
        newModel.lastAccusedIndex shouldBe 0 // P1 wurde angeklagt
      }

      "drawAll sollte alle Karten funktional zur Spielerhand hinzufügen" in {
        val model = setupInitialModel().copy(discardedCards = List(cardA, cardK))
        val initialP1HandSize = model.players(0).hand.size // 2

        val p1 = model.players(0)
        val newModel = model.drawAll(p1) // P1 zieht

        newModel.players(0).hand.size shouldBe initialP1HandSize + 2
        newModel.discardedCards shouldBe empty
        newModel should not be model
      }
    }
  }
}