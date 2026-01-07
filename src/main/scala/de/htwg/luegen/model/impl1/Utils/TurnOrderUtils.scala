package de.htwg.luegen.model.impl1.Utils

import de.htwg.luegen.model.impl1.Player

object TurnOrderUtils {
  private val baseOrder = List(1,5,4,8,2,6,3,7)

  def mapOrderToPlayerCount(players: List[Player]): List[Int] = {
    baseOrder.filter(_ <= players.size).map(_ - 1)
  }

  def getOrderWithStartIndex(validOrder: List[Int], startIndex: Int): List[Int] = {
    validOrder.drop(startIndex) ++ validOrder.take(startIndex)
  }
}
