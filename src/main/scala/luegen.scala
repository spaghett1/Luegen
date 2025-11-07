import scala.annotation.tailrec
import scala.collection.mutable

object luegen {

  import scala.io.StdIn
  import scala.util.Try
  import scala.util.Random

  case class Card(suit: String, rank: String) {
    override def toString: String = suit + rank
  }

  case class Player(name: String, var hand: List[Card] = Nil)

  enum Outcome {
    case Played
    case ChallengedLieWon
    case ChallengedLieLost
    case Invalid
  }

  object Game {

    import scala.collection.mutable.Stack

    var numberIndex = 0
    var discardedCards = mutable.Stack[Card]()
    var roundRank = ""
    var amountPlayed = 0

    def createDeck(): List[Card] = {
      val spade = "\u2660"
      val heart = "\u2665"
      val diamond = "\u2666"
      val club = "\u2663"
      val suits = List(spade, heart, diamond, club)
      val ranks =
        List("2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A")

      for (s <- suits; r <- ranks) yield Card(s, r)
    }

    def initPlayers(): List[Player] = {

      val numPlayers = getNum

      (1 to numPlayers).map { i =>
        Player(getPlayerName(i))
      }.toList
    }

    @tailrec
    def getNum: Int = {
      println("Wieviele Spieler? (2-8)")
      val input = StdIn.readLine()

      Try(input.toInt).toOption match {
        case Some(n) if n >= 2 && n <= 8 => n
        case _ =>
          println("Ungueltige Eingabe!")
          getNum
      }
    }

    @tailrec
    def getPlayerName(i: Int): String = {
      println(s"Name von Spieler $i (max. 10 Zeichen)")
      val name = StdIn.readLine().trim()
      if (name.isEmpty) {
        println("Bitte gebe etwas ein")
        getPlayerName(i)
      } else if (name.length > 10) {
        println("Name zu lang! Bitte abkuerzen")
        getPlayerName(i)
      } else name

    }

    def init(): Unit = {

      import scala.util.Random

      val deck = Random.shuffle(createDeck())
      println(s"Deck size: ${deck.size}")

      val players = initPlayers()
      val playerNum = players.size
      for ((card, i) <- deck.zipWithIndex) {
        val playerIndex = i % playerNum
        players(playerIndex).hand = players(playerIndex).hand :+ card
      }

      val turnOrder = List(1, 5, 4, 8, 2, 6, 3, 7)
      val validOrder = turnOrder
        .filter(_ <= players.length)
        .map(_ - 1)
      val startIndex = Random.nextInt(validOrder.length)
      playGame(players, validOrder, startIndex)

    }

    def playGame(players: List[Player], validOrder: List[Int], startIndex: Int): Unit = {

      val playOrder = validOrder.drop(startIndex) ++ validOrder.take(startIndex)
      Grid.createGrid(players)
      playOrder.zipWithIndex.foreach { case (idx, i) =>
        val player = players(idx)
        Grid.printGrid()
        println(s"${player.name} ist dran")
        if (i == 0) {
          firstPlayerRound(player)
        }
        else {
          val prevIndex = playOrder(idx - 1)
          val prevPlayer = players(prevIndex)
          val outcome = turn(player, prevPlayer)
          outcome match {
            case Outcome.Played => ()
            case Outcome.ChallengedLieWon =>
              playGame(players, validOrder, idx)
            case Outcome.ChallengedLieLost =>
              playGame(players, validOrder, idx - 1)
            case _ => println("Fehler beim Auswerten des Spielzuges")
          }
        }
      }
    }


    def turn(player: Player, prevPlayer: Player): Outcome = {
      println(s"Luege von ${prevPlayer.name} aufdecken?")
      println("j/n")
      val input = StdIn.readLine()
      if (input == null) {
        return Outcome.Invalid
      }
      if (input.length != 1 || (input != "j"&& input != "n")) {
        println("Ungueltige Eingabe!")
        turn(player, prevPlayer)
      } else if (input == "j") {
        if (callLiar(player, prevPlayer)) {
          Outcome.ChallengedLieWon
        } else {
          Outcome.ChallengedLieLost
        }
      } else if (input == "n") {
        playerRound(player)
        Outcome.Played
      } else {
        Outcome.Invalid
      }
    }
    def callRank(player: Player): String = {
      val ranks = List("2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A")

      println("Gebe ein Symbol fuer die Runde ein")
      println("(2-10, B, D, K, A): ")
      val input = StdIn.readLine().trim()
      if (input.length != 1 || !ranks.contains(input)) {
        println("Ungueltiger Wert! Nochmal: ")
        callRank(player)
      } else {
        input
      }
    }

