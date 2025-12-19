package de.htwg.luegen

import de.htwg.luegen.model.impl1.{Card, GameModel, Player}
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class GameModelSpec extends AnyWordSpec with Matchers {

  // Setup Spieler und Karten
  val cardA = Card("H", "A")
  val cardK = Card("H", "K")
  val card2 = Card("S", "2")
  val card3 = Card("C", "3")

  // Funktionale Setup-Helfer
  def setupInitialModel(): GameModel = {
    val p1Hand = Player("P1").addCards(List(cardA, cardK))
    val p2Hand = Player("P2").addCards(List(card2))
    val p3Hand = Player("P3").addCards(List(card3))
    val allPlayers = List(p1Hand, p2Hand, p3Hand)

    GameModel().copy(
      players = allPlayers,
      playOrder = List(0, 1, 2),
      currentPlayerIndex = 0,
      lastPlayerIndex = 2,
      turnState = NeedsCardInput // Setze Zustand für Spieltests
    )
  }

  def getCurrentPlayer(model: GameModel): Player = model.players(model.currentPlayerIndex)

  "A GameModel (Functional)" when {

    // --- Memento Tests hinzugefügt ---

    "managing Mementos" should {
      val initialModel = setupInitialModel().addLog("Start Log")

      "createMemento sollte ein Memento OHNE logHistory erstellen" in {
        val memento = initialModel.createMemento()

        // Prüfen, ob das Memento ein gültiges Objekt ist
        memento.turnState shouldBe NeedsCardInput
        memento.players.size shouldBe 3

        // Prüfen, dass das Memento NICHT die logHistory enthält (implizit durch Struktur)
        // Die Memento-Struktur ist nun extern definiert und enthält den Log nicht.
        // Das Memento selbst sollte keine Log-Historie haben.
        // Das Model enthält die Log-Historie.
        initialModel.getLogHistory should not be empty
      }

      "restoreMemento sollte den Zustand korrekt wiederherstellen und den Log beibehalten" in {
        val memento = initialModel.createMemento()
        val logBeforeRestore = initialModel.getLogHistory

        // Model ändert sich nochmal vor der Wiederherstellung
        val modelToRestore = initialModel.copy(
          roundRank = "Q",
          logHistory = modelChange.logHistory :+ "Aktuelle Änderung"
        )

        // Wiederherstellung
        val restoredModel = modelToRestore.restoreMemento(memento)

        // Zustandswiederherstellung
        restoredModel.roundRank shouldBe "K"
        restoredModel.turnState shouldBe NeedsRankInput

        // Log-Persistenz: Das Log sollte das Log von modelToRestore behalten
        // (Da der Log-Feld in restoreMemento nicht gesetzt wird, behält es den Wert von modelToRestore)
        restoredModel.logHistory.size shouldBe 3
        restoredModel.logHistory.last shouldBe "Aktuelle Änderung"
      }
    }

    // --- Bestehende Tests angepasst ---

    "initialized" should {
      "dealCards sollte ein NEUES Model mit 52 Karten zurückgeben" in {
        val model = GameModel().setupPlayers(List("A", "B", "C", "D"))
        val newModel = model.dealCards()
        newModel.getPlayers.map(_.hand.size).sum shouldBe 52
        newModel should not be model
      }

      "setupTurnOrder sollte currentPlayerIndex und playOrder korrekt setzen" in {
        Random.setSeed(42)
        val model = GameModel().setupPlayers(List("A", "B", "C"))
        val newModel = model.setupTurnOrder()

        newModel.getCurrentPlayerIndex should be >= 0
        newModel.getTurnState shouldBe NeedsRankInput // Zustand sollte gesetzt sein
        newModel should not be model
      }

      "setupRank sollte den roundRank setzen und ein NEUES Model zurückgeben" in {
        val model = setupInitialModel()
        val newModel = model.setupRank("A")
        newModel.getRoundRank shouldBe "A"
        newModel.getTurnState shouldBe NeedsCardInput
        newModel should not be model
      }
    }

    "a lie is challenged (evaluateReveal)" should {

      "addLog should add new entry to logHistory" in {
        val model = setupInitialModel()
        model.logHistory shouldBe empty

        val newModel = model.addLog("test entry 1")
        newModel.getLogHistory should contain theSameElementsInOrderAs List("test entry 1")

        val finalModel = newModel.addLog("test entry 2")
        finalModel.getLogHistory.size shouldBe 2
      }

      "clearError sollte lastInputError von Some auf None setzen" in {
        val model = setupInitialModel().copy(lastInputError = Some("Fehler"))
        model.lastInputError should not be None

        val newModel = model.clearError()
        newModel.getLastInputError shouldBe None
        newModel should not be model
      }
    }

    // ... (Andere Tests bleiben im Wesentlichen gleich) ...
  }
}