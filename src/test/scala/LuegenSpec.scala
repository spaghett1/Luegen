import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.io.{ByteArrayInputStream, StringReader}

class LuegenSpec extends AnyWordSpec with Matchers {

  import luegen._

  "Our Game object" should {

    "create a full deck" in {
      val deck = Game.createDeck()
      deck.size shouldBe 52
      deck.map(_.suit).distinct.sorted shouldBe List("♠", "♥", "♣", "♦").sorted
      deck.map(_.rank).distinct.sorted shouldBe List(
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "B",
        "D",
        "K",
        "A"
      ).sorted
    }

    "initialize players with valid input" in {
      val input = new StringReader("3\nLuca\nAlex\nBryan\n")
      Console.withIn(input) {
        val players = Game.initPlayers()
        players.size shouldBe 3
        players.map(_.name) shouldBe List("Luca", "Alex", "Bryan")
      }
    }

    "retry until a valid number is entered" in {
      // Test Input:
      // "abc" -> Try-Failure branch
      // "10"  -> Some(n) but guard false
      // "1"   -> Some(n) but guard false
      // "2"   -> valid input, guard true
      val input = new StringReader("abc\n10\n1\n2\n")

      Console.withIn(input) {
        Game.getNum() shouldBe 2
      }
    }

    "retry getPlayerName until valid" in {
      val input = new StringReader("\nZuLangerName\nAlex\n")
      Console.withIn(input) {
        Game.getPlayerName(1) shouldBe "Alex"
      }
    }

    "init game and distribute cards to players" in {
      val input = new StringReader("4\nAlex\nLuca\nBryan\nPatrick\n")
      Console.withIn(input) {
        Game.init() // Dieser Aufruf ruft createDeck, initPlayers und Grid auf
        // Wir prüfen hier nur, dass keine Exception geworfen wurde
        succeed
      }
    }
  }

  "Our Grid object" should {

    "create a grid for players" in {
      for (numPlayers <- 2 to 8) {
        val players = (1 to numPlayers).map(i => Player(s"P$i")).toList
        Grid.createGrid(players)
        Grid.text.length shouldBe 11

        val gridString = Grid.text.mkString
        players.foreach {p =>
          gridString should include(p.name)
        }

      }
    }

    "print grid with a single or double digit number" in {
      val players = List(Player("A"), Player("B"), Player("C"), Player("D"))
      Grid.createGrid(players)
      Grid.printGrid(5)
      Grid.text(5) should include("  5 ")
      Grid.printGrid(10)
      Grid.text(5) should include(" 10 ")
    }
  }
}
