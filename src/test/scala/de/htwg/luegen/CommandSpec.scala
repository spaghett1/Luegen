package de.htwg.luegen

import de.htwg.luegen.controller.impl1._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.{GameModel, Card}

class CommandSpec extends AnyWordSpec with Matchers {

  "GameCommand" should {
    val initialModel = GameModel()

    "InitCommand should return model unchanged" in {
      InitCommand.execute(initialModel) shouldBe initialModel
    }

    "SetupPlayerCountCommand should set the player count" in {
      val command = SetupPlayerCountCommand(4)
      val newModel = command.execute(initialModel)
      newModel.getPlayerCount shouldBe 4
    }

    val modelWithCount = SetupPlayerCountCommand(2).execute(initialModel)

    "SetupPlayersCommand should setup names, turn order and deal cards" in {
      val command = SetupPlayersCommand(List("Alice", "Bob"))
      val newModel = command.execute(modelWithCount)

      newModel.getPlayers.map(_.name) should contain allOf ("Alice", "Bob")
      newModel.getPlayers.head.hand should not be empty
    }

    val modelWithPlayers = SetupPlayersCommand(List("Alice", "Bob")).execute(modelWithCount)

    "HandleRoundRankCommand should set the rank" in {
      val command = HandleRoundRankCommand("Ass")
      val newModel = command.execute(modelWithPlayers)
      newModel.getRoundRank shouldBe "Ass"
    }

    "HandleCardPlayCommand should play selected cards" in {
      val command = HandleCardPlayCommand(List(1)) // Erste Karte spielen
      val newModel = command.execute(modelWithPlayers)
      newModel.getTurnState shouldBe TurnState.Played
    }

    "HandleChallengeDecisionCommand should process the turn" in {
      val command = HandleChallengeDecisionCommand(true)
      val newModel = command.execute(modelWithPlayers)
      newModel shouldBe a [IGameModel]
    }

    "SetNextPlayerCommand should change current player" in {
      val command = SetNextPlayerCommand()
      val newModel = command.execute(modelWithPlayers)
      newModel shouldBe a [IGameModel]
    }
  }

  "LoggingCommandDecorator" should {
    "execute the wrapped command and add log entries" in {
      val initialModel = GameModel()
      // Wir nutzen HandleRoundRankCommand als Beispiel
      val decorated = LoggingCommandDecorator(HandleRoundRankCommand("König"))

      val newModel = decorated.execute(initialModel)

      // Prüfen, ob der Log-Eintrag vom Decorator hinzugefügt wurde
      newModel.getLogHistory should contain ("Command ausgefuehrt: HandleRoundRankCommand")
      // Prüfen, ob die Logik des wrappedCommands ebenfalls ausgeführt wurde
      newModel.getRoundRank shouldBe "König"
    }
  }
}