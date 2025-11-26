package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec

class GameController(var model: GameModel) extends Observable {
  private val observers: ListBuffer[Observer] = ListBuffer()

  override def registerObserver(o: Observer): Unit = {
    observers += o
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())
  
  def initGame(): GameModel = {
    notifyObservers()
    model
  }
  def setupGame(numPlayers: Int, names: List[String]): GameModel = {
    model = model.setupPlayers(names)
    model = model.dealCards()
    model = model.setupTurnOrder()

    notifyObservers()
    model
  }

  def handleRoundRank(rank: String): GameModel = {
    model = model.copy(roundRank = rank)
    notifyObservers()
    model
  }

  def handleCardPlay(cardIndices: List[Int]): GameModel = {
    model  = model.playCards(cardIndices)
    model = model.setNextPlayer()
    notifyObservers()
    model
  }

  def handleChallengeDecision(callsLie: Boolean): GameModel = {
    model = model.playerTurn(callsLie)
    if (model.turnState != Played) {
      model = model.setNextPlayer()
    }
    notifyObservers()
    model
  }

  def getCurrentPlayers = model.players
  def getDiscardedCount = model.discardedCards.length
  def getCurrentPlayer = model.players(model.currentPlayerIndex)
  def getPrevPlayer = model.getPrevPlayer()
  def isValidRanks = model.validRanks
  def isFirstTurn = model.isFirstTurn
  def getRoundRank = model.roundRank
  def getTurnState = model.turnState
  def getPlayedCards = model.lastPlayedCards
}
