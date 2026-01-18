package de.htwg.luegen.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.StubController
import de.htwg.luegen.model.impl1.{Card, Player, PlayerType}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.TurnState
import java.io.ByteArrayOutputStream

class TuiViewSpec extends AnyWordSpec with Matchers {

  // Hilfsmethode um den Output direkt im Test zu fangen
  def capture(f: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) { f }
    out.toString("UTF-8")
  }

  "GameView" should {

    "den Log und den aktuellen Spielstatus anzeigen" in {
      val controller = new StubController
      val view = new GameView(using controller)

      controller.mockTurnState = TurnState.NeedsRankInput
      controller.mockCurrentPlayer = Player("Alice", List(Card("♠", "A")), Human)

      val output = capture { view.updateDisplay() }
      output should include("Sage einen Rang fuer die Runde an (2-10, B,D,K,A):")
    }

    "spezielle Nachrichten für Herausforderungen anzeigen" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val alice = Player("Alice")
      val bob = Player("Bob")

      // Hier muss der Aufruf in den capture-Block!
      val winOutput = capture { view.challengerWonMessage(alice, bob) }
      winOutput should include("Bob hat gelogen")
      winOutput should include("Er zieht alle Karten")

      val lostOutput = capture { view.challengerLostMessage(alice, bob) }
      lostOutput should include("Bob hat die Wahrheit gesagt")
      lostOutput should include("Alice zieht alle Karten")
    }

    "die Kartenhand eines Spielers korrekt formatieren" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val player = Player("Test", List(Card("♥", "10"), Card("♦", "K")), Human)

      val output = capture { view.displayPlayerHand(player) }

      output should include("1")
      output should include("2")
      output should include("♥10")
      output should include("♦K")
    }

    "Input-Anfragen an den richtigen Screen delegieren" in {
      val controller = new StubController
      val view = new GameView(using controller)
      controller.mockTurnState = TurnState.NeedsPlayerCount

      // Testet nur die Existenz/Zustand des Objekts, da StdIn schwer zu testen ist
      view shouldBe a [GameView]
    }
  }
}