    def callLiar(challenger: Player, player: Player):Boolean = {
      val lastCards = discardedCards.take(amountPlayed)
      val lied = lastCards.exists(_.rank != roundRank)

      if(lied) {
        println(s"${player.name} hat gelogen!")
        player.hand ++= discardedCards
        discardedCards.clear()
        true
      } else {
        println(s"${player.name} hat die Wahrheit gesagt!")
        challenger.hand ++= discardedCards
        discardedCards.clear()
        false
      }


    }

    def firstPlayerRound(player: Player) = {
      roundRank = callRank(player)
      playerRound(player)
    }

    def playerRound(player: Player): Unit = {
      val hand = player.hand
      val width = if (hand.nonEmpty) hand.map(_.toString.length).max + 1 else 1
      val indices = (1 to hand.length)
        .map( i => ("%-" + width + "s").format(i))
        .mkString

      val cards = hand
        .map(c => ("%-" + width + "s").format(c))
        .mkString

      println(indices)
      println(cards)
      val selIndices = getSelection(player)
      val removed = removeFromHand(player, selIndices)
      amountPlayed = removed.length
      println(s"${player.name} legt ab: ${removed.mkString(", ")}")
      Grid.clearScreen()
      println(s"${player.name} legt ${selIndices.length} Karten ab")

    }

    def getSelection(player: Player): List[Card] = {
      val maxCards = 3
      val handSize = player.hand.size
      println("Waehle bis zu drei Karten (durch Kommas getrennt): ")
      val input = StdIn.readLine()

      val selections = input
        .split(",")
        .map(_.trim)
        .filter(s => s.nonEmpty && s.forall(_.isDigit))
        .map(_.toInt)
        .toList


      val selCards = selections
        .filter(i => i >= 1 && i <= player.hand.size)
        .map(i => player.hand(i - 1))
      selCards
    }

    def removeFromHand(player: Player, toRemove: List[Card]): List[Card] = {
      val (removed, kept) = player.hand.partition(toRemove.contains)
      player.hand = kept
      discardedCards.pushAll(removed)
      removed
    }
  }

  object Grid {
    var text = new Array[String](11)
    var numberPos = 0;

    def createGrid(players: List[Player]): Unit = {

      val names = players
        .map(p => if p.name.length % 2 == 0 then p.name else s" ${p.name}")
        .padTo(8, "")
        .toArray

      val sizeVert = (math.max(names(2).length, names(6).length))
      names(2) = " " * (sizeVert - names(2).length) + names(2)
      names(6) = " " * (sizeVert - names(6).length) + names(6)
      val spacesVert = " " * (sizeVert + 1)

      text(1) = spacesVert + "+" + "-" * 22 + "+"
      text(9) = text(1)
      text(2) = spacesVert + "|" + " " * 22 + "|"
      text(8) = text(2)
      text(4) = spacesVert + "|" + " " * 8 + "|" + " " * 4 + "|" + " " * 8 + "|"
      text(6) = text(4)

      val spacesTop = " " * ((24 - names(0).length) / 2)
      text(0) = spacesVert + spacesTop + names(0) + spacesTop
      text(3) = spacesVert + "|" + " " * 8 + "+----+" + " " * 8 + "|"
      text(7) = text(3)
      text(5) = s"${names(2)} |${" " * 8}|  0 |${" " * 8}| ${names(3)}"
      val spacesBot = " " * ((24 - names(1).length) / 2)
      text(10) = spacesVert + spacesBot + names(1) + spacesBot
      if players.size > 4 then
        val spacesTop = " " * (22 - names(0).length - names(4).length)
        text(0) = spacesVert + " " + names(0) + spacesTop + names(4) + " "
        if players.size > 5 then
          val spacesBot = " " * (22 - names(1).length - names(5).length)
          text(10) = spacesVert + " " + names(1) + spacesBot + names(5)
          if players.size > 6 then
            text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| "
            text(5) = s"${spacesVert} |${" " * 8}|  0 |${" " * 8}| ${names(3)} "
            text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| "
            if players.size > 7 then
              text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| ${names(3)}"
              text(5) = s"${spacesVert}|${" " * 8}|  0 |${" " * 8}| "
              text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| ${names(7)}"
      numberPos = sizeVert + 11
    }

    def printGrid(): Unit = {
      val stack = Game.discardedCards
      var number = 0
      if stack.isEmpty then
        number = 0
      else
        number = stack.size

      val line = text(5);
      val card = if (number > 9) s" ${number} " else s"  ${number} "
      val newline = line.patch(numberPos, card, 4)
      text(5) = newline
      println(text.mkString("\n"))
    }

    def clearScreen(): Unit  = {
      print("\u001b[2J")
      print("\u001b[H")
      println("\n" * 20)
    }

    @main def test() = {
      luegen.Game.init()
    }

  }
}
