case class Card(suit: String, rank: String) {
  override def toString: String = s"$suit$rank"
}

case class Player(
  name: String,
  hand: List[Card] = Nil
) {
  def addCards(list: List[Card]): Player = {
    this.copy(hand = list)
  }
}

val cards = List(Card("D", "K"), Card("C", "D"))
val player = Player("Alice")
val updatedPlayer = player.addCards(cards)
println(updatedPlayer.hand.mkString(","))