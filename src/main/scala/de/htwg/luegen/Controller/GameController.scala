package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.Outcomes
import de.htwg.luegen.Outcomes.Played

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec

class GameController(val model: GameModel) extends Observable {
  private var view: GameView = uninitialized
  private val observers: ListBuffer[Observer] = ListBuffer()
  
  
  override def registerObserver(o: Observer): Unit = {
    observers += o
    o match {
      case gameView: GameView => view = gameView
      case _ =>
    }
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())
  
  def initGame(): Unit = {
    val numPlayers = view.getNum
    val playersList = (1 to numPlayers).map(view.getPlayerName).toList

    model.setupPlayers(playersList)

    model.dealCards()
    
    view.initGrid(model.players)

    model.setupTurnOrder()

    view.startGamePrompt(model.currentPlayer)

    notifyObservers()
    
    playGameLoop()
  }
  
  def playGameLoop(): Unit = {

    val player = model.currentPlayer
    var outcome = Outcomes.Invalid
    if (model.isFirstTurn) {
      model.roundRank = view.callRank(model.validRanks)
      playAction(player)
      outcome = Played
    }
    val prevPlayer = model.getPrevPlayer()
    outcome = playerTurnAction(player, prevPlayer)
    model.setNextPlayer(outcome)

    notifyObservers()

    playGameLoop()
  }

  def playerTurnAction(player: Player, prevPlayer: Player): Outcomes = {
    val callsLie = view.readYesNo(prevPlayer)
    if (callsLie) {
      val outcome = model.evaluateReveal()
      outcome match {
        case Outcomes.ChallengedLieWon => {
          view.challengerWonMessage(player, prevPlayer)
        }
        case Outcomes.ChallengedLieLost => {
          view.challengerLostMessage(player, prevPlayer)
        }
      }
      outcome
    } else {
      playAction(player)
      Played
    }
  }

  def playAction(player: Player): Unit = {
    view.displayPlayerHand(player)
    val cardIndices = view.selectCards(player)
    val selected = model.playCards(cardIndices)
    view.printLayedCards(player, selected)
  }

  def getCurrentPlayers = model.players
  def getDiscardedCount = model.discardedCards.length
  def getCurrentPlayer = model.currentPlayer
}
