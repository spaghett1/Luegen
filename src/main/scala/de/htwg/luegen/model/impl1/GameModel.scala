package de.htwg.luegen.model.impl1

import de.htwg.luegen.model.impl1.Utils.{DeckUtils, Memento, TurnOrderUtils}
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*

import scala.collection.mutable
import scala.util.Random

case class GameModel(
  discardedCards: List[Card] = Nil,
  roundRank: String = "",
  lastPlayedCards: List[Card] = Nil,
  playerCount: Int = 0,
  players: List[Player] = List.empty,
  currentPlayerIndex: Int = 0,
  lastPlayerIndex: Int = 0,
  validRanks: List[String] = List("2","3","4","5","6","7","8","9","10","B","D","K","A"),
  playOrder: List[Int] = List.empty,
  turnState: TurnState = NeedsPlayerCount,
  amountPlayed: Int = 0,
  lastAccusedIndex: Int = 0,
  lastInputError: Option[String] = None,
  logHistory: List[String] = Nil
) extends IGameModel {

  override def setPlayerCount(num: Int): IGameModel = this.copy(playerCount = num, turnState = NeedsPlayerNames)

  override def setupPlayers(list: List[String]): IGameModel =  {
    this.copy(
      players = list.map(p => Player(name = p, playerType = Human)),
      turnState = NeedsRankInput
    )
  }

  override def dealCards(): IGameModel = {
    val deck = DeckUtils.shuffle(DeckUtils.createDeck())

    val updatedPlayers = deck.zipWithIndex.foldLeft(players) { case (accPlayers, (card, i)) =>
      val playerIndex = i % players.length
      val player = accPlayers(playerIndex)
      val updatedPlayer = player.addCards(List(card))
      accPlayers.updated(playerIndex, updatedPlayer)
    }

    this.copy(players = updatedPlayers)
  }

  override def setupTurnOrder(): IGameModel = {
    val validOrder = TurnOrderUtils.mapOrderToPlayerCount(players)
    val startIndex = Random.nextInt(validOrder.size)
    val newPlayOrder = TurnOrderUtils.getOrderWithStartIndex(validOrder, startIndex)
    this.copy(
      playOrder = newPlayOrder,
      currentPlayerIndex = newPlayOrder.head,
    )
  }

  override def setupRank(rank: String): IGameModel = {
    this.copy(
      roundRank = rank,
      turnState = NeedsCardInput
    )
  }

  override def isFirstTurn: Boolean = roundRank == ""

  override def playCards(selIndices: List[Int]): IGameModel = {
    val player = players(currentPlayerIndex)
    val selection = selIndices.map(p => player.hand(p - 1))
    val (updatedPlayer, removedCards) = player.removeCards(selection)
    val updatedPlayers = (players.updated(currentPlayerIndex, updatedPlayer))
    this.copy(
      players = updatedPlayers,
      discardedCards = removedCards ++ discardedCards,
      amountPlayed = removedCards.size,
      turnState = Played,
      lastPlayedCards = removedCards,
    )
  }

  override def playerTurn(callsLie: Boolean): IGameModel = {
    if (callsLie) {
      evaluateReveal()
    } else {
      this.copy(turnState = NeedsCardInput)
    }
  }

  override def getPrevPlayer: Player = {
    val orderSize = playOrder.size
    val pIndexInOrder = (currentPlayerIndex - 1 + orderSize) % orderSize
    val pIndexInModel = playOrder(pIndexInOrder)
    players(pIndexInModel)
  }

  private def evaluateReveal(): GameModel = {
    val prevPlayer = getPrevPlayer
    val player = players(currentPlayerIndex)
    val lied = lastPlayedCards.exists(_.rank != roundRank)

    val modelAfterDraw = if (lied) {
      drawAll(prevPlayer).copy(
        turnState = ChallengedLieWon
      )
    } else {
      drawAll(player).copy(
        turnState = ChallengedLieLost
      )
    }
    modelAfterDraw.copy(
      lastPlayedCards = Nil,
      amountPlayed = 0,
      lastAccusedIndex = players.indexOf(prevPlayer)
    )
  }

  private def drawAll(player: Player): GameModel = {
    val playerIndex = players.indexOf(player)
    val updated = player.addCards(discardedCards)
    val updatedPlayers = players.updated(playerIndex, updated)
    this.copy(
      players = updatedPlayers,
      discardedCards = List.empty
    )
  }

  override def setNextPlayer(): IGameModel = {
    val lastState = turnState
    val orderSize = playOrder.size
    val currentIndexInOrder = playOrder.indexOf(currentPlayerIndex)
    val (next, newRoundRank) = lastState match {
      case ChallengedLieWon =>
        (currentIndexInOrder, "")
      case ChallengedLieLost =>
        ((currentIndexInOrder + 1) % orderSize, "")
      case _ =>
        ((currentIndexInOrder + 1) % orderSize, this.roundRank)
    }
    val nextInModel = playOrder(next)
    val nextTurnState = if (newRoundRank.isEmpty) NeedsRankInput else NeedsChallengeDecision
    this.copy(
      roundRank = newRoundRank,
      currentPlayerIndex = nextInModel,
      lastPlayerIndex = currentPlayerIndex,
      turnState = nextTurnState
    )
  }

  override def addLog(entry: String): IGameModel = this.copy(logHistory = logHistory :+ entry)

  override def setError(error: String): IGameModel = {
    this.copy(lastInputError = Some(error))
  }
  override def clearError(): IGameModel = this.copy(lastInputError = None)

  override def getPlayerCount: Int = this.playerCount
  override def getTurnState: TurnState = this.turnState
  def getPlayers: List[Player] = this.players
  def getCurrentPlayerIndex: Int = this.currentPlayerIndex
  def getRoundRank: String = this.roundRank
  def getLastInputError: Option[String] = this.lastInputError
  def getLogHistory: List[String] = this.logHistory
  def getDiscardedCards: List[Card] = this.discardedCards
  def getValidRanks: List[String] = this.validRanks
  def getPlayedCards: List[Card] = this.lastPlayedCards
  
  def createMemento(): Memento = {
    Memento(
      discardedCards,
      roundRank,
      lastPlayedCards,
      playerCount,
      players,
      currentPlayerIndex,
      lastPlayerIndex,
      validRanks,
      playOrder,
      turnState,
      amountPlayed,
      lastAccusedIndex,
    )
  } 

  def restoreMemento(memento: Memento): IGameModel = {
    this.copy(
      discardedCards = memento.discardedCards,
      roundRank = memento.roundRank,
      lastPlayedCards = memento.lastPlayedCards,
      playerCount = memento.playerCount,
      players = memento.players,
      currentPlayerIndex = memento.currentPlayerIndex,
      lastPlayerIndex = memento.lastPlayerIndex,
      validRanks = memento.validRanks,
      playOrder = memento.playOrder,
      turnState = memento.turnState,
      amountPlayed = memento.amountPlayed,
      lastAccusedIndex = memento.lastAccusedIndex,
    )
  }
}
