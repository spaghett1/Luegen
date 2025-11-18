package de.htwg.luegen.Model

import scala.collection.mutable
import scala.util.Random
import de.htwg.luegen.Model.Player
import de.htwg.luegen.Model.Utils.*
import de.htwg.luegen.Outcomes
import de.htwg.luegen.Outcomes.*

class GameModel {
  var discardedCards: mutable.Stack[Card] = mutable.Stack.empty
  var roundRank = ""
  var amountPlayed = 0
  var players: List[Player] = List.empty 
  var playOrder: List[Int] = List.empty
  var currentPlayer: Player = Player()
  var validRanks = List("2","3","4","5","6","7","8","9","10","B","D","K","A")
  
  
  def setupPlayers(list: List[String]) =  {
    players = list.map(name => Player(name))
  }
  
  def dealCards() = {
    val deck = DeckUtils.shuffle(DeckUtils.createDeck())
    deck.zipWithIndex.foreach { case (card, i) =>
      val player = players(i % players.length)
      player.addCards(List(card))
    }
  } 
  
  def setupTurnOrder() = {
    val validOrder = TurnOrderUtils.mapOrderToPlayerCount(players)
    val startIndex = Random.nextInt(validOrder.size)
    playOrder = TurnOrderUtils.getOrderWithStartIndex(validOrder, startIndex)
    currentPlayer = players(playOrder.head)
  }
  
  def isFirstTurn: Boolean = roundRank == ""

  def playCards(selIndices: List[Int]): List[Card] = {
    val selection = selIndices.map(p => currentPlayer.hand(p - 1))
    discardedCards.pushAll(selection)
    currentPlayer.removeCards(selection)
    amountPlayed = selection.size
    selection
  }

  def getPrevPlayer(): Player = {
    val currentIndex = players.indexOf(currentPlayer)
    val orderSize = playOrder.size
    val pIndexInOrder = (currentIndex - 1 + orderSize) % orderSize
    val pIndexInModel = playOrder(pIndexInOrder)
    players(pIndexInModel)
  }

  def evaluateReveal(): Outcomes = {
    val prevPlayer = getPrevPlayer()
    val lied = discardedCards.take(amountPlayed).exists(_.rank != roundRank)

    if (lied) {
      drawAll(prevPlayer)
      Outcomes.ChallengedLieWon
    } else {
      drawAll(currentPlayer)
      Outcomes.ChallengedLieLost
    }
  }

  def drawAll(player: Player): Unit = {
    player.addCards(discardedCards.toList)
    discardedCards.clear()
  }

  def setNextPlayer(outcome: Outcomes): Unit = {
    val orderSize = playOrder.size
    val currentIndexInModel = players.indexOf(currentPlayer)
    val currentIndexInOrder = playOrder.indexOf(currentIndexInModel)
    var pIndex = 0
    outcome match {
      case Played | ChallengedLieLost | Invalid  => {
        pIndex = (currentIndexInOrder + 1) % orderSize
        val pIndexInModel = playOrder(pIndex)
        currentPlayer = players(pIndexInModel)
      }
      case ChallengedLieWon =>
    }
  }
}
