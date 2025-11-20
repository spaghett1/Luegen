package de.htwg.luegen.Controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

import de.htwg.luegen.Model._
import de.htwg.luegen.Outcomes
import de.htwg.luegen.View._

import scala.collection.mutable.Stack
import scala.util.Random

class GameControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  // Interne Case Class, um den Controller-Getter-Typ zu simulieren (für Vergleiche)
  private case class ActionDetails(
    isGameStart: Boolean = false,
    playedPlayer: Option[Player] = None,
    playedCards: List[Card] = Nil
  )

  // Setup Players and Cards for predictable results
  val p1 = Player("P1", List(Card("S", "A"), Card("H", "K")))
  val p2 = Player("P2", List(Card("C", "2")))
  val p3 = Player("P3", List(Card("D", "10")))
  val allPlayers = List(p1, p2, p3)

  // Setze den Random Seed, damit setupTurnOrder vorhersagbar ist
  Random.setSeed(42)

  /** NEU: Initialisiert den Controller mit einem Mock-Model. */
  def setupController(): (GameModel, GameController, GameView) = {
    // *** Wichtig: Model ist nun ein Mock! ***
    val mockModel: GameModel = mock[GameModel]

    // Setze erwarteten Zustand auf dem Mock:
    when(mockModel.players).thenReturn(allPlayers)
    when(mockModel.currentPlayer).thenReturn(p1)
    when(mockModel.getPrevPlayer()).thenReturn(p3) // Annahme: P3 ist vorheriger Spieler
    when(mockModel.roundRank).thenReturn("A") // Annahme: RoundRank ist gesetzt
    when(mockModel.validRanks).thenReturn(List("A", "K", "D")) // Für handleRoundRank Test

    val controller = new GameController(mockModel)
    val mockView: GameView = mock[GameView]
    controller.registerObserver(mockView)

    (mockModel, controller, mockView)
  }

  "A GameController (using simplified Mockito)" should {

    "initGame sollte nur notifyObservers aufrufen" in {
      val (_, controller, mockView) = setupController()

      controller.initGame()

      verify(mockView, times(1)).updateDisplay()
      controller.getLastActionOutcome should be(Outcomes.Invalid)
    }

    // --- Test der View-Callbacks ---

    "setupGame sollte Model initialisieren und GameStart Details setzen" in {
      val (mockModel, controller, mockView) = setupController()

      // Arrange (Simuliere Zustand vor setupGame)
      when(mockModel.players).thenReturn(Nil)

      // Act
      controller.setupGame(3, List("A", "B", "C"))

      // Assert Model-Aktionen
      verify(mockModel, times(1)).setupPlayers(List("A", "B", "C"))
      verify(mockModel, times(1)).dealCards()
      verify(mockModel, times(1)).setupTurnOrder()

      // Assert transienter Zustand (GameStart)
      controller.getLastActionOutcome should be(Outcomes.Played)
      controller.getLastActionDetails.get.isGameStart should be(true)

      verify(mockView, times(1)).updateDisplay()
    }

    "handleRoundRank sollte roundRank setzen und ActionDetails löschen" in {
      val (mockModel, controller, mockView) = setupController()

      // Act
      controller.handleRoundRank("K")

      // Assert Model-Aktionen
      verify(mockModel, times(1)).roundRank_=(any[String])

      // Assert transienter Zustand Reset
      controller.getLastActionOutcome should be(Outcomes.Played)
      controller.getLastActionDetails should be(None)

      verify(mockView, times(1)).updateDisplay()
    }

    "handleCardPlay sollte Karten spielen, ActionDetails setzen und Spieler wechseln" in {
      val (mockModel, controller, mockView) = setupController()

      val playedCards = List(Card("S", "X"), Card("S", "Y")) // Mock returned cards

      // Stub der Model-Aktionen
      when(mockModel.playCards(List(1, 2))).thenReturn(playedCards)

      // Act
      controller.handleCardPlay(List(1, 2))

      // Assert Model-Aktionen
      verify(mockModel, times(1)).playCards(List(1, 2)) // Verify input
      verify(mockModel, times(1)).setNextPlayer(Outcomes.Played) // Verify player advance

      // Assert transienter Zustand (Played)
      controller.getLastActionOutcome should be(Outcomes.Played)
      // Wir können die tatsächlich gemockten Karten prüfen
      controller.getLastActionDetails.get.playedCards should contain theSameElementsAs playedCards

      verify(mockView, times(1)).updateDisplay()
    }

    // --- Tests für Challenge Decision ---

    "handleChallengeDecision (callsLie=true) sollte LieWon korrekt behandeln (Gewinner bleibt)" in {
      val (mockModel, controller, mockView) = setupController()

      // Arrange for LieWon
      when(mockModel.evaluateReveal()).thenReturn(Outcomes.ChallengedLieWon)

      // Act
      controller.handleChallengeDecision(callsLie = true)

      // Assert Model-Aktionen
      verify(mockModel, times(1)).evaluateReveal()
      verify(mockModel, times(1)).setNextPlayer(Outcomes.ChallengedLieWon)

      // Assert transienter Zustand
      controller.getLastActionOutcome should be(Outcomes.ChallengedLieWon)
      controller.getLastActionDetails should be(None)

      verify(mockView, times(1)).updateDisplay()
    }

    "handleChallengeDecision (callsLie=false) sollte nicht evaluateReveal aufrufen und Spieler wechseln" in {
      val (mockModel, controller, mockView) = setupController()

      // Act
      controller.handleChallengeDecision(callsLie = false)

      // Assert Model-Aktionen
      verify(mockModel, never()).evaluateReveal()
      verify(mockModel, times(1)).setNextPlayer(Outcomes.Played)

      // Assert transienter Zustand
      controller.getLastActionOutcome should be(Outcomes.Played)
      controller.getLastActionDetails should be(None)

      verify(mockView, times(1)).updateDisplay()
    }

    "Getter-Methoden sollten Model-Daten zurückgeben" in {
      val (mockModel, controller, _) = setupController()

      when(mockModel.roundRank).thenReturn("10")
      when(mockModel.discardedCards).thenReturn(Stack(Card("S", "A")))

      controller.getCurrentPlayers should equal(allPlayers)
      controller.getDiscardedCount shouldBe 1
      controller.getCurrentPlayer should equal(p1)
      controller.getPrevPlayer should equal(p3)
      controller.isValidRanks should equal(List("A", "K", "D"))
      controller.isFirstTurn should equal(false) // da roundRank auf "A" gemockt ist
      controller.getRoundRank should equal("10")
    }
  }
}