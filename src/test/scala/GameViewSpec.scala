package de.htwg.luegen.View

import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{Card, GameModel, Player}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import scala.Console.{withIn, withOut}
import scala.util.Try

/**
 * Dummy GameController für die Instanziierung der GameView.
 * Stellt nur die minimal notwendigen Getter bereit, um View-Methoden ohne Absturz zu testen.
 */
class DummyController(initialModel: GameModel = GameModel()) extends GameController(initialModel) {
  // Für callRank-Tests: Minimal gültige Ränge
  override def isValidRanks: List[String] = List("2", "A")
}

class GameViewSpec extends AnyWordSpec with Matchers {

  private val originalIn = System.in
  private val originalOut = System.out
  private val dummyController = new DummyController()
  // GameView Instanziierungs-Helfer
  private def createView() = new GameView(dummyController)

  /**
   * HILFSFUNKTION: Simuliert Benutzereingaben und erfasst Konsolenausgabe,
   * notwendig für das Testen von Methoden, die `StdIn.readLine()` verwenden.
   */
  def simulateInput[T](inputs: List[String])(testCode: => T): (T, String) = {
    val inputData = inputs.mkString("\n") + "\n" // wichtiges trailing newline
    val inputStream = new ByteArrayInputStream(inputData.getBytes("UTF-8"))

    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, true, "UTF-8") // auto-flush true

    System.setIn(inputStream)
    System.setOut(printStream)

