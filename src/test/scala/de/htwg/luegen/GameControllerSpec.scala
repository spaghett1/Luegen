package de.htwg.luegen.controller.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.GameModel
import de.htwg.luegen.model.fileIO.xml.FileIO
import de.htwg.luegen.TurnState
import de.htwg.luegen.controller.Observer

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

    "provide information about the current and previous players" in {
      val controller = new GameController(using model, fileIo)
      controller.handlePlayerCount(2)
      controller.handlePlayerNames(List("Alice", "Bob"))

      val currPlayer = controller.getCurrentPlayer
      val prevPlayer = controller.getPrevPlayer

      currPlayer.name should not be prevPlayer.name
      List("Alice", "Bob") should contain (prevPlayer.name) // Bei 2 Spielern ist der Letzte der Vorherige
    }

    "handle errors by updating the model state" in {
      val controller = new GameController(using model, fileIo)
      val testError = new Exception("Ungültiger Zug")

      controller.handleError(testError)
      controller.getInputError shouldBe Some("Ungültiger Zug")
    }
  }
}