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

  def handleRawInput(rawInput: String): GameModel = {
    model = rawInput.toLowerCase.trim match {
      case "undo" => undo()
      case "redo" => redo()
      case _ =>
        model.turnState match {
          case NeedsPlayerCount => validatePlayerCount(rawInput)
          case NeedsPlayerNames => validatePlayerNames(rawInput)
          case NeedsRankInput =>  validateRank(rawInput)
          case NeedsCardInput => validateCardInput(rawInput)
          case NeedsChallengeDecision => validateChallengeDecision(rawInput)
          case _ => model
        }
    }
    notifyObservers()
    model
  }

  def validatePlayerCount(input: String): GameModel = {
    input.toIntOption match {
      case Some(n) if n > 0 && n < 9 =>
        executeCommand(SetupPlayerCountCommand(n))
      case _ =>
        model.copy(
          lastInputError = Some("Gebe eine gueltige Zahl ein!")
        )
    }
  }

  def validatePlayerNames(input: String): GameModel = {
    val names = input.split(",").map(_.trim).toList
    val isValidCount = names.size == model.playerCount
    val isValidName = names.nonEmpty && names.forall(name => name.nonEmpty && name.length <= 10)
    if (isValidName && isValidCount) {
      executeCommand(SetupPlayersCommand(names))
    } else {
      model.copy(
        lastInputError = Some(s"Gebe ${model.playerCount} Namen durch Komma getrennt ein! (max 10 Zeichen)")
      )
    }
  }

  def validateRank(input: String): GameModel = {
    if (model.validRanks.contains(input)) {
      executeCommand(HandleRoundRankCommand(input))
    } else {
      model.copy(
        lastInputError = Some("Gebe einen gueltigen Rang ein!")
      )
    }
  }

  def validateCardInput(input: String): GameModel = {
    val selIndicesOpt = Try {
      input.split(",").map(_.trim.toInt).toList
    }.toOption

    selIndicesOpt match {
      case Some(selIndices) =>
        val player = model.players(model.currentPlayerIndex)
        val playerHandSize = player.hand.size

        val isValidQuantity = selIndices.size >= 1 && selIndices.size <= 3
        val isValidRange = selIndices.forall(i => i >= 1 && i <= playerHandSize)

        if (isValidQuantity && isValidRange) {
          executeCommand(HandleCardPlayCommand(selIndices))
        } else {
          model.copy(
            lastInputError = Some("Gebe gueltige Indices ein! (max 3 Indices)")
          )
        }
      case _ =>
        model.copy(
          lastInputError = Some("Gebe gueltige Zahlen ein!")
        )
    }
  }

  def validateChallengeDecision(input: String): GameModel = {
    if (input == "j") {
      executeCommand(HandleChallengeDecisionCommand(true))
    } else if (input == "n") {
      executeCommand(HandleChallengeDecisionCommand(false))
    } else {
      model.copy(
        lastInputError = Some("Gebe 'j' oder 'n' ein!")
      )
    }
  }
  
  def initGame(): GameModel = {
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
