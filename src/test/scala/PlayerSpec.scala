package de.htwg.luegen.Model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  val card1 = Card("H", "10") // "H10" (Länge 3)
  val card2 = Card("S", "K")  // "SK" (Länge 2)
  val card3 = Card("D", "2")  // "D2" (Länge 2)

  "A Player" when {
    "managing its hand" should {
      val player = Player("Test")

      "Karten korrekt hinzufügen" in {
        player.addCards(List(card1, card2))
        player.hand should contain theSameElementsAs List(card1, card2)
      }

      "Karten korrekt entfernen und die entfernten Karten zurückgeben" in {
        player.hand = List(card1, card2, card3)
        val removed = player.removeCards(List(card1, card3))

        removed should contain theSameElementsAs List(card1, card3)
        player.hand should contain theSameElementsAs List(card2)

        // Versuch, eine nicht vorhandene Karte zu entfernen
        player.removeCards(List(card1)) shouldBe empty
      }
    }

    "queried for card data" should {
      "longestCardName korrekt zurückgeben" in {
        val playerWithCards = Player("Test", List(card2, card1, card3)) // Längen: 2, 3, 2
        playerWithCards.longestCardName shouldBe 3
      }

      "longestCardName sollte 0 für leere Hand zurückgeben" in {
        val playerWithoutCards = Player("Empty")
        playerWithoutCards.longestCardName shouldBe 0
      }

      "hasCards und cardCount korrekt widerspiegeln" in {
        val player = Player("Test", List(card1))
        player.hasCards shouldBe true
        player.cardCount shouldBe 1

        val emptyPlayer = Player("Empty")
        emptyPlayer.hasCards shouldBe false
        emptyPlayer.cardCount shouldBe 0
      }
    }
  }
}
