package de.htwg.luegen.model

trait PlayerType
case object Human extends impl1.PlayerType
case object AI extends impl1.PlayerType

case class Player(name: String = "", hand: List[Card] = Nil, playerType: impl1.PlayerType = Human) {
  import de.htwg.luegen.model.impl1.Card.*
  
  def addCards(cards: List[Card]): impl1.Player = {
    this.copy(hand = hand ++ cards)
  }
  
  def removeCards(cards: List[Card]): (impl1.Player, List[Card]) = {
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
