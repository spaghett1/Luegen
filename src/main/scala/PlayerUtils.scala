object PlayerUtils {
  
  import InputUtils._
  import DeckUtils._

  def initPlayers(): List[Player] = {
    val numPlayers = getNum

    (1 to numPlayers).map { i =>
      Player(getPlayerName(i))
    }.toList
  }

  def dealCards(players: List[Player], deck: List[Card]): Unit = {
    deck.zipWithIndex.foreach { case (card, i) =>
      val player = players(i % players.length)
      player.addCards(List(card))
    }
  }
}
