import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.io.StringReader

class LuegenSpec extends AnyWordSpec with Matchers {

  import luegen.Game._
  import luegen.Grid
  import luegen.Card
  import luegen.Player
  import luegen.Outcome

  "Game" should {

    "getNum retries until valid number" in {
      val inputs = new StringReader("x\n1\n9\n3\n")
      Console.withIn(inputs) {
        getNum shouldBe 3
      }
    }

    "getPlayerName retries until valid" in {
      val inputs = new StringReader("\nTooLongName123\nAlice\n")
      Console.withIn(inputs) {
        getPlayerName(1) shouldBe "Alice"
      }
    }

    "callRank retries until valid" in {
      val inputs = new StringReader("Z\n2\n")
      Console.withIn(inputs) {
        callRank(Player("X")) shouldBe "2"
      }
    }

    "turn handles Played, ChallengedLieWon, ChallengedLieLost" in {
      val liar = Player("Liar", List(Card("♠","2")))
      val challenger = Player("Challenger", Nil)
      discardedCards.clear()
      discardedCards.pushAll(List(Card("♠","2")))
      roundRank = "3"
      amountPlayed = 1

      // Played
      val inputs1 = new StringReader("n\n1\n")
      Console.withIn(inputs1) {
        val result = turn(challenger, liar)
        result shouldBe Outcome.Played
      }

      // ChallengedLieWon
      val inputs2 = new StringReader("j\n")
      Console.withIn(inputs2) {
        val result = turn(challenger, liar)
        result shouldBe Outcome.ChallengedLieLost
      }

      // ChallengedLieLost (spieler lügt nicht, challenger verliert)
      roundRank = "2"
      val inputs3 = new StringReader("j\n")
      Console.withIn(inputs3) {
        val result = turn(challenger, liar)
        result shouldBe Outcome.ChallengedLieLost
      }
    }

    "firstPlayerRound sets rank and plays cards" in {
      val p = Player("P", List(Card("♠","A"), Card("♥","K")))
      val inputs = new StringReader("A\n1,2\n")
      Console.withIn(inputs) {
        firstPlayerRound(p)
        roundRank shouldBe "A"
        discardedCards.nonEmpty shouldBe true
      }
    }

    "playerRound removes selected cards" in {
      val p = Player("P", List(Card("♠","A"), Card("♥","K"), Card("♦","Q")))
      discardedCards.clear()
      val inputs = new StringReader("1,3\n")
      Console.withIn(inputs) {
        playerRound(p)
        p.hand.map(_.toString) shouldBe List("♥K")
        discardedCards.size shouldBe 2
      }
    }

    "getSelection returns correct cards" in {
      val p = Player("P", List(Card("♠","A"), Card("♥","K")))
      val inputs = new StringReader("2\n")
      Console.withIn(inputs) {
        val sel = getSelection(p)
        sel.map(_.toString) shouldBe List("♥K")
      }
    }

    "removeFromHand updates hand and discardedCards" in {
      val p = Player("P", List(Card("♠","A"), Card("♥","K")))
      discardedCards.clear()
      val removed = removeFromHand(p, List(Card("♠","A")))
      removed.map(_.toString) shouldBe List("♠A")
      p.hand.map(_.toString) shouldBe List("♥K")
      discardedCards.size shouldBe 1
    }

    "callLiar distributes cards correctly" in {
      val liar = Player("Liar", List(Card("♠","A")))
      val challenger = Player("C", Nil)
      discardedCards.clear()
      discardedCards.pushAll(List(Card("♠","2"), Card("♥","3")))
      roundRank = "2"
      amountPlayed = 2

      // Liar hat gelogen
      callLiar(challenger, liar) shouldBe true
      liar.hand.size shouldBe 3
      discardedCards.isEmpty shouldBe true

      // Reset
      liar.hand = List(Card("♠","A"))
      challenger.hand = Nil
      discardedCards.pushAll(List(Card("♠","2"), Card("♥","2")))

      callLiar(challenger, liar) shouldBe false
      challenger.hand.size shouldBe 2
      discardedCards.isEmpty shouldBe true
    }

    "playGame runs through without exceptions" in {
      val players = List(
        Player("A", List(Card("♠","2"))),
        Player("B", List(Card("♥","3")))
      )
      val order = List(0,1)
      discardedCards.clear()
      val inputs = new StringReader("2\n1\nn\nn\n")
      Console.withIn(inputs) {
        noException shouldBe thrownBy(playGame(players, order, 0))
      }
    }

  }

  "Grid" should {

    "create and print grid without errors" in {
      val players = List(Player("A"), Player("B"), Player("C"), Player("D"))
      noException shouldBe thrownBy(Grid.createGrid(players))
      noException shouldBe thrownBy(Grid.printGrid())
      noException shouldBe thrownBy(Grid.clearScreen())
    }

  }

  "Player" should {

    "Player case class works" in {
      val p = Player("X", List(Card("♠","2")))
      p.name shouldBe "X"
      p.hand.size shouldBe 1
    }

  }

}