    try {
      // Wenn Testcode blockt wegen fehlender Input, brechen wir ihn ab statt zu hängen
      val result = Try {
        withIn(inputStream) {
          withOut(printStream) {
            testCode
          }
        }
      } match {
        case scala.util.Success(value) => value
        case scala.util.Failure(e) =>
          throw new RuntimeException("❌ Test blockte oder crashte wegen fehlender Eingabe! " +
            "Bitte testen, ob genug Input geliefert wurde.\nUrsache: " + e.getMessage, e)
      }

      (result, outputStream.toString("UTF-8").trim)
    } finally {
      System.setIn(originalIn)
      System.setOut(originalOut)
    }
  }

  // Beispiel-Daten
  val cardA = Card("H", "A")
  val cardK = Card("C", "K")
  val card10 = Card("S", "10") // Länge 3
  val cardLong = Card("D", "100") // Länge 4
  val testPlayer = Player("Tester", List(cardA, cardK, card10))
  val playerWithLongNameCard = Player("P", List(cardLong, cardA))

  "GameView Input Methods (Testing retryUntilValid wrapper)" should {

    "getNum sollte eine gültige Zahl (2-8) zurückgeben und ungültige wiederholen" in {
      // Input: "1" (ungültig), "neun" (ungültig/Parse-Fehler), "4" (gültig)
      val (result, output) = simulateInput(List("1", "neun", "4")) {
        createView().getNum
      }
      result shouldBe 4
      output should include("Wieviele Spieler? (2-8)")
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 2
    }

    "getPlayerName sollte einen gültigen Namen (< 10 Zeichen) zurückgeben" in {
      // Input: "" (ungültig), "DerNameIstZuLangFuerZehn" (ungültig), "Max" (gültig)
      val (result, output) = simulateInput(List("", "DerNameIstZuLangFuerZehn", "Max")) {
        createView().getPlayerName(1)
      }
      result shouldBe "Max"
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 2
    }

    "callRank sollte einen gültigen Rang ('A') zurückgeben" in {
      // Input: "J" (ungültig), "A" (gültig, da in DummyController.isValidRanks)
      val (result, output) = simulateInput(List("J", "A")) {
        createView().callRank(dummyController.isValidRanks)
      }
      result shouldBe "A"
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 1
    }

    "selectCards sollte bis zu drei gültige Indizes zurückgeben" in {
      // P1 Hand size is 3
      // Input: "1,2,3,4" (zu viele/ungültiger Index), "1,2" (gültig)
      val (result, output) = simulateInput(List("1,2,3,4", "1,2")) {
        createView().selectCards(testPlayer)
      }
      result should contain theSameElementsInOrderAs List(1, 2)
      output.split('\n').count(_ contains "Ungueltige Eingabe!") shouldBe 1
    }

    "readYesNo sollte true für 'j' und false für 'n' zurückgeben" in {
      // Input: "falsch", "j"
      val (resultJ, _) = simulateInput(List("falsch", "j")) {
        createView().readYesNo(testPlayer)
      }
      resultJ shouldBe true

      // Input: "n"
      val (resultN, _) = simulateInput(List("n")) {
        createView().readYesNo(testPlayer)
      }
      resultN shouldBe false
    }
  }

  // --------------------------------------------------------------------------------

  "GameView Output/Message Methods" should {

    "printPrompt sollte den Text auf der Konsole ausgeben" in {
      val (_, output) = simulateInput(Nil) {
        createView().printPrompt("Test-Prompt")
      }
      output should include("Test-Prompt")
    }

    "displayPlayerHand sollte Indizes und Karten korrekt formatieren (mittlere Länge)" in {
      // Longest Card Name ist 3 (S10)
      val (_, output) = simulateInput(Nil) {
        createView().displayPlayerHand(testPlayer) // Hand: [HA, CK, S10]
      }
      val lines = output.split('\n')
      // Indices (1, 2, 3) - mit Padding auf 3
      lines(0) shouldBe "1  2  3  "
      // Cards (HA, CK, S10) - mit Padding auf 3
      lines(1) shouldBe "HA CK S10"
    }

    "displayPlayerHand sollte korrekt padden, wenn eine Karte eine andere Länge hat" in {
      // Longest Card Name ist 4 (D100)
      val (_, output) = simulateInput(Nil) {
        createView().displayPlayerHand(playerWithLongNameCard) // Hand: [D100, HA]
      }
      val lines = output.split('\n')
      // Indices (1, 2) - mit Padding auf 4
      lines(0) shouldBe "1   2   "
      // Cards (D100, HA) - mit Padding auf 4
      lines(1) shouldBe "D100HA"
    }

    "printLayedCards sollte die gespielten Karten korrekt ausgeben" in {
      val cards = List(cardA, cardK)
      val (_, output) = simulateInput(Nil) {
        createView().printLayedCards(testPlayer, cards)
      }
      output shouldBe s"${testPlayer.name} legt ab: HA, CK"
    }

    "startGamePrompt sollte die Startnachricht korrekt ausgeben" in {
      val (_, output) = simulateInput(Nil) {
        createView().startGamePrompt(testPlayer)
      }
      output shouldBe s"Das Spiel startet mit ${testPlayer.name}!"
    }

    "challengerWonMessage (LieWon) sollte die Nachricht für den erfolgreichen Aufdecker ausgeben" in {
      val prevPlayer = Player("Prev")
      val (_, output) = simulateInput(Nil) {
        createView().challengerWonMessage(testPlayer, prevPlayer)
      }
      val lines = output.split('\n')
      lines(0) shouldBe s"${prevPlayer.name} hat gelogen!"
      lines(1) shouldBe "Er zieht alle Karten."
    }

    "challengerLostMessage (LieLost) sollte die Nachricht für den erfolglosen Aufdecker ausgeben" in {
      val prevPlayer = Player("Prev")
      val (_, output) = simulateInput(Nil) {
        createView().challengerLostMessage(testPlayer, prevPlayer)
      }
      val lines = output.split('\n')
      lines(0) shouldBe s"${prevPlayer.name} hat die Wahrheit gesagt!"
      lines(1) shouldBe s"${testPlayer.name} zieht alle Karten!"
    }
  }

  // --------------------------------------------------------------------------------

  "GameView Utility Methods" should {

    "initGrid sollte ohne Fehler ausgeführt werden" in {
      val players = List(Player("A"), Player("B"), Player("C"), Player("D"))
      val view = createView()

      // Prüft nur, ob die Methode ohne Fehler durchläuft, da die interne Grid-Instanz private ist
      noException should be thrownBy view.initGrid(players)
    }
  }
}
