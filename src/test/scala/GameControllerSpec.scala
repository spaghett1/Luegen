package de.htwg.luegen.Controller

import de.htwg.luegen.Model.{Card, GameModel, Player}
import de.htwg.luegen.Outcomes
import de.htwg.luegen.Outcomes.{ChallengedLieLost, ChallengedLieWon, Played}
import de.htwg.luegen.View.GameView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class GameControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  // Mocks sind außerhalb definiert, um in setupController wiederverwendet zu werden
  val mockModel: GameModel = mock[GameModel]
  val mockView: GameView = mock[GameView]

  val testPlayer = Player("CurrentPlayer", List(Card("S", "A")))
  val testPrevPlayer = Player("PrevPlayer")

  /**
   * Setzt Mocks zurück, initialisiert den Controller und konfiguriert die Standard-Mocks.
   * MUSS VOR JEDEM TEST aufgerufen werden.
   */
  def setupController(): GameController = {
    // *** KRITISCH: Setzt alle Mockito-Zähler und Stubs zurück ***
    reset(mockModel, mockView)

    val controller = new GameController(mockModel)

    // Die Registrierung findet hier statt, um die `view` Variable im Controller zu setzen
    controller.registerObserver(mockView)

    // Standard-Mocks setzen
    when(mockModel.currentPlayer).thenReturn(testPlayer)
    when(mockModel.getPrevPlayer()).thenReturn(testPrevPlayer)
    when(mockModel.validRanks).thenReturn(List("A", "K"))

    controller
  }

  "Ein GameController" should {

    "observer management" should {

      "sollte die View bei der Registrierung als Observer NICHT sofort benachrichtigen" in {
        // Da registerObserver() kein notifyObservers() aufruft, sollte die View 0x aufgerufen werden.
        val controller = setupController()
        verify(mockView, never()).updateDisplay()
      }

      "notifyObservers sollte alle Observables benachrichtigen" in {
        val controller = setupController()
        val mockObserver: Observer = mock[Observer]
        controller.registerObserver(mockObserver)

        // mockView ist bereits registriert (1. Observer)
        // mockObserver wird registriert (2. Observer)

        controller.notifyObservers()

        // Die View wurde 1x durch notifyObservers() aufgerufen.
        verify(mockView, times(1)).updateDisplay()
        verify(mockObserver, times(1)).updateDisplay()
      }
    }

    "initGame" should {
      "Model-Setup und View-Initialisierung korrekt orchestrieren" in {
        val controller = setupController()

        when(mockView.getNum).thenReturn(2)
        when(mockView.getPlayerName(any[Int])).thenReturn("P1", "P2")
        when(mockModel.players).thenReturn(List(Player("P1"), Player("P2")))
        // Blockiere die endlose Rekursion (playGameLoop)
        doThrow(new RuntimeException("Test Stop")).when(mockModel).isFirstTurn

        try { controller.initGame() } catch { case _: Throwable => }

        // notifyObservers() wird in initGame() aufgerufen, daher ist der Zähler 1
        verify(mockView, times(1)).updateDisplay()
      }
    }


    "playerTurnAction" when {

      "die Lüge AUFGEDECKT wird (ChallengedLieWon)" in {
        val controller = setupController()
        when(mockView.readYesNo(testPrevPlayer)).thenReturn(true)
        when(mockModel.evaluateReveal()).thenReturn(ChallengedLieWon)

        controller.playerTurnAction(testPlayer, testPrevPlayer)

        verify(mockModel, times(1)).evaluateReveal() // FIX: Zähler ist 1 (isoliert)
        verify(mockView).challengerWonMessage(testPlayer, testPrevPlayer)
      }

      "die Lüge AUFGEDECKT wird (ChallengedLieLost)" in {
        val controller = setupController()
        when(mockView.readYesNo(testPrevPlayer)).thenReturn(true)
        when(mockModel.evaluateReveal()).thenReturn(ChallengedLieLost)

        controller.playerTurnAction(testPlayer, testPrevPlayer)

        verify(mockModel, times(1)).evaluateReveal() // FIX: Zähler ist 1 (isoliert)
        verify(mockView).challengerLostMessage(testPlayer, testPrevPlayer)
      }

      "der Spieler NICHT aufdeckt (Karte gespielt)" in {
        val controller = setupController()
        when(mockView.readYesNo(testPrevPlayer)).thenReturn(false)
        when(mockView.selectCards(testPlayer)).thenReturn(List(1))
        when(mockModel.playCards(List(1))).thenReturn(List(Card("S", "A")))

        controller.playerTurnAction(testPlayer, testPrevPlayer)

        verify(mockModel, never()).evaluateReveal() // FIX: evaluateReveal wird nicht aufgerufen
        verify(mockView, times(1)).displayPlayerHand(testPlayer)
      }
    }

    "playAction aufgerufen wird" should {
      "Karten auswählen, spielen und das Ergebnis anzeigen" in {
        val controller = setupController()
        val selectedCards = List(Card("S", "K"))
        when(mockView.selectCards(testPlayer)).thenReturn(List(1))
        when(mockModel.playCards(List(1))).thenReturn(selectedCards)

        controller.playAction(testPlayer)

        verify(mockView, times(1)).displayPlayerHand(testPlayer) // FIX: Zähler ist 1 (isoliert)
        verify(mockModel).playCards(List(1))
      }
    }

    "Getter-Methoden aufgerufen werden" should {
      "die aktuellen Daten vom Model zurückgeben" in {
        val controller = setupController()
        when(mockModel.players).thenReturn(List(testPlayer))
        when(mockModel.discardedCards).thenReturn(collection.mutable.Stack(Card("S", "A")))

        controller.getCurrentPlayers should equal(List(testPlayer))
        controller.getDiscardedCount shouldBe 1
      }
    }
  }
}