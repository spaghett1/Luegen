package de.htwg.luegen.model

case class Card(suit: String, rank: String) {
  override def toString: String = s"$suit$rank"
}
