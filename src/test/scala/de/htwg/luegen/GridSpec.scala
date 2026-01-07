
package de.htwg.luegen

import de.htwg.luegen.model.impl1.Player
import de.htwg.luegen.view.Grid
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
  val players5 = pList.take(5)
  val players6 = pList.take(6)
  val players7 = pList.take(7)


  "A Grid (Functional Case Class)" when {

    "updateGridWithPlayers is called" should {
      val initialGrid = Grid()

      "die Spieler korrekt für 4 Spieler platzieren und ein NEUES Grid zurückgeben" in {
        val newGrid = initialGrid.updateGridWithPlayers(players4)

        // Prüfen der Immutability
        newGrid should not be initialGrid
        newGrid.lastPlayers should contain theSameElementsInOrderAs players4

        // Prüfen der Zeichnung
        newGrid.text(0).trim should include ("P1")
        newGrid.text(5) should include ("| P4")
      }

      "bei gleicher Spielerliste die GLEICHE Instanz zurückgeben (Effizienztest)" in {
        // Zuerst initialisieren, um lastPlayers zu setzen
        val firstGrid = initialGrid.updateGridWithPlayers(players4)

        // Nochmal mit der gleichen Liste aufrufen
        val secondGrid = firstGrid.updateGridWithPlayers(players4)

        // Prüfen der Effizienz
        secondGrid should be theSameInstanceAs firstGrid
      }

      "bei geänderter Spielerliste eine NEUE Instanz zurückgeben" in {
        val firstGrid = initialGrid.updateGridWithPlayers(players4)
        val secondGrid = firstGrid.updateGridWithPlayers(players5)

        secondGrid should not be theSameInstanceAs(firstGrid)
        secondGrid.lastPlayers should contain theSameElementsInOrderAs players5
      }

      "die korrekte Struktur für 5 Spieler platzieren" in {
        val newGrid = initialGrid.updateGridWithPlayers(players5)
        newGrid.text(0) should include ("P5")
        newGrid.text(10) should not include ("P6")
      }

      "die korrekte Struktur für 6 Spieler platzieren" in {
        val newGrid = initialGrid.updateGridWithPlayers(players6)
        newGrid.text(10) should include ("P6")
        newGrid.text(7) should include ("| ") // Kein P7/P8
      }

      "die korrekte Struktur für 7 Spieler platzieren" in {
        val newGrid = initialGrid.updateGridWithPlayers(players7)
        newGrid.text(7) should include ("P7")
        newGrid.text(7) should not include ("P8")
        newGrid.text(3) should not include ("P4")
      }

      "die Spieler korrekt für 8 Spieler platzieren" in {
        val newGrid = initialGrid.updateGridWithPlayers(players8)
        newGrid.text(7) should include ("P7")
        newGrid.text(7) should include ("P8")
      }

      "Spielernamen mit ungerader Länge korrekt padden" in {
        val players = List(Player("Odd"), Player("Even"))
        val newGrid = initialGrid.updateGridWithPlayers(players)

        newGrid.text(0) should include (" Odd")
      }
    }

    "updateGridWithNumber is called" should {

      // Zuerst eine Grid-Instanz mit Spielerdaten erstellen
      val grid4Players = Grid().updateGridWithPlayers(players4)

      "die Kartenanzahl korrekt im Center platzieren (einstellig) und String zurückgeben" in {
        val output = grid4Players.updateGridWithNumber(5)

        output should include ("|  5 |")
        output should not include ("| 0 |")
      }

      "die Kartenanzahl korrekt im Center platzieren (zweistellig) und String zurückgeben" in {
        val output = grid4Players.updateGridWithNumber(10)

        output should include ("| 10 |")
      }
    }
  }
}