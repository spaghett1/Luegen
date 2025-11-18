package de.htwg.luegen.Model

case class Card(suit: String, rank: String) {
  override def toString: String = s"$suit$rank"
}
