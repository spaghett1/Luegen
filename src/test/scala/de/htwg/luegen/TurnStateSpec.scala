package de.htwg.luegen

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class TurnStateSpec extends AnyWordSpec with Matchers {
  "reads func in TurnState" should {
    "correctly catch IllegalArgumentException" in {
      val json = JsString("something")
      val state = json.validate[TurnState] shouldBe JsError("Unbekannter TurnState")
    }

    "correctly catch StringFormatException" in {
      val json = JsNull
      json.validate[TurnState] shouldBe JsError("String erwartet")
    }
  }
}
