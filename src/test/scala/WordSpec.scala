package luegen
// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
import org.scalatest.wordspec.AnyWordSpec

class WordSpec extends AnyWordSpec {
  "Our luegen game" should {
    /*"calculate the correct number of cards with given amount of players" in {
      assert()
    }
    */
    "create player correctly" in {
      val player = Luegen.Player(1, "Alex", score = 40, queen = 2)
      assert(player.number == 1)
      assert(player.name == "Alex")
      assert(player.hand.isEmpty)
      assert(player.score == 40)
      assert(player.queen == 2)
    }

    "add score correctly to a player" in {
      val players = Seq(
        Luegen.Player(1, "Alex"),
        Luegen.Player(2, "Luca")
      )
      Luegen.addScore(players, 1, 10)
      assert(players.head.score == 10)
    }

    "get score correctly from a player" in {
      val players = Seq(
        Luegen.Player(1, "Alex", score = 5),
        Luegen.Player(2, "Luca", score = 10)
      )
      val scores = Luegen.getScore(players)
      assert(scores.contains("Alex: 5 Punkte"))
      assert(scores.contains("Luca: 10 Punkte"))
    }
  }
}
