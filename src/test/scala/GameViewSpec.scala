package de.htwg.luegen.View

import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{Card, Player}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import scala.Console.*

class GameViewSpec extends AnyWordSpec with Matchers with MockitoSugar {

  // Helfer-Methode zur Erfassung der Konsolenausgabe (System.out)
  def captureOutput(code: => Unit): String = {
    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos, true, "UTF-8")
    val oldOut = System.out

    // 1. System.out umleiten (für Code, der direkt System.out verwendet)
    System.setOut(ps)

    try {
      // 2. scala.Console.out umleiten (für print/println)
      withOut(ps) {
        code
      }

      // 3. Explizites Leeren der Streams nach der Ausführung
      ps.flush()

      // 4. Den String aus dem Puffer lesen
      baos.toString("UTF-8").trim
    } finally {
      // Original-Stream immer wiederherstellen!
      System.setOut(oldOut)
    }
  }

  // Mock des Controllers
  val mockController: GameController = mock[GameController]

  // Instanz der REALEN GameView (da wir das private Grid nicht ersetzen können)
  // Dies ist der Schlüssel, um den Fehler mit dem privaten Feld zu vermeiden.
  val view = new GameView(mockController)

  val testPlayer = Player("Alice", List(Card("S", "A"), Card("H", "10")))
  val testPrevPlayer = Player("Bob")

  private val originalIn = System.in
  private val originalOut = System.out

  def simulateInput[T](inputs: List[String])(testCode: => T): (T, String) = {
    // 1. INPUT: Stream mit den simulierten Eingaben erstellen
    val inputData = inputs.mkString(System.lineSeparator())
    val inputStream = new ByteArrayInputStream(inputData.getBytes("UTF-8"))

    // 2. OUTPUT: Stream zur Erfassung der Konsolenausgabe
    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, false, "UTF-8") // Auto-flush false

    // Streams auf Java-Ebene umleiten
    System.setIn(inputStream)
    System.setOut(printStream)

    try {
      var result: T = null.asInstanceOf[T]

      // Nutzt Scala's idiomatische withIn/withOut Umleitung
      withIn(inputStream) {
        withOut(printStream) {
          result = testCode
        }
      }

      printStream.flush()
      // .trim() entfernt unnötige Leerzeichen am Anfang/Ende
      (result, outputStream.toString("UTF-8").trim)
    } finally {
      // Streams wiederherstellen
      System.setIn(originalIn)
      System.setOut(originalOut)
    }
  }
  // ======================================================================================

  "A GameView" when {

    // ... [Bestehende Tests für displayPlayerHand, printLayedCards, updateDisplay, etc. hier] ...


    // ======================================================================================
    // NEUE TESTS FÜR INPUT-METHODEN (Direkte Korrekte Eingabe)
    // ======================================================================================
    "handling correct user input via retryUntilValid" should {

      "getNum sollte eine korrekte Spieleranzahl zurückgeben (4)" in {
        val (result, output) = simulateInput(List("4")) {
          view.getNum
        }
        result shouldBe 4
        output should include("Wieviele Spieler? (2-8)")
        output should not include "Ungueltige Eingabe!"
      }

      "getPlayerName sollte einen korrekten Namen zurückgeben ('Charlie')" in {
        val (result, output) = simulateInput(List("Charlie")) {
          view.getPlayerName(3)
        }
        result shouldBe "Charlie"
        output should include("Name von Spieler 3 (max. 10 Zeichen)")
      }

      "callRank sollte einen gültigen Rang zurückgeben ('A')" in {
        val validRanks = List("2", "A", "K")
        val (result, output) = simulateInput(List("A")) {
          view.callRank(validRanks)
        }
        result shouldBe "A"
        output should include("Gebe ein Symbol fuer die Runde ein")
      }

      "selectCards sollte gültige, kommaseparierte Indizes zurückgeben ('1,2')" in {
        // Spieler hat 3 Karten
        val (result, output) = simulateInput(List("1,2", "1,2")) {
          view.selectCards(testPlayer)
        }
        result shouldBe List(1, 2)
        output should include("Waehle bis zu drei Karten (durch Kommas getrennt)")
      }

      "readYesNo sollte true für 'j' zurückgeben" in {
        val (result, output) = simulateInput(List("j")) {
          view.readYesNo(testPrevPlayer)
        }
        result shouldBe true
        output should include(s"Luege von Spieler ${testPrevPlayer.name} aufdecken?")
      }

      "readYesNo sollte false für 'n' zurückgeben" in {
        val (result, output) = simulateInput(List("n")) {
          view.readYesNo(testPrevPlayer)
        }
        result shouldBe false
      }
    }
  }

  "Eine GameView" should {

    "displayPlayerHand" should {
      "Indizes und Karten korrekt formatieren und drucken" in {
        val player = Player("Test", List(Card("H", "10"), Card("S", "2"))) // Längen: 3, 2

        val output = captureOutput {
          view.displayPlayerHand(player)
        }
        println(s"Output: ${output}end")

        val expected = "1  2  \nH10S2"
        // Längste Karte ist H10 (Länge 3). Indizes und Karten werden auf 3 gepaddet.
        output shouldBe expected
      }
    }

    "die Meldungen korrekt drucken" should {

      "printLayedCards" in {
        val cards = List(Card("S", "K"))
        captureOutput {
          view.printLayedCards(testPlayer, cards)
        } shouldBe s"${testPlayer.name} legt ab: SK"
      }

      "startGamePrompt" in {
        captureOutput {
          view.startGamePrompt(testPlayer)
        } shouldBe s"Das Spiel startet mit ${testPlayer.name}!"
      }

      "printPrompt ohne Zeilenumbruch" in {
        captureOutput {
          view.printPrompt("Bitte eingeben:")
        } shouldBe "Bitte eingeben:"
      }
    }

    "die Nachrichten zum Aufdecken korrekt drucken" should {

      "challengerWonMessage" in {
        val output = captureOutput {
          view.challengerWonMessage(testPlayer, testPrevPlayer)
        }
        output should include ("Bob hat gelogen!")
        output should include ("Er zieht alle Karten.")
      }

      "challengerLostMessage" in {
        val output = captureOutput {
          view.challengerLostMessage(testPlayer, testPrevPlayer)
        }
        output should include ("Bob hat die Wahrheit gesagt!")
        output should include ("Alice zieht alle Karten!")
      }
    }

    "updateDisplay" should {
      // Da wir das private Grid nicht mocken können, prüfen wir nur die Interaktion mit dem Controller
      "die notwendigen Daten vom Controller abrufen" in {
        when(mockController.getCurrentPlayer).thenReturn(testPlayer)
        when(mockController.getDiscardedCount).thenReturn(0)

        // Führe updateDisplay aus und ignoriere die Ausgabe des echten Grids
        captureOutput {
          view.updateDisplay()
        }

        // Verifiziere, dass die Methoden aufgerufen wurden
        verify(mockController).getCurrentPlayer
        verify(mockController).getDiscardedCount
      }
    }

    "initGrid" should {
      // Wir können hier nur prüfen, dass die Methode ohne Fehler aufgerufen wird,
      // da die Interaktion mit dem privaten 'grid' nicht verifizierbar ist.
      "keine Exception werfen" in {
        noException should be thrownBy view.initGrid(List(testPlayer))
      }
    }
  }
}
