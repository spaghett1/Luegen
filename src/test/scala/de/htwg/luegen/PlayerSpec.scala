package de.htwg.luegen

import de.htwg.luegen.Model.{Card, Player}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.luegen.Model
import de.htwg.luegen.Model.Human

class PlayerSpec extends AnyWordSpec with Matchers {

  val card1 = Card("H", "10") // "H10" (Länge 3)
  val card2 = Card("S", "K")  // "SK" (Länge 2)
  val card3 = Card("D", "2")  // "D2" (Länge 2)

  "A Player (Functional)" when {

    "initialized with default values" should {
      "set correct defualt values (emtpy name, empty hand)" in {
        val defaultPlayer = Player()
        defaultPlayer.name shouldBe ""
        defaultPlayer.hand shouldBe empty
        defaultPlayer.playerType shouldBe Human
      }
    }
    "managing its hand" should {

      "addCards sollte ein NEUES Player-Objekt zurückgeben und die Hand erweitern" in {
        val player = Player("Test", List(card1))

        val newPlayer = player.addCards(List(card2))

        // NEUER Zustand
        newPlayer.hand should contain theSameElementsAs List(card1, card2)

        // IMMUTABILITY: Alter Zustand muss unberührt sein
        player.hand should contain theSameElementsAs List(card1)
        player should not be newPlayer
      }

      "removeCards sollte ein NEUES Player-Objekt mit reduzierter Hand zurückgeben" in {
        val player = Player("Test", List(card1, card2, card3))

        val (newPlayer, removedCards) = player.removeCards(List(card1, card3))

        // NEUER Zustand
        removedCards should contain theSameElementsAs List(card1, card3)
        newPlayer.hand should contain theSameElementsAs List(card2)

        // IMMUTABILITY: Alter Zustand muss unberührt sein
        player.hand should contain theSameElementsAs List(card1, card2, card3)
        player should not be newPlayer
      }
    }

    "queried for card data" should {
      "longestCardName korrekt zurückgeben" in {
        val playerWithCards = Player("Test", List(card2, card1, card3))
        playerWithCards.longestCardName shouldBe 3
      }

      "longestCardName should return 0 when no data" in {
        val player = Player("Test", Nil)
        player.longestCardName shouldBe 0
      }

      "hasCards und cardCount korrekt widerspiegeln" in {
        val player = Player("Test", List(card1))
        player.hasCards shouldBe true
        player.cardCount shouldBe 1
      }
    }
  }
}