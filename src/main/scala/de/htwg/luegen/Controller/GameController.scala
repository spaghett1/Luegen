package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec

trait GameCommand {
  def execute(model: GameModel): GameModel
}

case class LoggingCommandDecorator(wrappedCommand: GameCommand) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val logEntry = s"Command ausgefuehrt: ${wrappedCommand.getClass.getSimpleName}"
    val modelAfterExecution = wrappedCommand.execute(model)
    modelAfterExecution.addLog(logEntry)
  }
}

case class HandleRoundRankCommand(rank: String) extends GameCommand {
  override def execute(model: GameModel): GameModel = model.setupRank(rank)
}

case class HandleCardPlayCommand(cardIndices: List[Int]) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val modelAfterPlay = model.playCards(cardIndices)
    modelAfterPlay.setNextPlayer()
  }
}

case class HandleChallengeDecisionCommand(callsLie: Boolean) extends GameCommand {
  override def execute(model: GameModel): GameModel = model.playerTurn(callsLie)
}

class GameController(var model: GameModel) extends Observable {
  private val observers: ListBuffer[Observer] = ListBuffer()

  override def registerObserver(o: Observer): Unit = {
    observers += o
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())

  private def executeCommand(command: GameCommand): GameModel = {
    val decoratedCommand = LoggingCommandDecorator(command)
    model = decoratedCommand.execute(model)
    notifyObservers()
    model
  }
  
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
    executeCommand(HandleRoundRankCommand(rank))
  }

  def handleCardPlay(cardIndices: List[Int]): GameModel = {
    executeCommand(HandleCardPlayCommand(cardIndices))
  }

  def handleChallengeDecision(callsLie: Boolean): GameModel = {
    executeCommand(HandleChallengeDecisionCommand(callsLie))
  }

  def getCurrentPlayers = model.players
  def getDiscardedCount = model.discardedCards.length
  def getCurrentPlayer = model.players(model.currentPlayerIndex)
  def getCurrentPlayerType = model.players(model.currentPlayerIndex).playerType
  def getPrevPlayer = model.getPrevPlayer()
  def isValidRanks = model.validRanks
  def isFirstTurn = model.isFirstTurn
  def getRoundRank = model.roundRank
  def getTurnState = model.turnState
  def getPlayedCards = model.lastPlayedCards
  def getLog = model.logHistory
}
