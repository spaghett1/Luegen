package de.htwg.luegen

import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{AI, Card, GameModel, Human, Player, PlayerType, *}
import de.htwg.luegen.TurnState.*
import de.htwg.luegen.View.{GameScreen, GameView, NeedsCardInputScreen, NeedsChallengeDecisionScreen, NeedsPlayerCountScreen, NeedsPlayerNamesScreen, NeedsRankInputScreen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import scala.Console.{withIn, withOut}

// Mock Controller, der handleRawInput aufzeichnet
class MockInputController(initialModel: GameModel = GameModel()) extends GameController(initialModel) {
  var lastRawInput: String = ""
  var executionCount: Int = 0

  override def handleRawInput(rawInput: String): GameModel = {
    lastRawInput = rawInput
    executionCount += 1
    // Rückgabe eines Dummy-Models (damit der Code weiterläuft)
    initialModel.copy(turnState = Played)
  }

  // Dummy Getter für die View
  override def getCurrentPlayer: Player = initialModel.players.headOption.getOrElse(Player("Test"))
  override def getPrevPlayer: Player = initialModel.players.lastOption.getOrElse(Player("Prev"))
  override def getCurrentPlayerType: PlayerType = initialModel.players.headOption.getOrElse(Player("Test")).playerType
  override def isValidRanks: List[String] = List("2", "A")
}

class GameScreenSpec extends AnyWordSpec with Matchers {
  private val originalIn = System.in
  private val originalOut = System.out

  // Helfer für I/O Simulation
  def simulateScreenInput(input: String, model: GameModel = GameModel(), screen: GameScreen): (MockInputController, String) = {
    // Stellt den Input-Stream bereit
    val inputNewLinw = input + System.lineSeparator()
    val inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"))
    val outputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(outputStream, true, "UTF-8")

    System.setIn(inputStream)
    System.setOut(printStream)

    val mockController = new MockInputController(model)
    // GameView instanziieren, die den Controller nutzt
    val dummyView = new GameView(mockController)

    try {
      withIn(inputStream) {
        withOut(outputStream) {
          screen.renderAndHandleInput(mockController, dummyView)
        }
      }
      printStream.flush()
      (mockController, outputStream.toString("UTF-8").trim)
    } finally {
      System.setIn(originalIn)
      System.setOut(originalOut)
    }
  }

  // Allgemeines Setup-Model für Input-Tests
  val testPlayer = Player("Human", List(Card("H", "A")))
  val aiPlayer = Player("AI", List(Card("H", "K")), AI)
  val baseModel = GameModel().copy(
    players = List(testPlayer, aiPlayer),
    currentPlayerIndex = 0,
    playerCount = 2
  )

  "GameScreen Objects" should {

    "NeedsPlayerCountScreen (Human) should read input and delegate" in {
      val (controller, output) = simulateScreenInput("4", baseModel.copy(turnState = NeedsPlayerCount), NeedsPlayerCountScreen)
      output should include("Wieviele Spieler?")
      controller.lastRawInput shouldBe "4"
      controller.executionCount shouldBe 1
    }

    "NeedsPlayerNamesScreen (Human) should read input and delegate" in {
      val (controller, output) = simulateScreenInput("Max,Anna", baseModel.copy(turnState = NeedsPlayerNames), NeedsPlayerNamesScreen)
      output should include("Gebe die Spielernamen ein")
      controller.lastRawInput shouldBe "Max,Anna"
    }

    "NeedsRankInputScreen (Human) should read input and delegate" in {
      val (controller, output) = simulateScreenInput("A", baseModel.copy(turnState = NeedsRankInput), NeedsRankInputScreen)
      output should include("Sage einen Rang")
      controller.lastRawInput shouldBe "A"
    }

    "NeedsRankInputScreen (AI) should use default rank and delegate" in {
      // Das ReadLine wird übersprungen, AI wählt den ersten validen Rang ("2" im Mock Controller)
      val (controller, output) = simulateScreenInput("", baseModel.copy(turnState = NeedsRankInput, players = List(aiPlayer)), NeedsRankInputScreen)
      controller.lastRawInput shouldBe "2"
      // Der Prompt wird trotzdem ausgegeben, da er außerhalb der AI-Logik liegt
      output should include("Sage einen Rang")
    }

    "NeedsCardInputScreen (Human) should read input and delegate" in {
      val (controller, output) = simulateScreenInput("1,2", baseModel.copy(turnState = NeedsCardInput), NeedsCardInputScreen)
      output should include("Gebe Kartenindices ein")
      controller.lastRawInput shouldBe "1,2"
    }

    "NeedsCardInputScreen (AI) should use default index '1' and delegate" in {
      val (controller, output) = simulateScreenInput("", baseModel.copy(turnState = NeedsCardInput, players = List(aiPlayer)), NeedsCardInputScreen)
      controller.lastRawInput shouldBe "1"
    }

    "NeedsChallengeDecisionScreen (Human) should read input and delegate" in {
      val (controller, output) = simulateScreenInput("j", baseModel.copy(turnState = NeedsChallengeDecision), NeedsChallengeDecisionScreen)
      output should include("Luege von AI aufdecken? (j/n)")
      controller.lastRawInput shouldBe "j"
    }

    "NeedsChallengeDecisionScreen (AI) should use default 'n' and delegate" in {
      val (controller, output) = simulateScreenInput("", baseModel.copy(turnState = NeedsChallengeDecision, players = List(aiPlayer)), NeedsChallengeDecisionScreen)
      controller.lastRawInput shouldBe "n"
    }
  }
}
