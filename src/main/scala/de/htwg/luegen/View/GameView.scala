package de.htwg.luegen.View

import de.htwg.luegen.Controller.Observer
import de.htwg.luegen.Controller.IGameController
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*

import scala.util.Try
import scala.io.StdIn
import de.htwg.luegen.View.*

class GameView(controller: IGameController) extends Observer {
  private var grid = new Grid
  
  controller.registerObserver(this)

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
  
  override def updateDisplay(): Unit = {

    val playerCount = controller.getPlayerCount
    val players = controller.getCurrentPlayers
    val playedCards = controller.getPlayedCards
    val discardedCount = controller.getDiscardedCount
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
    
    stateToScreen.get(state).foreach(_.display(controller))
  }

  def handleInput(): Unit = {
    val state = controller.getTurnState
    val input = StdIn.readLine()
    if (input != null) {
      val state = controller.getTurnState
      stateToScreen.get(state).foreach(_.processInput(input, controller))
    }
  }
  
  def initGrid(players: List[Player]) = {
    grid.initGrid(players)
  }

  def printPrompt(prompt: String): Unit = {
    print(prompt)
  }

  def displayPlayerHand(player: Player): Unit = {
    val width = player.longestCardName

    val indices = (1 to player.hand.length)
      .map(i => String.format(s"%-${width}s", i.toString))
      .mkString

    val cards = player.hand
      .map(c => String.format(s"%-${width}s", c.toString))
      .mkString

    // todo
    println(indices)
    println(cards)

  }

  def printLayedCards(player: Player, cards: List[Card]) = {
    println(s"${player.name} legt ab: ${cards.mkString(", ")}")
  }
  
  def startGamePrompt(player: Player) = {
    println(s"Das Spiel startet mit ${player.name}!")
  }

  def challengerWonMessage(player: Player, prevPlayer: Player) = {
    println(s"${prevPlayer.name} hat gelogen!")
    println("Er zieht alle Karten.")
  }

  def challengerLostMessage(player: Player, prevPlayer: Player) = {
    println(s"${prevPlayer.name} hat die Wahrheit gesagt!")
    println(s"${player.name} zieht alle Karten!")
  }
}
