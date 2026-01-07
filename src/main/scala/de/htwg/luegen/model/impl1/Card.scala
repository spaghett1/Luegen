package de.htwg.luegen.model.impl1

case class Card(suit: String, rank: String) {
  override def toString: String = s"$suit$rank"
}
