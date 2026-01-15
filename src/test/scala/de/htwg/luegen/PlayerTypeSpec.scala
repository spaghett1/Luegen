package de.htwg.luegen.model.impl1

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*

class PlayerTypeSpec extends AnyWordSpec with Matchers {
  "PlayerType" should {
    "have the correct string values" in {
      PlayerType.Human.toString shouldBe "Human"
      PlayerType.AI.toString shouldBe "AI"
    }

    "serialize to JSON correctly" in {
      Json.toJson(PlayerType.Human) shouldBe JsString("Human")
      Json.toJson(PlayerType.AI) shouldBe JsString("AI")
    }

    "deserialize from JSON correctly" in {
      JsString("Human").as[PlayerType] shouldBe PlayerType.Human
      JsString("AI").as[PlayerType] shouldBe PlayerType.AI
    }

    "return a JsError for invalid input" in {
      val result = JsString("Robot").validate[PlayerType]
      result.isError shouldBe true
      // Optional: Fehlermeldung prüfen
      result match {
        case JsError(errors) => errors.head._2.head.message shouldBe "Unknown PLayerType"
        case _ => fail("Should have been a JsError")
      }
    }
  }
}
