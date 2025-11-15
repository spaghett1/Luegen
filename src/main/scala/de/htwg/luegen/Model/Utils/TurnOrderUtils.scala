package de.htwg.luegen.Model.Utils

import de.htwg.luegen.Model.{Outcomes, Player}

object TurnOrderUtils {
  private val baseOrder = List(1,5,4,8,6,3,7)

  def validOrder(players: List[Player]): List[Int] = {
    baseOrder.filter(_ <= players.size).map(_ - 1)
  }

  def determinePlayOrder(validOrder: List[Int], startIndex: Int): List[Int] = {
    validOrder.drop(startIndex) ++ validOrder.take(startIndex)
  }

  def nextPlayerIndex(outcome: Outcomes, currentIndex: Int, total: Int): Int = outcome match {
    case Outcomes.Played => {
      println(s"currIndex: ${currentIndex}")
      val nextIndex = (currentIndex + 1)
      println(s"nextIndex: ${nextIndex}")
      nextIndex
    }
    case Outcomes.ChallengedLieWon => currentIndex
    case Outcomes.ChallengedLieLost => (currentIndex + 1) % total
    case _ => currentIndex
  }


}
