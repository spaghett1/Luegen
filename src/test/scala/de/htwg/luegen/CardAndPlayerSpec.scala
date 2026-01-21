package de.htwg.luegen.model.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.TurnState
import de.htwg.luegen.model.impl1.PlayerType
import de.htwg.luegen.model.impl1.PlayerType.Human
import play.api.libs.json.Json

class CardSpec extends AnyWordSpec with Matchers {
  "A Card" should {
    val card = Card("♠", "10")
    "have a suit" in { card.suit should be("♠") }
    "have a rank" in { card.rank should be("10") }
    "have a nice String representation" in { card.toString should be("♠10") }
  }
}

class PlayerSpec extends AnyWordSpec with Matchers {
  "A Player" should {
    val hand = List(Card("♥", "A"), Card("♦", "7"))
    val player = Player("TestBot", hand, Human)
    "have a name" in { player.name should be("TestBot") }
    "have cards" in { player.hand should be(hand) }
    "report correct card count" in { player.cardCount should be(2) }
    "have correct json representation" in {
      import de.htwg.luegen.model.impl1.Player.playerFormat
      val json = Json.toJson(player) (playerFormat)
      (json \ "name").as[String] shouldBe "TestBot"

      val jsonObj = Json.obj(
        "name" -> "Alice"
      )

      val result = jsonObj.validate[Player]
      result.isSuccess shouldBe true
      val p = result.get
      p.name shouldBe "Alice"
    }
  }
}
