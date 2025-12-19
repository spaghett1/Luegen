package de.htwg.luegen

import de.htwg.luegen.model.impl1.Player
import de.htwg.luegen.model.impl1.Utils.TurnOrderUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TurnOrderUtilsSpec extends AnyWordSpec with Matchers {

  val pList8 = (1 to 8).map(i => Player(s"P$i")).toList
  val pList3 = (1 to 3).map(i => Player(s"P$i")).toList

  "TurnOrderUtils" should {

    "mapOrderToPlayerCount" should {
      "die Basisreihenfolge für 8 Spieler korrekt abbilden" in {
        val expectedIndices = List(0, 4, 3, 7, 1, 5, 2, 6)
        TurnOrderUtils.mapOrderToPlayerCount(pList8) should contain theSameElementsInOrderAs expectedIndices
      }

      "die Basisreihenfolge für 3 Spieler korrekt filtern und abbilden" in {
        // Filtered (<= 3): [1, 3] -> Indizes (0-basiert): [0, 2]
        val expectedIndices = List(0, 1, 2)
        TurnOrderUtils.mapOrderToPlayerCount(pList3) should contain theSameElementsInOrderAs expectedIndices
      }
    }

    "getOrderWithStartIndex" should {
      val validOrder = List(0, 4, 3, 7, 1, 5, 2, 6)

      "die Liste korrekt rotieren (Start nicht 0)" in {
        // Start bei Index 2 (Element '3')
        TurnOrderUtils.getOrderWithStartIndex(validOrder, 1) should contain theSameElementsInOrderAs List(4, 3, 7, 1, 5, 2, 6, 0)
      }

      "die Liste bei Index 0 nicht rotieren" in {
        TurnOrderUtils.getOrderWithStartIndex(validOrder, 0) should contain theSameElementsInOrderAs validOrder
      }
    }
  }
}
