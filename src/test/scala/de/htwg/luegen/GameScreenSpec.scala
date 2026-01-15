package de.htwg.luegen

import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.model.impl1.{AI, Card, GameModel, Player, Human}
import de.htwg.luegen.view._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalamock.scalatest.MockFactory // Empfohlen für saubere Methoden-Tests

class GameScreenSpec extends AnyWordSpec with Matchers with MockFactory {

  "GameScreen Objects" should {

    "NeedsPlayerCountScreen" should {
      "validate correct input" in {
        NeedsPlayerCountScreen.validateInput("4").isSuccess shouldBe true
        NeedsPlayerCountScreen.validateInput("9").isFailure shouldBe true
      }
      "call handlePlayerCount on valid input" in {
        val mockController = mock[IGameController]
        (mockController.handlePlayerCount _).expects(4)
        NeedsPlayerCountScreen.processInput("4")(using mockController)
      }
    }

    "NeedsPlayerNamesScreen" should {
      "call handlePlayerNames with correct list" in {
        val mockController = mock[IGameController]
        (() => mockController.getPlayerCount).expects().returns(2)
        (mockController.handlePlayerNames _).expects(List("Alice", "Bob"))

        NeedsPlayerNamesScreen.processInput("Alice, Bob")(using mockController)
      }
    }

    "NeedsRankInputScreen" should {
      "handle Human input" in {
        val mockController = mock[IGameController]
        (() => mockController.getCurrentPlayerType).expects().returns(Human)
        (() => mockController.isValidRanks).expects().returns(List("10", "A"))
        (mockController.handleRoundRank _).expects("10")

        NeedsRankInputScreen.processInput("10")(using mockController)
      }
      "handle AI input automatically" in {
        val mockController = mock[IGameController]
        (() => mockController.getCurrentPlayerType).expects().returns(AI)
        (() => mockController.isValidRanks).expects().twice().returns(List("A"))
        (mockController.handleRoundRank _).expects("A")

        NeedsRankInputScreen.processInput("")(using mockController)
      }
    }

    "NeedsCardInputScreen" should {
      "call handleCardInput with parsed indices" in {
        val mockController = mock[IGameController]
        val testPlayer = Player("Test", List(Card("H", "10"), Card("H", "A")))

        (() => mockController.getCurrentPlayer).expects().returns(testPlayer)
        (() => mockController.getCurrentPlayerType).expects().returns(Human)
        (mockController.handleCardInput _).expects(List(1, 2))

        NeedsCardInputScreen.processInput("1, 2")(using mockController)
      }
    }

    "NeedsChallengeDecisionScreen" should {
      "call handleChallengeDecision" in {
        val mockController = mock[IGameController]
        (() => mockController.getCurrentPlayer).expects().returning(Player("Test"))
        (() => mockController.getCurrentPlayerType).expects().returns(Human)
        (mockController.handleChallengeDecision _).expects(true)

        NeedsChallengeDecisionScreen.processInput("j")(using mockController)
      }
    }

    "Global Commands" should {
      "trigger save and load" in {
        val mockController = mock[IGameController]
        (() => mockController.save).expects().once()
        (() => mockController.load).expects().once()

        NeedsPlayerCountScreen.processInput("save")(using mockController)
        NeedsPlayerCountScreen.processInput("load")(using mockController)
      }
      "trigger undo and redo" in {
        val mockController = mock[IGameController]
        (mockController.undo _).expects().once()
        (mockController.redo _).expects().once()

        NeedsPlayerCountScreen.processInput("undo")(using mockController)
        NeedsPlayerCountScreen.processInput("redo")(using mockController)
      }
    }
  }
}