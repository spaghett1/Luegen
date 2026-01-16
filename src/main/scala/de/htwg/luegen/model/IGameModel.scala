package de.htwg.luegen.model

import de.htwg.luegen.TurnState
import de.htwg.luegen.model.impl1.{Card, Player}

trait IGameModel {
  def playCards(selIndices: List[Int]): IGameModel
  def playerTurn(callsLie: Boolean): IGameModel
  def setNextPlayer(): IGameModel
  def setPlayerCount(count: Int): IGameModel
  def setupPlayers(list: List[String]): IGameModel
  def setupRank(rank: String): IGameModel
  def setupTurnOrder(): IGameModel
  def dealCards(): IGameModel
  def addLog(entry: String): IGameModel
  
  def setError(error: String): IGameModel
  def getError(): Option[String]
  def clearError(): IGameModel
  
  def createMemento(): Memento
  def restoreMemento(memento: Memento): IGameModel
  
  def getPlayerCount: Int
  def getTurnState: TurnState
  def getPlayers: List[Player]
  def getCurrentPlayerIndex: Int
  def getRoundRank: String
  def getLastInputError: Option[String]
  def getLogHistory: List[String]
  def getDiscardedCards: List[Card]
  def getPrevPlayer: Player
  def getValidRanks: List[String]
  def getPlayedCards: List[Card]
  def isFirstTurn: Boolean
}
 