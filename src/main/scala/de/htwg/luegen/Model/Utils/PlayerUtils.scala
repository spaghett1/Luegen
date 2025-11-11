package de.htwg.luegen.Model.Utils

import de.htwg.luegen.Model.{Card, Player}
import de.htwg.luegen.View.InputUtils

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
