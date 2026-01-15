package de.htwg.luegen.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.{Player, PlayerType, Card}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.TurnState
import play.api.libs.json.*

class MementoSpec extends AnyWordSpec with Matchers {

  "A Memento" should {
    // Test-Daten vorbereiten
    val cards = List(Card("♠", "A"))
    val players = List(Player("Alice", cards, Human))
    val memento = Memento(
      discardedCards = cards,
      roundRank = "A",
      playerCount = 1,
      players = players,
      playOrder = List(0),
      turnState = TurnState.NeedsCardInput
    )

    "store values correctly in the case class" in {
      memento.roundRank shouldBe "A"
      memento.playerCount shouldBe 1
      memento.players.head.name shouldBe "Alice"
    }

    "generate correct XML" in {
      val xml = memento.toXml
      (xml \ "roundRank").text.trim shouldBe "A"
      (xml \ "playerCount").text.trim shouldBe "1"
      (xml \ "playOrder").text.trim shouldBe "0"
      // Prüfen, ob die XML-Struktur der Spieler enthalten ist
      (xml \ "players" \ "player").toList should not be empty
    }

    "support JSON serialization (Play-JSON)" in {
      // Objekt -> JSON
      val json = Json.toJson(memento)
      (json \ "roundRank").as[String] shouldBe "A"
      (json \ "playerCount").as[Int] shouldBe 1

      // JSON -> Objekt
      val deserialized = json.as[Memento]
      deserialized shouldBe memento
      deserialized.turnState shouldBe TurnState.NeedsCardInput
    }

    "have default values" in {
      val defaultMemento = Memento()
      defaultMemento.discardedCards shouldBe empty
      defaultMemento.turnState shouldBe TurnState.NoTurn
    }
  }
}
