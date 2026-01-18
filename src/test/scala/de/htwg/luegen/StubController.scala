package de.htwg.luegen

import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.{Card, Player, PlayerType}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.TurnState
import de.htwg.luegen.controller.Observer

class StubController extends IGameController {
  var lastCalledMethod: String = ""
  var lastPlayerCount: Int = 0
  var lastPlayerNames: List[String] = Nil
  var lastRoundRank: String = ""
  var lastCardSelection: List[Int] = Nil
  var lastChallengeDecision: Option[Boolean] = None
  var lastErrorMsg: String = ""

  var mockPlayerCount: Int = 2
  var mockCurrentPlayer: Player = Player("Alice", Nil, Human)
  var mockCurrentPlayerType: PlayerType = Human
  var mockPrevPlayer: Player = Player("Bob", Nil, Human)
  var mockValidRanks: List[String] = List("10", "A")
  var mockInputError: Option[String] = None
  var mockTurnState: TurnState = TurnState.NoTurn
  var currentPlayers: List[Player] = Nil

  override def handlePlayerCount(count: Int): IGameModel = {
    lastCalledMethod = "handlePlayerCount"
    lastPlayerCount = count
    null
  }

  override def handlePlayerNames(names: List[String]): IGameModel = {
    lastCalledMethod = "handlePlayerNames"
    lastPlayerNames = names
    null
  }

  override def handleRoundRank(rank: String): IGameModel = {
    lastCalledMethod = "handleRoundRank"
    lastRoundRank = rank
    null
  }

  override def handleCardInput(selection: List[Int]): IGameModel = {
    lastCalledMethod = "handleCardInput"
    lastCardSelection = selection
    null
  }

  override def handleChallengeDecision(decision: Boolean): IGameModel = {
    lastCalledMethod = "handleChallengeDecision"
    lastChallengeDecision = Some(decision)
    null
  }

  override def handleError(e: Throwable): IGameModel = {
    lastCalledMethod = "handleError"
    lastErrorMsg = e.getMessage
    null
  }

  override def setNextPlayer(): IGameModel = {
    lastCalledMethod = "setNextPlayer"
    null
  }

  override def undo(): IGameModel = { lastCalledMethod = "undo"; null }
  override def redo(): IGameModel = { lastCalledMethod = "redo"; null }
  override def save: Unit = { lastCalledMethod = "save" }
  override def load: Unit = { lastCalledMethod = "load" }

  override def getPlayerCount: Int = mockPlayerCount
  override def getCurrentPlayer: Player = mockCurrentPlayer
  override def getCurrentPlayerType: PlayerType = mockCurrentPlayerType
  override def getPrevPlayer: Player = mockPrevPlayer
  override def isValidRanks: List[String] = mockValidRanks
  override def getInputError: Option[String] = mockInputError
  override def getTurnState: TurnState = mockTurnState

  override def registerObserver(o: Observer): Unit = {}
  override def notifyObservers(): Unit = {}
  override def initGame(): IGameModel = null
  override def getRoundRank: String = ""
  override def getDiscardedCount: Int = 0
  override def getPlayedCards: List[Card] = Nil
  override def getIsFirstTurn: Boolean = false
  override def getLog: List[String] = Nil
  override def getCurrentPlayers: List[Player] = currentPlayers

  override def getAllDiscardedQuartets: List[String] = Nil
}
