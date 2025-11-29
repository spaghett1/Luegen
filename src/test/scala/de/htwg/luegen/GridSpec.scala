package de.htwg.luegen

import de.htwg.luegen.Model.Player
import de.htwg.luegen.View.Grid
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.Console.withOut
import scala.util.Try

class GridSpec extends AnyWordSpec with Matchers {

  // Die I/O Capture-Methode wird nur für clearScreen benötigt
  def captureOutput(code: => Unit): String = {
    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos, false, "UTF-8")
    val oldOut = System.out

    System.setOut(ps)

    try {
      withOut(ps) {
        code
      }

      ps.flush()
      baos.toString("UTF-8")
    } finally {
      System.setOut(oldOut)
    }
  }

  val pList = (1 to 8).map(i => Player(s"P$i")).toList
  val players4 = pList.take(4)
  val players8 = pList.take(8)

  // NEU: Listen für die Grenzfälle 5, 6 und 7 zur Coverage-Erhöhung
  val players5 = pList.take(5)
  val players6 = pList.take(6)
  val players7 = pList.take(7)


  "A Grid" when {

    "initGrid is called" should {

      "Spielernamen mit ungerader Länge korrekt padden" in {
        val players = List(Player("Odd"), Player("Even"))
        val grid = new Grid()
        grid.initGrid(players)

        grid.text(0) should include (" Odd")
      }

      "die Spieler korrekt für 4 Spieler platzieren" in {
        val grid = new Grid()
        grid.initGrid(players4)

        grid.text(0).trim should include ("P1")
        grid.text(5) should include ("| P4")
      }

      // NEUER TEST: 5 Spieler (players.size > 4, but NOT > 5)
      "die korrekte Struktur für 5 Spieler platzieren (Testet if > 4, else von if > 5)" in {
        val grid = new Grid()
        grid.initGrid(players5) // P1, P2, P3, P4, P5

        // P5 (Index 4) muss auf Zeile 0 erscheinen (durch das äußere if > 4)
        grid.text(0) should include ("P5")

        // Zeile 10 muss die Standard-Platzierung für P2 zeigen (durch den impliziten else-Fall von if > 5)
        grid.text(10) should not include ("P6")
      }

      // NEUER TEST: 6 Spieler (players.size > 5, but NOT > 6)
      "die korrekte Struktur für 6 Spieler platzieren (Testet if > 5, else von if > 6)" in {
        val grid = new Grid()
        grid.initGrid(players6) // P1, P2, P3, P4, P5, P6

        // P6 (Index 5) muss auf Zeile 10 erscheinen (durch if > 5)
        grid.text(10) should include ("P6")

        // P7 (Index 6) darf NICHT auf Zeile 7 erscheinen (impliziter else-Fall von if > 6)
        grid.text(7) should not include ("P7")
        grid.text(7) should include ("| ")
      }

      // NEUER TEST: 7 Spieler (players.size > 6, but NOT > 7)
      "die korrekte Struktur für 7 Spieler platzieren (Testet if > 6, else von if > 7)" in {
        val grid = new Grid()
        grid.initGrid(players7) // P1 bis P7

        // P7 (Index 6) muss auf Zeile 7 platziert werden (durch if > 6)
        grid.text(7) should include ("P7")

        // P8 (Index 7) darf NICHT auf Zeile 7 erscheinen (impliziter else-Fall von if > 7)
        grid.text(7) should not include ("P8")

        // P3 (Index 2) muss alleine auf Zeile 3 stehen (durch if > 6)
        grid.text(3) should not include ("P4")
      }

      "die Spieler korrekt für 8 Spieler platzieren" in {
        val grid = new Grid()
        grid.initGrid(players8)

        grid.text(0) should include ("P1")
        grid.text(0) should include ("P5")
        grid.text(7) should include ("P7")
        grid.text(7) should include ("P8")
      }
    }

    "printGrid is called" should {

      "die Kartenanzahl korrekt im Center platzieren (einstellig)" in {
        val grid = new Grid()
        grid.initGrid(players4)

        grid.printGrid(5)

        grid.text(5) should include ("|  5 |")
        grid.text(5) should not include ("| 0 |")
      }

      "die Kartenanzahl korrekt im Center platzieren (zweistellig)" in {
        val grid = new Grid()
        grid.initGrid(players4)

        grid.printGrid(10)

        grid.text(5) should include ("| 10 |")
      }
    }

    "utility methods" should {
      "clearScreen sollte die korrekten ANSI-Escape-Sequenzen ausgeben" in {
        val grid = new Grid()

        val output = captureOutput {
          grid.clearScreen()
        }

        val expected = "\n" * 20

        output shouldBe expected
      }
    }
  }
}