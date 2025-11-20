package de.htwg.luegen.View

import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Controller.ActionDetails // NEU: Import der Top-Level-Klasse
import de.htwg.luegen.Model.{Card, Player}
import de.htwg.luegen.Outcomes
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

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

  // Instanz der REALEN GameView (da wir Grid nicht mocken können)
  val view = new GameView(mockController)

  val testPlayer = Player("Alice", List(Card("S", "A"), Card("H", "10")))
  val testPrevPlayer = Player("Bob")
  val defaultPlayers = List(testPlayer, testPrevPlayer)

  // Die interne Definition der ActionDetails wurde entfernt und durch den Import ersetzt.

  private val originalIn = System.in
  private val originalOut = System.out

  def simulateInput[T](inputs: List[String])(testCode: => T): (T, String) = {
    val inputData = inputs.mkString(System.lineSeparator())
    val inputStream = new ByteArrayInputStream(inputData.getBytes("UTF-8"))

    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, false, "UTF-8")

    System.setIn(inputStream)
    System.setOut(printStream)

    try {
      var result: T = null.asInstanceOf[T]

      withIn(inputStream) {
        withOut(printStream) {
          result = testCode
        }
      }

      printStream.flush()
      (result, outputStream.toString("UTF-8").trim)
    } finally {
      System.setIn(originalIn)
      System.setOut(originalOut)
    }
  }

  // NEUE HELFERFUNKTION: Setzt die minimalen Controller-Stubs, um NullPointer-Exceptions zu vermeiden.
  def setupDefaultStubs(
    isFirstTurn: Boolean = false,
    roundRank: String = "K",
    currentPlayer: Player = testPlayer,
    prevPlayer: Player = testPrevPlayer): Unit = {

    reset(mockController)
    // KRITISCH: Stubs für alle Getter, die in updateDisplay aufgerufen werden
    when(mockController.getCurrentPlayers).thenReturn(List(currentPlayer, prevPlayer))
    when(mockController.getDiscardedCount).thenReturn(0)
    when(mockController.getCurrentPlayer).thenReturn(currentPlayer) // FIX 1: Player ist nicht null
    when(mockController.getPrevPlayer).thenReturn(prevPlayer)
    when(mockController.isFirstTurn).thenReturn(isFirstTurn)
    when(mockController.getRoundRank).thenReturn(roundRank) // FIX 2: String ist nicht null
    when(mockController.isValidRanks).thenReturn(List("A", "K"))

    // Default transient state (no special message)
    when(mockController.getLastActionOutcome).thenReturn(Outcomes.Invalid)
    when(mockController.getLastActionDetails).thenReturn(None)
  }
  // ======================================================================================

  "A GameView" when {

    // ======================================================================================
    // Tests für die INPUT-Methoden (unverändert)
    // ======================================================================================
    "handling correct user input via retryUntilValid" should {

      "getNum sollte eine korrekte Spieleranzahl zurückgeben (4)" in {
        val (result, output) = simulateInput(List("4")) { view.getNum }
        result shouldBe 4
        output should include("Wieviele Spieler? (2-8)")
      }

      "readYesNo sollte true für 'j' zurückgeben" in {
        val (result, output) = simulateInput(List("j")) { view.readYesNo(testPrevPlayer) }
        result shouldBe true
      }
    }

    // ======================================================================================
    // Tests für die OUTPUT-Methoden (unverändert)
    // ======================================================================================
    "displayPlayerHand" should {
      "Indizes und Karten korrekt formatieren und drucken" in {
        val player = Player("Test", List(Card("H", "10"), Card("S", "2")))
        val output = captureOutput { view.displayPlayerHand(player) }
        output shouldBe "1  2  \nH10S2"
      }
    }

    "die Nachrichten korrekt drucken" should {
      "printLayedCards" in {
        val cards = List(Card("S", "K"))
        captureOutput { view.printLayedCards(testPlayer, cards) } shouldBe s"${testPlayer.name} legt ab: SK"
      }

      "challengerWonMessage" in {
        val output = captureOutput { view.challengerWonMessage(testPlayer, testPrevPlayer) }
        output should include ("Bob hat gelogen!")
      }
    }


    // ======================================================================================
    // NEUE TESTS FÜR updateDisplay (Flow Control und Output Interpretation)
    // ======================================================================================
    "updateDisplay (Flow Control)" should {

      "den Setup-Flow starten, wenn players leer sind" in {
        reset(mockController)
        // Zustand: Leer
        when(mockController.getCurrentPlayers).thenReturn(List.empty)

        // Simulate Input: 2 Spieler, Namen P1, P2
        val (_, _) = simulateInput(List("2", "P1", "P2")) {
          view.updateDisplay()
        }

        verify(mockController).setupGame(2, List("P1", "P2"))
      }

      "den RoundRank-Flow starten, wenn isFirstTurn true ist" in {
        // Hinzufügen der Default-Stubs mit isFirstTurn = true
        setupDefaultStubs(isFirstTurn = true, roundRank = "")

        // Simulate Input: "A"
        val (_, _) = simulateInput(List("A")) {
          view.updateDisplay()
        }

        verify(mockController).handleRoundRank("A")
      }

      "den Challenge-Flow starten, wenn currentPlayer != prevPlayer" in {
        // Hinzufügen der Default-Stubs
        setupDefaultStubs(isFirstTurn = false, roundRank = "K")

        // Simulate Input: "j" (calls lie)
        val (_, _) = simulateInput(List("j")) {
          view.updateDisplay()
        }

        verify(mockController).handleChallengeDecision(true)
      }

      "den PlayCard-Flow starten, wenn currentPlayer == prevPlayer" in {
        // Hinzufügen der Default-Stubs, wobei currentPlayer und prevPlayer gleich sind
        setupDefaultStubs(isFirstTurn = false, roundRank = "K", prevPlayer = testPlayer)

        // Simulate Input: "1" (selects card 1)
        val (_, output) = simulateInput(List("1")) {
          view.updateDisplay()
        }

        verify(mockController).handleCardPlay(List(1))
        output should include (s"${testPlayer.name} ist am Zug")
      }
    }


    "updateDisplay (Output Interpretation)" should {

      "die Startmeldung anzeigen, wenn ActionOutcome Played und isGameStart True ist" in {
        setupDefaultStubs(isFirstTurn = true, roundRank = "") // isFirstTurn ist true

        // Transienter Zustand für GameStart
        when(mockController.getLastActionOutcome).thenReturn(Outcomes.Played)
        val startDetails = Some(ActionDetails(isGameStart = true))
        when(mockController.getLastActionDetails).thenReturn(startDetails)

        // Dummy I/O (RoundRank)
        val (_, output) = simulateInput(List("A")) {
          view.updateDisplay()
        }
        output should include (s"Das Spiel startet mit ${testPlayer.name}!")
        verify(mockController).handleRoundRank("A")
      }

      "die LayedCards-Meldung anzeigen, wenn ActionOutcome Played und Karten gespielt wurden" in {
        setupDefaultStubs(isFirstTurn = false, roundRank = "K")

        // Transienter Zustand für Cards Played
        when(mockController.getLastActionOutcome).thenReturn(Outcomes.Played)
        val playedCards = List(Card("H", "A"), Card("C", "K"))
        val playedDetails = Some(ActionDetails(playedPlayer = Some(testPrevPlayer), playedCards = playedCards))
        when(mockController.getLastActionDetails).thenReturn(playedDetails)

        // Dummy I/O (Challenge-Flow)
        val (_, output) = simulateInput(List("n")) {
          view.updateDisplay()
        }
        output should include (s"${testPrevPlayer.name} legt ab: HA, CK")
        verify(mockController).handleChallengeDecision(false) // 'n' für keine Lüge
      }

      "die ChallengerWon-Meldung anzeigen, wenn ActionOutcome ChallengedLieWon ist" in {
        setupDefaultStubs(isFirstTurn = false, roundRank = "K")

        // Transienter Zustand für Challenger Won
        when(mockController.getLastActionOutcome).thenReturn(Outcomes.ChallengedLieWon)
        when(mockController.getLastActionDetails).thenReturn(None)

        // Dummy I/O (Challenge-Flow)
        val (_, output) = simulateInput(List("j")) {
          view.updateDisplay()
        }
        output should include (s"${testPrevPlayer.name} hat gelogen!")
        verify(mockController).handleChallengeDecision(true)
      }
    }
  }
}