package de.htwg.luegen.View

import de.htwg.luegen.Controller.Observer
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*

import scala.util.Try
import scala.io.StdIn
import de.htwg.luegen.View.*

class GameView(val controller: GameController) extends Observer {
  private var grid = new Grid
  

  private val stateToScreen: Map[TurnState, GameScreen] = Map(
    NeedsPlayerCount -> NeedsPlayerCountScreen,
    NeedsPlayerNames -> NeedsPlayerNamesScreen,
    NeedsRankInput -> NeedsRankInputScreen,
    NeedsCardInput -> NeedsCardInputScreen,
    NeedsChallengeDecision -> NeedsChallengeDecisionScreen,
    ChallengedLieWon -> ChallengedLieWonScreen,
    ChallengedLieLost -> ChallengedLieLostScreen,
    Played -> PlayedScreen,
  )
  
  @volatile private var updatePending: Boolean = false
  
  override def updateDisplay(): Unit = synchronized {
    updatePending = true
    this.notify()
  }
  
  def runTuiLoop(): Unit = {
    renderStateAndHandleInput()

    while (true) {
      synchronized {
        while (!updatePending) {
          try {
            wait()
          } catch {
            case _: InterruptedException => Thread.currentThread().interrupt()
          }
        }
        updatePending = false
      }
      
      renderStateAndHandleInput()
    }
  }
  
  def renderStateAndHandleInput(): Unit = {
    val model = controller.model
    val players = model.players
    val discardedCount = model.discardedCards.length
    val roundRank = controller.getRoundRank
    val state = controller.getTurnState
    val log = controller.getLog

    println(log)

    if (players.nonEmpty) {
      val player = controller.getCurrentPlayer
      grid = grid.updateGridWithPlayers(players)
      val output = grid.updateGridWithNumber(discardedCount)
      println(output)
      displayPlayerHand(player)
      println(s"Aktueller Rang: ${if (roundRank.isEmpty) "Keiner" else roundRank }")
    }

    stateToScreen.get(state) match {
      case Some(screen) =>
        screen.renderAndHandleInput(controller)
      case None =>
        println("Ungueltiger Zustand!")
    }
  }

  def initGrid(players: List[Player]) = { }
  def printPrompt(prompt: String): Unit = { }
  def displayPlayerHand(player: Player): Unit = { }
  def printLayedCards(player: Player, cards: List[Card]) = { }
  def startGamePrompt(player: Player) = { }
}

//test