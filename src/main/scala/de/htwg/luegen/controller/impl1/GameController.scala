package de.htwg.luegen.controller.impl1

import de.htwg.luegen.controller.*
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.fileIO.IFileIO

import scala.collection.mutable.ListBuffer

class GameController(using private var model: IGameModel, fileIo: IFileIO) extends IGameController {
  private val observers: ListBuffer[Observer] = ListBuffer()

  private val undoStack: ListBuffer[HistoryEntry] = ListBuffer()
  private val redoStack: ListBuffer[HistoryEntry] = ListBuffer()

  override def registerObserver(o: Observer): Unit = {
    observers += o
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())

  private def executeCommand(command: GameCommand): IGameModel = {
    val decoratedCommand = LoggingCommandDecorator(command)
    undoStack += HistoryEntry(model.createMemento(), command)
    model = decoratedCommand.execute(model)
    redoStack.clear()
    notifyObservers()
    model
  }

  override def undo(): IGameModel = {
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

  override def redo(): IGameModel = {
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
  
  override def initGame(): IGameModel = {
    notifyObservers()
    model
  }

  override def handlePlayerCount(count: Int): IGameModel = {
    executeCommand(SetupPlayerCountCommand(count))
  }

  override def handlePlayerNames(names: List[String]): IGameModel = {
    executeCommand(SetupPlayersCommand(names))
  }

  override def handleRoundRank(rank: String): IGameModel = {
    executeCommand(HandleRoundRankCommand(rank))
  }

  override def handleCardInput(selection: List[Int]): IGameModel = {
    executeCommand(HandleCardPlayCommand(selection))
  }

  override def handleChallengeDecision(decision: Boolean): IGameModel = {
    executeCommand(HandleChallengeDecisionCommand(decision))
  }
  
  override def setNextPlayer(): IGameModel = {
    executeCommand(SetNextPlayerCommand())
  }

  override def handleError(e: Throwable): IGameModel = {
    val msg = e match {
      case _: NumberFormatException => "Ungueltige Eingabe! Bitte geben sie Zahlen ein!"
      case _ => e.getMessage
    }
    model = model.setError(msg)
    notifyObservers()
    model
  }

  override def getPlayerCount = model.getPlayerCount
  override def getCurrentPlayers = model.getPlayers
  override def getDiscardedCount = model.getDiscardedCards.length
  override def getCurrentPlayer = model.getPlayers(model.getCurrentPlayerIndex)
  override def getCurrentPlayerType = model.getPlayers(model.getCurrentPlayerIndex).playerType
  override def getPrevPlayer = model.getPrevPlayer
  override def getIsFirstTurn = model.isFirstTurn
  override def isValidRanks = model.getValidRanks
  override def getRoundRank = model.getRoundRank
  override def getTurnState = model.getTurnState
  override def getPlayedCards = model.getPlayedCards
  override def getInputError = model.getLastInputError
  override def getLog = model.getLogHistory

  override def save: Unit = {
    fileIo.save(model)
    notifyObservers()
  }

  override def load: Unit = {
    model = fileIo.load
    undoStack.clear()
    redoStack.clear()
    notifyObservers()
  }
}
 