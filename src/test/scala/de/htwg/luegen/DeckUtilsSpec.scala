package de.htwg.luegen

import de.htwg.luegen.model.impl1.Utils.DeckUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeckUtilsSpec extends AnyWordSpec with Matchers {

  "DeckUtils" should {
    "createDeck" should {
      "ein Standard-52-Karten-Deck generieren" in {
        val deck = DeckUtils.createDeck()

        deck.size shouldBe 52
        deck.map(_.suit).distinct.size shouldBe 4
        deck.map(_.rank).distinct.size shouldBe 13
        deck.toSet.size shouldBe 52 // Keine Duplikate
      }
    }

    "shuffle" should {
      "die Karten mischen, aber die Anzahl und den Inhalt beibehalten" in {
        val originalDeck = DeckUtils.createDeck()
        val shuffledDeck = DeckUtils.shuffle(originalDeck)

        shuffledDeck.size shouldBe 52
        shuffledDeck.toSet shouldBe originalDeck.toSet
      }
    }
  }
}
