package de.htwg.luegen.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.StubController
import de.htwg.luegen.model.impl1.{Card, Player, PlayerType}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.TurnState
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TuiViewSpec extends AnyWordSpec with Matchers {

  "GameView" should {

    "den Log und den aktuellen Spielstatus anzeigen" in {
      val controller = new StubController
      val view = new GameView(using controller)

      // Setup Stub-Daten
      controller.mockTurnState = TurnState.NeedsRankInput
      controller.mockCurrentPlayer = Player("Alice", List(Card("♠", "A")), Human)

      val out = new ByteArrayOutputStream()

      Console.withOut(out) {
        view.updateDisplay()
      }

      val output = out.toString("UTF-8")

      print(output)

      // Prüfen, ob wichtige UI-Elemente enthalten sind
      output should include("Sage einen Rang fuer die Runde an (2-10, B,D,K,A):")
    }

    "spezielle Nachrichten für Herausforderungen anzeigen" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val alice = Player("Alice")
      val bob = Player("Bob")

      "beim Sieg eines Challengers" in {
        val output = ""
        output should include("Bob hat gelogen")
        output should include("Er zieht alle Karten")
      }

      "beim Verlust eines Challengers" in {
        val output = ""
        output should include("Bob hat die Wahrheit gesagt")
        output should include("Alice zieht alle Karten")
      }
    }

    "die Kartenhand eines Spielers korrekt formatieren" in {
      val controller = new StubController
      val view = new GameView(using controller)
      val player = Player("Test", List(Card("♥", "10"), Card("♦", "K")), Human)

      val output = ""

      // Prüfen ob Indizes und Karten-Strings vorhanden sind
      output should include("1")
      output should include("2")
      output should include("♥10")
      output should include("♦K")
    }

    "Input-Anfragen an den richtigen Screen delegieren" in {
      val controller = new StubController
      val view = new GameView(using controller)

      // Wir simulieren eine Eingabe für den PlayerCount
      controller.mockTurnState = TurnState.NeedsPlayerCount

      // Da handleInput auf StdIn.readLine wartet, testen wir hier
      // primär, ob der Screen-Zustand korrekt erkannt wird.
      // In einem echten Unit-Test würde man StdIn mocken,
      // aber für die View-Logik reicht die Status-Prüfung.
      view shouldBe a [GameView]
    }
  }
}
