package luegen

object PlayerUtils {
  
  import DeckUtils.*
  import InputUtils.*

  def initPlayers(): List[Player] = {
    val numPlayers = InputUtils.getNum

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
