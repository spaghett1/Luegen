package de.htwg.luegen.controller

import de.htwg.luegen.TurnState
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.{Card, Player, PlayerType}

trait IGameController extends Observable {
  def initGame(): IGameModel
  def handlePlayerCount(count: Int): IGameModel
  def handlePlayerNames(names: List[String]): IGameModel
  def handleRoundRank(rank: String): IGameModel
  def handleCardInput(selection: List[Int]): IGameModel
  def handleChallengeDecision(decision: Boolean): IGameModel
  def setNextPlayer(): IGameModel
  def handleError(e: Throwable): IGameModel
  def undo(): IGameModel
  def redo(): IGameModel
  
  
  def getPlayerCount: Int
  def getCurrentPlayers: List[Player]
  def getDiscardedCount: Int
  def getCurrentPlayer: Player
  def getCurrentPlayerType: PlayerType
  def getPrevPlayer: Player
  def isValidRanks: List[String]
  def getIsFirstTurn: Boolean
  def getRoundRank: String
  def getTurnState: TurnState
  def getPlayedCards: List[Card]
  def getInputError: Option[String]
  def getLog: List[String]
  

}
