package de.htwg.luegen.model.impl1.Utils

import de.htwg.luegen.model.impl1.{Card, Player}

object DeckUtils {
  val Suits = List("♠", "♥", "♦", "♣")
  val Ranks = List("2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A")

  def createDeck(): List[Card] = {
    for (s <- Suits; r <- Ranks) yield Card(s, r)
  }

  def shuffle(deck: List[Card]): List[Card] = {
    scala.util.Random.shuffle(deck)
  }
}
