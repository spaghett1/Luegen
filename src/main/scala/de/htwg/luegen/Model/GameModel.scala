package de.htwg.luegen.Model

import scala.collection.mutable
import scala.util.Random
import de.htwg.luegen.Model.Player
import de.htwg.luegen.Model.Utils.*
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*

case class GameModel(
  discardedCards: List[Card] = Nil,
  roundRank: String = "",
  lastPlayedCards: List[Card] = Nil,
  players: List[Player] = List.empty,
  currentPlayerIndex: Int = 0,
  lastPlayerIndex: Int = 0,
  validRanks: List[String] = List("2","3","4","5","6","7","8","9","10","B","D","K","A"),
  playOrder: List[Int] = List.empty,
  turnState: TurnState = NoTurn,
  amountPlayed: Int = 0,
  lastAccusedIndex: Int = 0,
) {

  def setupPlayers(list: List[String]): GameModel =  {
    this.copy(players = list.map(p => Player(name = p)))
  }

  def dealCards(): GameModel = {
    val deck = DeckUtils.shuffle(DeckUtils.createDeck())

    // Karten aus dem Deck verteilen
    val updatedPlayers = deck.zipWithIndex.foldLeft(players) { case (accPlayers, (card, i)) =>
      val playerIndex = i % players.length
      val player = accPlayers(playerIndex)
      val updatedPlayer = player.addCards(List(card))
      accPlayers.updated(playerIndex, updatedPlayer)
    }

    this.copy(players = updatedPlayers)
  }

  def setupTurnOrder() = {
    val validOrder = TurnOrderUtils.mapOrderToPlayerCount(players)
    val startIndex = Random.nextInt(validOrder.size)
    val newPlayOrder = TurnOrderUtils.getOrderWithStartIndex(validOrder, startIndex)
    this.copy(
      playOrder = newPlayOrder,
      currentPlayerIndex = newPlayOrder.head,
      turnState = TurnState.NoTurn
    )
  }

  def setupRank(rank: String): GameModel = {
    this.copy(
      roundRank = rank,
      turnState = NoChallenge
    )
  }

  def isFirstTurn: Boolean = roundRank == ""

  def playCards(selIndices: List[Int]): GameModel = {
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

  def playerTurn(callsLie: Boolean): GameModel = {
    if (callsLie) {
      evaluateReveal()
    } else {
      this.copy(turnState = NoChallenge)
    }
  }

  def getPrevPlayer(): Player = {
    val orderSize = playOrder.size
    val pIndexInOrder = (currentPlayerIndex - 1 + orderSize) % orderSize
    val pIndexInModel = playOrder(pIndexInOrder)
    players(pIndexInModel)
  }

  def evaluateReveal(): GameModel = {
    val prevPlayer = getPrevPlayer()
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

  def drawAll(player: Player): GameModel = {
    val playerIndex = players.indexOf(player)
    val updated = player.addCards(discardedCards)
    val updatedPlayers = players.updated(playerIndex, updated)
    this.copy(
      players = updatedPlayers,
      discardedCards = List.empty
    )
  }

  def setNextPlayer(): GameModel = {
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
    this.copy(
      roundRank = newRoundRank,
      currentPlayerIndex = nextInModel,
      lastPlayerIndex = currentPlayerIndex,
      turnState = NoTurn
    )
  }
}
