package de.htwg.luegen.model.impl1.Utils

import de.htwg.luegen.model.impl1.{Card, Player}
import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*


case class Memento(
  discardedCards: List[Card] = Nil,
  roundRank: String = "",
  lastPlayedCards: List[Card] = Nil,
  playerCount: Int = 0,
  players: List[Player] = List.empty,
  currentPlayerIndex: Int = 0,
  lastPlayerIndex: Int = 0,
  validRanks: List[String] = List("2","3","4","5","6","7","8","9","10","B","D","K","A"),
  playOrder: List[Int] = List.empty,
  turnState: TurnState = NoTurn,
  amountPlayed: Int = 0,
  lastAccusedIndex: Int = 0,
)
