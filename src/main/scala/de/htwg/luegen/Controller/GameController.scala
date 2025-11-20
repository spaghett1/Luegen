package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.Outcomes
import de.htwg.luegen.Outcomes.{ChallengedLieLost, ChallengedLieWon, Invalid, Played}

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec

case class ActionDetails(
  isGameStart: Boolean = false,
  playedPlayer: Option[Player] = None,
  playedCards: List[Card] = Nil
)

class GameController(val model: GameModel) extends Observable {
  private var view: GameView = uninitialized
  private val observers: ListBuffer[Observer] = ListBuffer()

  var lastActionOutcome = Outcomes.Invalid
  var lastActionDetails: Option[ActionDetails] = None

  override def registerObserver(o: Observer): Unit = {
    observers += o
    o match {
      case gameView: GameView => view = gameView
      case _ =>
    }
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())
  
  def initGame(): Unit = {
    notifyObservers()
  }
  def setupGame(numPlayers: Int, names: List[String]): Unit = {
    model.setupPlayers(names)
    model.dealCards()

    view.initGrid(model.players)

    model.setupTurnOrder()

    lastActionOutcome = Outcomes.Played

    lastActionDetails = Some(ActionDetails(isGameStart = true))

    notifyObservers()
  }

  def handleRoundRank(rank: String): Unit = {
    model.roundRank = rank
    lastActionOutcome = Outcomes.Played
    lastActionDetails = None
    notifyObservers()
  }

  def handleCardPlay(cardIndices: List[Int]): Unit = {
    val player = model.currentPlayer
    val selected = model.playCards(cardIndices)

    lastActionOutcome = Played
    lastActionDetails = Some(ActionDetails(
      playedPlayer = Some(player),
      playedCards = selected
    ))

    model.setNextPlayer(Played)
    notifyObservers()
  }

  def handleChallengeDecision(callsLie: Boolean): Unit = {
    val outcome = if (callsLie) {
      val result = model.evaluateReveal()
      result
    } else {
      Played
    }

    lastActionOutcome = outcome
    lastActionDetails = None

    model.setNextPlayer(outcome)
    notifyObservers()
  }

  def getLastActionOutcome = lastActionOutcome
  def getLastActionDetails = lastActionDetails

  def getCurrentPlayers = model.players
  def getDiscardedCount = model.discardedCards.length
  def getCurrentPlayer = model.currentPlayer
  def getPrevPlayer = model.getPrevPlayer()
  def isValidRanks = model.validRanks
  def isFirstTurn = model.isFirstTurn
  def getRoundRank = model.roundRank
}
