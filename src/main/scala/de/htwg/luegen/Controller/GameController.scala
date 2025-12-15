package de.htwg.luegen.Controller

import de.htwg.luegen.View.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.TurnState.*
import de.htwg.luegen.Model.Utils.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.annotation.tailrec
import scala.Option.*
import scala.util.Try

case class HistoryEntry(model: Memento, command: GameCommand)

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
    undoStack += HistoryEntry(model.createMemento(), command)
    model = decoratedCommand.execute(model)
    redoStack.clear()
    notifyObservers()
    model
  }

  def undo(): GameModel = {
    undoStack.lastOption match {
      case Some(HistoryEntry(memento, command)) =>
        redoStack += HistoryEntry(model.createMemento(), command)
        undoStack.remove(undoStack.size - 1)

        model = model.restoreMemento(memento)

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
        undoStack += HistoryEntry(model.createMemento(), command)
        redoStack.remove(redoStack.size - 1)

        model = model.restoreMemento(memento)

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

  def handlePlayerCount(count: Int): GameModel = {
    executeCommand(SetupPlayerCountCommand(count))
  }

  def handlePlayerNames(names: List[String]): GameModel = {
    executeCommand(SetupPlayersCommand(names))
  }

  def handleRoundRank(rank: String): GameModel = {
    executeCommand(HandleRoundRankCommand(rank))
  }

  def handleCardInput(selection: List[Int]): GameModel = {
    executeCommand(HandleCardPlayCommand(selection))
  }

  def handleChallengeDecision(decision: Boolean): GameModel = {
    executeCommand(HandleChallengeDecisionCommand(decision))
  }

  def handleError(e: Throwable): GameModel = {
    val msg = e match {
      case _: NumberFormatException => "Ungueltige Eingabe! Bitte geben sie Zahlen ein!"
      case _ => e.getMessage
    }
    model = model.copy(
      lastInputError = Some(msg)
    )
    notifyObservers()
    model
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
  def getInputError = model.lastInputError
  def getLog = model.logHistory
}
