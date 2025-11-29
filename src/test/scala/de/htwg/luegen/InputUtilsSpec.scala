package de.htwg.luegen

import de.htwg.luegen.View.InputUtils.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import scala.Console.{withIn, withOut}
import scala.io.StdIn
import scala.util.Try // Wichtige Imports

class InputUtilsSpec extends AnyWordSpec with Matchers {

  // Speichert die ursprünglichen System-Streams zur Wiederherstellung nach dem Test
  private val originalIn = System.in
  private val originalOut = System.out

  /**
   * HILFSFUNKTION: Simuliert Benutzereingaben und erfasst Konsolenausgabe
   * unter Verwendung der robusten Doppel-Umleitung (System + Console).
   */
  def simulateInput[T](inputs: List[String])(testCode: => T): (T, String) = {
    // 1. INPUT: Erstellt einen Stream mit den simulierten Eingaben
    val inputData = inputs.mkString(System.lineSeparator())
    val inputStream = new ByteArrayInputStream(inputData.getBytes("UTF-8"))

    // 2. OUTPUT: Erstellt einen Stream zur Erfassung der Konsolenausgabe
    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, true, "UTF-8")

    // 3. Streams umleiten (System.in ist entscheidend für StdIn.readLine)
    System.setIn(inputStream)
    System.setOut(printStream)

    try {
      var result: T = null.asInstanceOf[T] // Initialisierung

      // Nutzt Scala's idiomatische withIn/withOut Umleitung
      withIn(inputStream) {
        withOut(printStream) {
          result = testCode
        }
      }

      printStream.flush()
      (result, outputStream.toString("UTF-8"))
    } finally {
      // 4. Streams wiederherstellen
      System.setIn(originalIn)
      System.setOut(originalOut)
    }
  }

  // --- Parser und Validator für die Tests ---
  val intParser: String => Option[Int] = str => Try(str.trim.toInt).toOption
  val rangeValidator: Int => Boolean = n => n >= 1 && n <= 10
  val prompt = "Zahl eingeben:"

  "InputUtils.retryUntilValid" should {

    "den Wert sofort bei erster gültiger Eingabe zurückgeben" in {
      val (result, output) = simulateInput(List("5")) {
        retryUntilValid(prompt, intParser, rangeValidator)
      }
      result shouldBe 5
      // Prüft, ob der Prompt nur 1x gesendet wurde (keine Wiederholung)
      output.split('\n').count(_ contains prompt) shouldBe 1
    }

    "eine ungültige Eingabe (Parsing-Fehler) wiederholen" in {
      val (result, output) = simulateInput(List("nein", "8")) {
        retryUntilValid(prompt, intParser, rangeValidator)
      }
      result shouldBe 8
      // Prüft, ob der Fehler-Prompt 1x ausgegeben wurde
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 1
    }

    "eine ungültige Eingabe (Validierungs-Fehler) wiederholen" in {
      val (result, output) = simulateInput(List("15", "9")) {
        retryUntilValid(prompt, intParser, rangeValidator)
      }
      result shouldBe 9
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 1
    }

    "nach mehreren Fehlversuchen korrekt den letzten gültigen Wert zurückgeben" in {
      val (result, output) = simulateInput(List("falsch", "0", "11", "7")) {
        retryUntilValid(prompt, intParser, rangeValidator)
      }
      result shouldBe 7
      // 3 Fehlversuche = 3 Fehlermeldungen
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 3
    }
  }
}
