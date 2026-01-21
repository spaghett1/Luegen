package de.htwg.luegen.model.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.{ChallengedLieLost, ChallengedLieWon}
import de.htwg.luegen.model.impl1.PlayerType.Human

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

    val player1 = Player("Alice", List(Card("A", "10"), Card("B", "K")))
    val player2 = Player("Bob", List(Card("C", "8"), Card("D", "9")))

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
      val setup = model.copy(players = List(player1, player2), currentPlayerIndex = 0, playOrder = List(0, 1))
        .setupRank("A")
        .playCards(List(1))

      // P2 deckt auf (Challenge = true)
      val updated = setup.setNextPlayer().playerTurn(true)
      // Der Zustand sollte sich zu ChallengedLieWon oder ChallengedLieLost ändern
      List(TurnState.ChallengedLieWon, TurnState.ChallengedLieLost) should contain (updated.getTurnState)
    }

    "process a challenge decision (Player lied)" in {
      val setup = model.copy(players = List(player1, player2), currentPlayerIndex = 0, playOrder = List(0, 1))
        .setupRank("A")
        .playCards(List(2))
      val updated = setup.setNextPlayer().playerTurn(true)
      updated.getTurnState shouldBe ChallengedLieWon
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

    "correctly decide winning player" in {
      val player = Player("Alice", List(), Human)
      val model = GameModel().copy(players = List(player))
      val newModel = model.setNextPlayer()
      newModel.getLastInputError shouldBe Some("GEWONNEN: Alice hat keine Karten mehr und gewonnen!")
    }

    "correctly decide losing player" in {
      val player = Player("Bob", List(Card("1", "D"), Card("2", "D"), Card("3", "D"), Card("4", "D")), Human)
      val model = GameModel().copy(players = List(player))
      val newModel = model.setNextPlayer()
      newModel.getLastInputError shouldBe Some("GAME OVER: Bob hat 4 Damen und verloren!")
    }

    val player = Player("Alice", List(Card("1", "B"), Card("2", "B"), Card("3", "B"), Card("4", "B"), Card("5", "D")))
    val testPlayer = Player("Bob", List(Card("A", "K"), Card("B", "D")))

    "correctly set next Player for empty playOrder" in {
      val model1 = GameModel().copy(playOrder = Nil, players = List(player, testPlayer), currentPlayerIndex = 0)
      val playerIndex = model1.getCurrentPlayerIndex
      val testModel = model1.setNextPlayer()
      testModel.getPlayers(playerIndex).name shouldBe player.name
      testModel.getPlayers(playerIndex).discardedQuartets should contain ("B")
    }

    "correctly set next Player for ChallengedLieWon case" in {
      val model = GameModel().copy(players = List(player, testPlayer), playOrder = List(0,1), turnState = ChallengedLieWon).setNextPlayer()
      model.getRoundRank shouldBe ""
      model.getPlayers(model.getCurrentPlayerIndex).name shouldBe player.name
    }

    "correctly set next player for ChallengedLieLost case" in {
      val model = GameModel().copy(playOrder = List(0, 1), players = List(player, testPlayer), turnState = ChallengedLieLost, currentPlayerIndex = 0)
      val currentPlayerIndex = model.currentPlayerIndex
      println(model.currentPlayerIndex)
      val testModel = model.setNextPlayer()
      testModel.getRoundRank shouldBe ""
      testModel.getCurrentPlayerIndex should not be currentPlayerIndex
    }
  }
}