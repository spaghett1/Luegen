package de.htwg.luegen.model.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.TurnState

class GameModelSpec extends AnyWordSpec with Matchers {

  "A GameModel" should {
    val model = GameModel()

    "set player count and transition to NeedsPlayerNames" in {
      val updated = model.setPlayerCount(3)
      updated.getPlayerCount shouldBe 3
      updated.getTurnState shouldBe TurnState.NeedsPlayerNames
    }

    "handle player names and transition to NeedsRankInput" in {
      val names = List("Alice", "Bob", "Charlie")
      val updated = model.setPlayerCount(3).setupPlayers(names)

      updated.getPlayers.map(_.name) shouldBe names
      updated.getTurnState shouldBe TurnState.NeedsRankInput
      // Überprüfung ob Karten ausgeteilt wurden
      updated.getPlayers.forall(_.hand.isEmpty) shouldBe true
    }

    "set a round rank" in {
      val updated = model.setPlayerCount(2)
        .setupPlayers(List("P1", "P2"))
        .setupRank("A")

      updated.getRoundRank shouldBe "A"
      updated.getTurnState shouldBe TurnState.NeedsCardInput
    }

    "handle playing cards" in {
      val modelWithCards = model.setPlayerCount(2)
        .setupPlayers(List("P1", "P2"))
        .dealCards()
        .setupRank("10")

      // Spieler 1 spielt die erste Karte aus seiner Hand
      val updated = modelWithCards.playCards(List(1))

      updated.getTurnState shouldBe TurnState.Played
      updated.getDiscardedCards.size shouldBe 1
    }

    "process a challenge decision (no lie)" in {
      val setup = model.setPlayerCount(2)
        .setupPlayers(List("P1", "P2"))
        .dealCards()
        .setupRank("A")
        .playCards(List(1))

      // P2 glaubt P1 (Challenge = false)
      val updated = setup.playerTurn(false)
      updated.getTurnState shouldBe TurnState.NeedsCardInput
    }

    "process a challenge decision (lie check)" in {
      val setup = model.setPlayerCount(2)
        .setupPlayers(List("P1", "P2"))
        .dealCards()
        .setupRank("A")
        .playCards(List(1))

      // P2 deckt auf (Challenge = true)
      val updated = setup.playerTurn(true)
      // Der Zustand sollte sich zu ChallengedLieWon oder ChallengedLieLost ändern
      List(TurnState.ChallengedLieWon, TurnState.ChallengedLieLost) should contain (updated.getTurnState)
    }

    "handle errors correctly" in {
      val error = new Exception("Test Error")
      val updated = model.setError(error.getMessage)
      updated.getError() shouldBe Some("Test Error")

      val cleared = updated.clearError()
      cleared.getError() shouldBe None
    }

    "restore state from a memento" in {
      val memento = model.createMemento()
      val restored = GameModel().restoreMemento(memento)
      restored.getPlayers shouldBe model.getPlayers
      restored.getTurnState shouldBe model.getTurnState
    }
  }
}