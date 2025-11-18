package de.htwg.luegen.Model

import de.htwg.luegen.Model.Utils.TurnOrderUtils
import de.htwg.luegen.Outcomes
import de.htwg.luegen.Outcomes.{ChallengedLieLost, ChallengedLieWon, Played}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameModelSpec extends AnyWordSpec with Matchers {

  // Setup Spieler und Karten
  val player1 = Player("P1")
  val player2 = Player("P2")
  val player3 = Player("P3")
  val cardA = Card("H", "A")
  val cardK = Card("H", "K")
  val card2 = Card("S", "2")
  val card3 = Card("C", "3")

  def setupModel(): GameModel = {
    val model = new GameModel()
    model.players = List(player1, player2, player3)

    // Setze eine vorhersagbare Reihenfolge für P1, P3, P2 (Indizes: 0, 2, 1)
    model.playOrder = List(0,1,2)
    model.currentPlayer = player1

    // Reset Spielerhände
    player1.hand = List(cardA, cardK)
    player2.hand = List(card2)
    player3.hand = List(card3)
    model
  }

  "A GameModel" when {
    "initialized" should {
      "Spieler korrekt initialisieren" in {
        val model = new GameModel()
        model.setupPlayers(List("Alice", "Bob"))
        model.players.map(_.name) should contain theSameElementsAs List("Alice", "Bob")
      }

      "52 Karten an die Spieler verteilen" in {
        val model = new GameModel()
        model.setupPlayers(List("A", "B", "C", "D"))
        model.dealCards()
        model.players.map(_.hand.size).sum shouldBe 52
      }

      "setupTurnOrder sollte die currentPlayer setzen" in {
        val model = setupModel()
        val expectedOrder = model.playOrder
        // Da die Reihenfolge bereits im Setup gesetzt ist, wird hier nur die Methode getestet
        model.setupPlayers(List("A", "B", "C"))
        model.dealCards()
        model.setupTurnOrder()
        model.currentPlayer should not be Player()
        model.playOrder should not be empty
        model.playOrder should not be empty
      }

      "isFirstTurn korrekt melden" in {
        val model = new GameModel()
        model.isFirstTurn shouldBe true
        model.roundRank = "A"
        model.isFirstTurn shouldBe false
      }
    }

    "a player plays cards" should {
      val model = setupModel()
      "Karten aus der Hand entfernen und zum Ablagestapel hinzufügen" in {
        // P1 spielt Karte A (Index 1)
        val playedCards = model.playCards(List(1))

        playedCards should contain theSameElementsAs List(cardA)
        player1.hand should contain theSameElementsAs List(cardK)
        model.discardedCards.head shouldBe cardA
        model.amountPlayed shouldBe 1
      }
    }

    "turn order is processed" should {
      val model = setupModel()

      "getPrevPlayer sollte den korrekten vorherigen Spieler zurückgeben" in {
        // P1 (aktuell) -> P3 (vorherig in der Reihenfolge [0, 2, 1])
        model.getPrevPlayer() shouldBe player3

        // Setze aktuellen Spieler auf P3
        model.currentPlayer = player3

        // P3 (aktuell) -> P2 (vorherig)
        model.getPrevPlayer() shouldBe player2
      }

      "setNextPlayer sollte den nächsten Spieler korrekt setzen (Played)" in {
        model.currentPlayer = player1 // Start P1
        model.setNextPlayer(Played)
        model.currentPlayer shouldBe player2
      }

      "setNextPlayer sollte den nächsten Spieler korrekt setzen (ChallengedLieLost)" in {
        model.currentPlayer = player3 // Start P3
        model.setNextPlayer(ChallengedLieLost)
        model.currentPlayer shouldBe player1
      }

      "setNextPlayer sollte den aktuellen Spieler behalten (ChallengedLieWon)" in {
        model.currentPlayer = player2 // Start P2
        model.setNextPlayer(ChallengedLieWon)
        model.currentPlayer shouldBe player2
      }

      "setNextPlayer sollte den nächsten Spieler setzen (Invalid)" in {
        model.currentPlayer = player3 // Start P2
        model.setNextPlayer(Outcomes.Invalid)
        model.currentPlayer shouldBe player1 // Wrap
      }
    }

    "a lie is challenged" should {
      "evaluateReveal sollte ChallengedLieWon zurückgeben, wenn gelogen wurde" in {
        val model = setupModel()
        model.roundRank = "K" // Angesagter Rang ist König
        model.amountPlayed = 1
        model.discardedCards.push(card2) // P3 (prevPlayer) hat eine 2 gespielt und gelogen

        model.currentPlayer = player2

        val outcome = model.evaluateReveal()

        outcome shouldBe ChallengedLieWon
        player1.hand.size shouldBe 3 // P3 zieht die Karten [Card3, Card2]
        model.discardedCards shouldBe empty
      }

      "evaluateReveal sollte ChallengedLieLost zurückgeben, wenn die Wahrheit gesagt wurde" in {
        val model = setupModel()
        model.roundRank = "A" // Angesagter Rang ist Ass
        model.amountPlayed = 1
        model.discardedCards.push(cardA) // P3 (prevPlayer) hat ein Ass gespielt und die Wahrheit gesagt

        model.currentPlayer = player1

        val outcome = model.evaluateReveal()

        outcome shouldBe ChallengedLieLost
        player1.hand.size shouldBe 3 // P1 (der Aufdecker) zieht die Karten [CardA, CardK, CardA]
        model.discardedCards shouldBe empty
      }

      "drawAll sollte alle Karten vom Stapel zur Spielerhand hinzufügen" in {
        val model = setupModel()
        model.discardedCards.push(cardA, cardK)
        val initialP1HandSize = player1.hand.size

        model.drawAll(player1)

        player1.hand.size shouldBe initialP1HandSize + 2
        model.discardedCards shouldBe empty
      }
    }
  }
}
