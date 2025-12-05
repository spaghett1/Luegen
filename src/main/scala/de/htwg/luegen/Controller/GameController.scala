package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec
import scala.Option.*

case class HistoryEntry(model: GameModel, command: GameCommand)

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

case object InitCommand extends GameCommand {
  override def execute(model: GameModel): GameModel = model
}

case class SetupPlayerCountCommand(numPlayers: Int) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    model.setPlayerCount(numPlayers)
  }
}

case class SetupPlayersCommand(names: List[String]) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val modelPlayerSetup = model.setupPlayers(names)
    val modelTurnOrderSetup = modelPlayerSetup.setupTurnOrder()
    modelTurnOrderSetup.dealCards()
  }
}

case class HandleRoundRankCommand(rank: String, prevRank: String = "") extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    model.setupRank(rank)
  }
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

  private val undoStack: ListBuffer[HistoryEntry] = ListBuffer()
  private val redoStack: ListBuffer[HistoryEntry] = ListBuffer()

  override def registerObserver(o: Observer): Unit = {
    observers += o
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())

  private def executeCommand(command: GameCommand): GameModel = {
    val decoratedCommand = LoggingCommandDecorator(command)
    undoStack += HistoryEntry(model, command)
    model = decoratedCommand.execute(model)
    redoStack.clear()
    notifyObservers()
    model
  }

  def undo(): GameModel = {
    undoStack.lastOption match {
      case Some(memento, command) =>
        redoStack += HistoryEntry(model, command)
        undoStack.remove(undoStack.size - 1)
        val currentLog = model.logHistory

        model = memento
        model = model.copy(logHistory = currentLog)

        val commandName = command.getClass.getSimpleName.replace("Command", "")
        model = model.addLog(s"UNDO: Undid $commandName")

        notifyObservers()
        model

      case None => model
    }
  }

  def redo(): GameModel = {
    redoStack.lastOption match {
      case Some(HistoryEntry(memento, command)) =>
        undoStack += HistoryEntry(model, command)
        redoStack.remove(redoStack.size - 1)
        val currentLog = model.logHistory

        model = memento
        model = model.copy(logHistory = currentLog)

        val commandName = command.getClass.getSimpleName.replace("Command", "")
        model = model.addLog(s"REDO: Redid $commandName")
        notifyObservers()
        model
      case None =>
        model
    }
  }
  
  def initGame(): GameModel = {
    notifyObservers()
    model
  }
  
  def setupPlayerCount(numPlayers: Int) = {
    
    executeCommand(SetupPlayerCountCommand(numPlayers))
    notifyObservers()
    model
    
  }

  def setupPlayers(names: List[String]): GameModel = {
    
    executeCommand(SetupPlayersCommand(names))
    

    notifyObservers()
    model
  }

  def handleRoundRank(rank: String): GameModel = {
    if (rank == "2") {
      undo()
    } else {
      executeCommand(HandleRoundRankCommand(rank))
    }
  }

  def handleCardPlay(cardIndices: List[Int]): GameModel = {

    executeCommand(HandleCardPlayCommand(cardIndices))
  }

  def handleChallengeDecision(callsLie: Boolean): GameModel = {
    executeCommand(HandleChallengeDecisionCommand(callsLie))
  }
  
  def getPlayerCount = model.playerCount
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
