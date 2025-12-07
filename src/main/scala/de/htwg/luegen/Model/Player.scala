package de.htwg.luegen.Model

sealed trait PlayerType
case object Human extends PlayerType
case object AI extends PlayerType

case class Player(name: String = "", hand: List[Card] = Nil, playerType: PlayerType = Human) {
  import Card.*
  
  def addCards(cards: List[Card]): Player = {
    this.copy(hand = hand ++ cards)
  }
  
  def removeCards(cards: List[Card]): (Player, List[Card]) = {
    val (removed, kept) = hand.partition(cards.contains)
    (this.copy(hand = kept), removed)
  }
  
  def longestCardName: Int = {
    if (hasCards) {
      hand.map(_.toString.length).max
    } else {
      0
    }
  }
  
  def hasCards: Boolean = hand.nonEmpty
  
  def cardCount: Int = hand.size
}
