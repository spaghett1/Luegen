package luegen

case class Player(name: String, var hand: List[Card] = Nil) {
  import Card.*
  
  def addCards(cards: List[Card]): Unit = {
    hand = hand ++ cards
  }
  
  def removeCards(cards: List[Card]): List[Card] = {
    val (removed, kept) = hand.partition(cards.contains)
    hand = kept
    removed
  }
  
  def longestCardName: Int = {
    if (hasCards) {
      hand.map(_.toString.length).max
    } else {
      0
    }
  }
  
  def playSelectedCards(cards: List[Card]): Unit = {
    if (cards.isEmpty) return
    removeCards(cards)
    GameData.discardedCards.pushAll(cards)
    GameData.amountPlayed = cards.length
    InputUtils.playerLaysCards(this, cards)
  }
  
  def hasCards: Boolean = hand.nonEmpty
  
  def cardCount: Int = hand.size
}
