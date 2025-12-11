package de.htwg.luegen

import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{Card, GameModel, Human, Player, PlayerType}
import de.htwg.luegen.View.GameView
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import scala.Console.{withIn, withOut}
import scala.util.Try
import de.htwg.luegen.Controller.Observer // Import hinzufügen

/**
 * Dummy GameController: Erforderlich, um eine GameView zu instanziieren und 
 * die minimal notwendigen Controller-Methoden (Getter) für die View zu mocken.
 */
class DummyController(initialModel: GameModel = GameModel()) extends GameController(initialModel) {
  // Überschreiben nur der minimal notwendigen Getter, um die View-Methoden auszuführen
  override def isValidRanks: List[String] = List("2", "A")
  override def getCurrentPlayer: Player = initialModel.players.headOption.getOrElse(Player("Dummy"))
  override def getPrevPlayer: Player = initialModel.players.lastOption.getOrElse(Player("DummyPrev"))
  override def getLog: List[String] = initialModel.logHistory
  override def getInputError: Option[String] = initialModel.lastInputError
  override def getCurrentPlayerType: PlayerType = Human
}

class GameViewSpec extends AnyWordSpec with Matchers {

  private val originalIn = System.in
  private val originalOut = System.out

  // Helfer zur Erstellung einer View mit einem DummyController
  private def createView(model: GameModel = GameModel(players = List(Player("Dummy")))) =
    new GameView(new DummyController(model))

  /**
   * HILFSFUNKTION: Erfasst Konsolenausgabe, notwendig für das Testen von Output-Methoden.
   */
  def captureOutput[T](testCode: => T): (T, String) = {
    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, true, "UTF-8")

    System.setOut(printStream)

    try {
      val result = withOut(printStream) {
        testCode
      }
      printStream.flush()
      (result, outputStream.toString("UTF-8").trim)
    } finally {
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

  // --------------------------------------------------------------------------------

  "GameView Output/Message Methods" should {

    "initGrid sollte ohne Fehler ausgeführt werden" in {
      val players = List(Player("A"), Player("B"), Player("C"), Player("D"))
      val view = createView()

      // Prüft nur, ob die Methode ohne Fehler durchläuft
      noException should be thrownBy view.initGrid(players)
    }

    "printPrompt sollte den Text auf der Konsole ausgeben" in {
      val (_, output) = captureOutput {
        createView().printPrompt("Test-Prompt")
      }
      output should include("Test-Prompt")
    }

    "displayPlayerHand sollte Indizes und Karten korrekt formatieren (mittlere Länge)" in {
      // Longest Card Name ist 3 (S10)
      val (_, output) = captureOutput {
        createView().displayPlayerHand(testPlayer) // Hand: [HA, CK, S10]
      }
      val lines = output.split('\n')
      // Indices (1, 2, 3) - mit Padding auf 3
      lines(0).trim shouldBe "1  2  3"
      // Cards (HA, CK, S10) - mit Padding auf 3
      lines(1).trim shouldBe "HA CK S10"
    }

    "displayPlayerHand sollte korrekt padden, wenn eine Karte eine andere Länge hat" in {
      // Longest Card Name ist 4 (D100)
      val (_, output) = captureOutput {
        createView().displayPlayerHand(playerWithLongNameCard) // Hand: [D100, HA]
      }
      val lines = output.split('\n')
      // Indices (1, 2) - mit Padding auf 4
      lines(0).trim shouldBe "1   2"
      // Cards (D100, HA) - mit Padding auf 4
      lines(1).trim shouldBe "D100HA"
    }

    "printLayedCards sollte die gespielten Karten korrekt ausgeben" in {
      val cards = List(cardA, cardK)
      val (_, output) = captureOutput {
        createView().printLayedCards(testPlayer, cards)
      }
      output shouldBe s"${testPlayer.name} legt ab: HA, CK"
    }

    "startGamePrompt sollte die Startnachricht korrekt ausgeben" in {
      val (_, output) = captureOutput {
        createView().startGamePrompt(testPlayer)
      }
      output shouldBe s"Das Spiel startet mit ${testPlayer.name}!"
    }

    "challengerWonMessage (LieWon) sollte die Nachricht für den erfolgreichen Aufdecker ausgeben" in {
      val prevPlayer = Player("Prev")
      val (_, output) = captureOutput {
        createView().challengerWonMessage(testPlayer, prevPlayer)
      }
      val lines = output.split('\n')
      lines(0) shouldBe s"${prevPlayer.name} hat gelogen!"
      lines(1) shouldBe "Er zieht alle Karten."
    }

    "challengerLostMessage (LieLost) sollte die Nachricht für den erfolglosen Aufdecker ausgeben" in {
      val prevPlayer = Player("Prev")
      val (_, output) = captureOutput {
        createView().challengerLostMessage(testPlayer, prevPlayer)
      }
      val lines = output.split('\n')
      lines(0) shouldBe s"${prevPlayer.name} hat die Wahrheit gesagt!"
      lines(1) shouldBe s"${testPlayer.name} zieht alle Karten!"
    }
  }
}