package de.htwg.luegen

import de.htwg.luegen.model.impl1.{Card, Player}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.view._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.util.{Failure, Success}

class GameScreenSpec extends AnyWordSpec with Matchers {

  "GameScreenSpec mit StubController" should {

    "globale Befehle in handleGlobalCommand korrekt verarbeiten" in {
      val controller = new StubController
      val screen = NeedsPlayerCountScreen // Stellvertretend für alle Screens

      screen.handleGlobalCommand(using controller)("undo") shouldBe true
      controller.lastCalledMethod shouldBe "undo"

      screen.handleGlobalCommand(using controller)("redo") shouldBe true
      controller.lastCalledMethod shouldBe "redo"

      screen.handleGlobalCommand(using controller)("save") shouldBe true
      controller.lastCalledMethod shouldBe "save"

      screen.handleGlobalCommand(using controller)("load") shouldBe true
      controller.lastCalledMethod shouldBe "load"

      screen.handleGlobalCommand(using controller)("egal") shouldBe false
    }

    "NeedsPlayerCountScreen" should {
      "Eingaben validieren" in {
        NeedsPlayerCountScreen.validateInput("4") shouldBe Success(4)
        NeedsPlayerCountScreen.validateInput("1").isFailure shouldBe true
        NeedsPlayerCountScreen.validateInput("abc").isFailure shouldBe true
      }

      "display aufrufen ohne abzustürzen" in {
        val controller = new StubController
        controller.mockInputError = Some("Fehler")
        noException should be thrownBy NeedsPlayerCountScreen.display(using controller)
      }

      "den Controller bei korrekter Eingabe aufrufen" in {
        val controller = new StubController
        NeedsPlayerCountScreen.processInput("4")(using controller)
        controller.lastPlayerCount shouldBe 4
      }

      "handleError bei falscher Eingabe aufrufen" in {
        val controller = new StubController
        NeedsPlayerCountScreen.processInput("9")(using controller)
        controller.lastCalledMethod shouldBe "handleError"
      }
    }

    "NeedsPlayerNamesScreen" should {
      "Namen korrekt validieren" in {
        NeedsPlayerNamesScreen.validateInput("Alice, Bob", 2) shouldBe Success(List("Alice", "Bob"))
        NeedsPlayerNamesScreen.validateInput("Alice", 2).isFailure shouldBe true
      }

      "Namen an den Controller weitergeben" in {
        val controller = new StubController
        controller.mockPlayerCount = 2
        NeedsPlayerNamesScreen.processInput("Alice, Bob")(using controller)
        controller.lastPlayerNames shouldBe List("Alice", "Bob")
      }
    }

    "NeedsRankInputScreen" should {
      "Rang-Eingaben validieren" in {
        val ranks = List("10", "A")
        NeedsRankInputScreen.validateInput("10", ranks) shouldBe Success("10")
        NeedsRankInputScreen.validateInput("K", ranks).isFailure shouldBe true
      }

      "KI-Spieler automatisch behandeln" in {
        val controller = new StubController
        controller.mockCurrentPlayerType = AI
        controller.mockValidRanks = List("Ass")

        NeedsRankInputScreen.processInput("irrelevant")(using controller)
        controller.lastRoundRank shouldBe "Ass"
      }
    }

    "NeedsCardInputScreen" should {
      "Karten-Indices validieren" in {
        NeedsCardInputScreen.validateInput("1, 2", 5) shouldBe Success(List(1, 2))
        NeedsCardInputScreen.validateInput("1, 2, 3, 4", 5).isFailure shouldBe true // Max 3 Karten
        NeedsCardInputScreen.validateInput("6", 5).isFailure shouldBe true // Index zu hoch
      }

      "Karten-Play an Controller senden" in {
        val controller = new StubController
        controller.mockCurrentPlayer = Player("Alice", List(Card("S", "A"), Card("H", "10")))

        NeedsCardInputScreen.processInput("1, 2")(using controller)
        controller.lastCardSelection shouldBe List(1, 2)
      }
    }

    "NeedsChallengeDecisionScreen" should {
      "j/n validieren" in {
        NeedsChallengeDecisionScreen.validateInput("j") shouldBe Success(true)
        NeedsChallengeDecisionScreen.validateInput("n") shouldBe Success(false)
      }

      "PrevPlayer im Display anzeigen" in {
        val controller = new StubController
        controller.mockPrevPlayer = Player("Lügner")
        noException should be thrownBy NeedsChallengeDecisionScreen.display(using controller)
      }

      "Entscheidung an Controller senden" in {
        val controller = new StubController
        NeedsChallengeDecisionScreen.processInput("j")(using controller)
        controller.lastChallengeDecision shouldBe Some(true)
      }
    }

    "Ergebnis-Screens (Played, Won, Lost)" should {
      "bei jeder Eingabe zum nächsten Spieler springen" in {
        val controller = new StubController

        PlayedScreen.processInput("")(using controller)
        controller.lastCalledMethod shouldBe "setNextPlayer"

        ChallengedLieWonScreen.processInput("ok")(using controller)
        controller.lastCalledMethod shouldBe "setNextPlayer"
      }

      "display ohne Fehler ausführen" in {
        val controller = new StubController
        noException should be thrownBy {
          PlayedScreen.display(using controller)
          ChallengedLieWonScreen.display(using controller)
        }
      }
    }
  }
}