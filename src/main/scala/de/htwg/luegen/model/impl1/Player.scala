package de.htwg.luegen.model.impl1

import de.htwg.luegen.model.impl1.Card

case class Player(name: String = "", hand: List[Card] = Nil, playerType: PlayerType = Human, discardedQuartets: List[String] = Nil) {
  import de.htwg.luegen.model.impl1.Card.*
  
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

  def discardQuartets(): (Player, List[Card]) = {
    val grouped = hand.groupBy(_.rank)
    // Finde Ränge, von denen 4 Karten vorhanden sind
    val quartetRanks = grouped.filter(_._2.size == 4).keys.toList

    if (quartetRanks.isEmpty) {
      (this, Nil)
    } else {
      val quartets = grouped.filter(_._2.size == 4).values.flatten.toList
      val newHand = hand.filterNot(quartets.contains)
      // Erstelle neuen Player mit reduzierter Hand und aktualisierter Quartett-Liste
      (this.copy(hand = newHand, discardedQuartets = discardedQuartets ++ quartetRanks), quartets)
    }
  }

  def toXml: scala.xml.Elem  = {
    <player>
      <name> {name} </name>
      <type> {playerType.toString} </type>
      <hand> {hand.mkString(",")} </hand>
    </player>
  }

}

object Player {
  import play.api.libs.json._
  implicit val playerFormat: Format[Player] = Json.format[Player]
}
