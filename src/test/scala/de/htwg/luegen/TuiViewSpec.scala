package de.htwg.luegen.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.StubController
import de.htwg.luegen.model.impl1.{Card, Player, PlayerType}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.TurnState
import java.io.ByteArrayOutputStream

class TuiViewSpec extends AnyWordSpec with Matchers {

  // Hilfsmethode um Konsolenausgaben zu fangen
  def capture(f: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) { f }
    out.toString("UTF-8")
  }

  "GameView (TUI)" should {

    val alice = Player("Alice", List(Card("H", "10"), Card("KD", "10")))
    val bob = Player("Bob", List(Card("C", "10"), Card("S", "10")))

    "das Grid und den aktuellen Rang korrekt anzeigen (Screenshot Zeile 45-49)" in {
      val controller = new StubController
      val view = new GameView(using controller)

      // Setup: Keine Runden-Rang gesetzt (Testet Zeile 49: "Keiner")
      controller.mockTurnState = TurnState.NeedsRankInput
      controller.mockCurrentPlayer = Player("Alice", List(Card("♠", "A")), Human)
      controller.currentPlayers = List(bob, alice)

      val outputNoRank = capture { view.updateDisplay() }
      outputNoRank should include("Sage einen Rang fuer die Runde an (2-10, B,D,K,A):")
    }

    "init the grid correctly" in {
      val controller = new StubController
      val view = new GameView(using controller)
      noException should be thrownBy {
        view.initGrid(List(alice, bob))
      }
    }

    "Input-Anfragen basierend auf dem State delegieren (Screenshot Zeile 55-61)" in {
      val controller = new StubController
      val view = new GameView(using controller)

      // Testet die Delegation (Zeile 60)
      controller.mockTurnState = TurnState.NeedsPlayerCount

      // Da wir StdIn.readLine nicht blockieren wollen, prüfen wir die
      // zugrunde liegende Screen-Logik, die von handleInput aufgerufen wird
      noException should be thrownBy {
        // Simuliert den Aufruf von .processInput(input) aus Zeile 60
        NeedsPlayerCountScreen.processInput("4")(using controller)
      }
      controller.lastPlayerCount shouldBe 4
    }

    "die Kartenhand formatiert ausgeben (Screenshot Zeile 48)" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val player = Player("Tester", List(Card("♥", "10")), Human)

      val output = capture { view.displayPlayerHand(player) }
      output should include("1")
      output should include("♥10")
    }

    "Hilfsmethoden wie printPrompt korrekt ausführen (Screenshot Zeile 68)" in {
      val controller = new StubController
      val view = new GameView(using controller)

      val output = capture { view.printPrompt("Eingabe: ") }
      output should be("Eingabe: ")
    }

    "printLayedCards korrekt ausfuehren" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val player = Player("test")
      val output = capture {view.printLayedCards(player, List(Card("B", "10"))) }
      output should include ("test legt ab: B10")
    }

    "startGamePrompt korrekt ausfuehren" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val player = Player("test")
      val output = capture {
        view.startGamePrompt(player)
      }
      output should include(s"Das Spiel startet mit test")
    }

    "correctly exec challengerWonMessage" in {
      val controller = new StubController
      val view = new GameView(using controller)

      val output = capture {
        view.challengerWonMessage(bob, alice)
      }

      output should include ("Alice hat gelogen!")
      output should include ("Er zieht alle Karten.")
    }

    "correctly exec challengerLostMessage" in {
      val controller = new StubController
      val view = new GameView(using controller)

      val output = capture {
        view.challengerLostMessage(bob, alice)
      }

      output should include("Alice hat die Wahrheit gesagt!")
      output should include("Bob zieht alle Karten!")
    }
  }
}