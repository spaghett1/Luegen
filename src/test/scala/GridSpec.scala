package de.htwg.luegen.Test

import de.htwg.luegen.Model.Player
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, PrintStream}
import de.htwg.luegen.View.Grid

import scala.Console.withOut

class GridSpec extends AnyWordSpec with Matchers {

  // Die I/O Capture-Methode wird nur für clearScreen benötigt
  def captureOutput(code: => Unit): String = {
    val baos = new ByteArrayOutputStream()
    // Wichtig: Auto-Flush auf false, um die Kontrolle über das Leeren zu behalten
    val ps = new PrintStream(baos, false, "UTF-8")
    val oldOut = System.out

    // 1. Manuelle Java-Umleitung (für System.out.print/println)
    System.setOut(ps)

    try {
      // 2. Scala-spezifische Umleitung (für print/println)
      withOut(ps) {
        code // Führt den Code aus (z.B. view.displayPlayerHand(player))
      }

      // 3. Explizites Leeren ALLER Puffer erzwingen
      ps.flush()

      // 4. Den String aus dem Puffer lesen
      baos.toString("UTF-8").trim
    } finally {
      // Original-Stream immer wiederherstellen!
      System.setOut(oldOut)
      // Der ursprüngliche scala.Console.out wird automatisch wiederhergestellt
      // wenn withOut beendet wird, daher muss hier nichts weiter getan werden.
    }
  }

  val pList = (1 to 8).map(i => Player(s"P$i")).toList
  val players4 = pList.take(4)
  val players8 = pList.take(8)

  "A Grid" when {

    "initGrid is called" should {

      "Spielernamen mit ungerader Länge korrekt padden" in {
        // Spieler "Odd" hat ungerade Länge und wird gepaddet
        val players = List(Player("Odd"), Player("Even"))
        val grid = new Grid()
        grid.initGrid(players)

        // Direkter Zugriff auf das Array `text`
        grid.text(0) should include (" Odd")
      }

      "die Spieler korrekt für 4 Spieler platzieren" in {
        val grid = new Grid()
        grid.initGrid(players4)

        // P1 und P4 (rechts) prüfen
        grid.text(0).trim should include ("P1")
        grid.text(5) should include ("| P4")
      }

      "die Spieler korrekt für 8 Spieler platzieren" in {
        val grid = new Grid()
        grid.initGrid(players8)

        // P1 und P5 sind auf Zeile 0
        grid.text(0) should include ("P1")
        grid.text(0) should include ("P5")
        // P7 und P8 sind auf Zeile 7
        grid.text(7) should include ("P7")
        grid.text(7) should include ("P8")
      }
    }

    "printGrid is called" should {

      "die Kartenanzahl korrekt im Center platzieren (einstellig)" in {
        val grid = new Grid()
        grid.initGrid(players4)

        grid.printGrid(5)

        // Prüfen des modifizierten Eintrags text(5)
        grid.text(5) should include ("|  5 |")
        grid.text(5) should not include ("| 0 |")
      }

      "die Kartenanzahl korrekt im Center platzieren (zweistellig)" in {
        val grid = new Grid()
        grid.initGrid(players4)

        grid.printGrid(10)

        // Prüfen des modifizierten Eintrags text(5)
        grid.text(5) should include ("| 10 |")
      }
    }
  }
}
