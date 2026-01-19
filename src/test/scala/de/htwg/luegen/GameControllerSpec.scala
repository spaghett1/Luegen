package de.htwg.luegen.controller.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.{Card, GameModel, Player}
import de.htwg.luegen.model.fileIO.xml.FileIO
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.{GameOver, NeedsCardInput, NeedsChallengeDecision}
import de.htwg.luegen.controller.Observer
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.PlayerType.{AI, Human}
import org.scalatest.matchers.should.Matchers.contain.oneOf
import org.scalatest.matchers.should.Matchers.oneOf

class GameControllerSpec extends AnyWordSpec with Matchers {

  // Hilfsklasse zum Testen der Benachrichtigungen
  class TestObserver extends Observer {
    var updated = false
    override def updateDisplay(): Unit = updated = true
  }

  "A GameController" should {
    // Grundaufbau für die Tests
    val model = GameModel()
    val fileIo = new FileIO

    "notify observers when the state changes" in {
      val controller = new GameController(using model, fileIo)
      val observer = new TestObserver
      controller.registerObserver(observer)

      controller.handlePlayerCount(3)
      observer.updated shouldBe true
      controller.getTurnState shouldBe TurnState.NeedsPlayerNames
    }

    "correctly manage player setup and game start" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("Alice", "Bob"))

      controller.getPlayerCount shouldBe 2
      controller.getCurrentPlayers.map(_.name) should contain allOf ("Alice", "Bob")
      controller.getTurnState shouldBe TurnState.NeedsRankInput
    }

    "handle round rank and card input" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("P1", "P2"))

      controller.handleRoundRank("Ass")
      controller.getRoundRank shouldBe "Ass"
      controller.getTurnState shouldBe TurnState.NeedsCardInput

      // Eine Karte spielen (Index 1)
      controller.handleCardInput(List(1))
      controller.getTurnState shouldBe TurnState.Played
    }

    "support undo and redo of commands" in {
      val controller = new GameController(using model, fileIo)

      controller.undo() shouldBe theSameInstanceAs (model)
      controller.redo() shouldBe theSameInstanceAs (model)
      controller.handlePlayerCount(2) // Erster Command
      controller.getTurnState shouldBe TurnState.NeedsPlayerNames

      controller.undo()
      controller.getTurnState shouldBe TurnState.NeedsPlayerCount

      controller.redo()
      controller.getTurnState shouldBe TurnState.NeedsPlayerNames
    }

    "delegate save and load to FileIO" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(4)

      // Testet ob save/load ohne Absturz durchlaufen
      controller.save
      controller.handlePlayerCount(2) // Zustand ändern
      controller.load

      // Nach dem Laden sollte wieder der alte Zustand da sein
      controller.getPlayerCount shouldBe 4
    }

    "init the Game" in {
      val controller = new GameController(using model, fileIo)
      controller.initGame() shouldBe theSameInstanceAs (model)
    }

    "provide information about the current and previous players" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("Alice", "Bob"))

      val currPlayer = controller.getCurrentPlayer
      val prevPlayer = controller.getPrevPlayer

      currPlayer.name should not be prevPlayer.name
      List("Alice", "Bob") should contain (prevPlayer.name) // Bei 2 Spielern ist der Letzte der Vorherige
    }

    "handle a ChallengeDecision" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("Alice", "Bob"))
      controller.handleChallengeDecision(false)
      controller.getTurnState shouldBe NeedsCardInput
    }

    "handle setting of next player" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("Alice", "Bob"))
      controller.handleRoundRank("A")
      controller.handleCardInput(List(1,2,3))

      controller.setNextPlayer()
      controller.getTurnState should (equal(NeedsChallengeDecision) or equal(GameOver))
    }

    "handle errors by updating the model state" in {
      val controller = new GameController(using model, fileIo)
      val testError = new Exception("Ungültiger Zug")

      controller.handleError(testError)
      controller.getInputError shouldBe Some("Ungültiger Zug")

      val numberError = new NumberFormatException("test")
      controller.handleError(numberError)
      controller.getInputError shouldBe Some("Ungueltige Eingabe! Bitte geben sie Zahlen ein!")
    }

    "get all infos from model" in {
      val model = GameModel()
      val controller = new GameController(using model, fileIo)

      val testPlayer1 = Player("Alice", List(Card("1", "2"), Card("3", "4")), Human)
      val testPlayer2 = Player("Bob", List(Card("5", "6"), Card("7", "8")), AI)

      val testModel = model.copy(
        discardedCards = List(Card("B", "A"), Card("10", "9")),
        players = List(testPlayer1, testPlayer2)
      )

      controller.setModel(testModel)


      controller.getDiscardedCount shouldBe 2
      controller.getCurrentPlayerType shouldBe Human
      controller.getIsFirstTurn shouldBe true
      controller.isValidRanks shouldBe List("2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A")
      controller.getPlayedCards shouldBe Nil
      controller.getLog shouldBe Nil
      controller.getAllDiscardedQuartets shouldBe Nil
    }
  }
}