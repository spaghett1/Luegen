package de.htwg.luegen.model

import de.htwg.luegen.TurnState
import de.htwg.luegen.TurnState.*
import de.htwg.luegen.model.impl1.{Card, Player}


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
) {

  def toXml: scala.xml.Elem = {
    <memento>
      <discardedCards> {discardedCards} </discardedCards>
      <roundRank> {roundRank} </roundRank>
      <lastPlayedCards> {lastPlayedCards} </lastPlayedCards>
      <playerCount> {playerCount} </playerCount>
      <players> {players.map(_.toXml)} </players>
      <currentPlayerIndex> {currentPlayerIndex} </currentPlayerIndex>
      <lastPlayerIndex> {lastPlayerIndex} </lastPlayerIndex>
      <validRanks> {validRanks } </validRanks>
      <playOrder> {playOrder.mkString(",")} </playOrder>
      <turnState> {turnState} </turnState>
      <amountPlayed> {amountPlayed} </amountPlayed>
      <lastAccusedIndex> {lastAccusedIndex} </lastAccusedIndex>
    </memento>
  }
}

object Memento {
  import play.api.libs.json._
  implicit val mementoFormat: Format[Memento] = Json.format[Memento]
}


