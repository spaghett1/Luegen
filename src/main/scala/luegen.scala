import scala.annotation.tailrec
import scala.collection.mutable

object luegen {

  import scala.io.StdIn
  import scala.util.Try
  import scala.util.Random

  case class Card(suit: String, rank: String) {
    override def toString: String = rank + suit
  }

  case class Player(name: String, var hand: List[Card] = Nil)

  object Game {
    
    import scala.collection.mutable.Stack
    
    var numberIndex = 0
    var discardedCards = mutable.Stack[Card]()

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
      val turnOrder = List(1,5,4,8,2,6,3,7)
      val validOrder = turnOrder
        .filter(_ <= playerNum)
        .map(_ -1)
        
      val startIndex = Random.nextInt(validOrder.length)
      val playOrder = validOrder.drop(startIndex) ++ validOrder.take(startIndex)
      Grid.createGrid(players)
      playOrder.foreach { idx => 
        val player = players(idx)
        playerRound(player)
        
      }
    }
    private def playerRound(player: Player): Unit = {
      Grid.printGrid(0)
      println(s"${player.name} ist dran: ")
      val hand = player.hand
      val width = hand.map(_.toString.length).max + 1
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
      println(s"${player.name} legt ab: ${removed.mkString(", ")}")
      Grid.clearScreen()
      println(s"${player.name} legt ${selIndices.length} Karten ab")
      
    }

    private def getSelection(player: Player): List[Card] = {
      val maxCards = 3
      val handSize = player.hand.size
      println("Waehle bis zu drei Karten (durch Kommas getrennt): ")
      val input = StdIn.readLine()

      val selections = input
        .split(",")
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(_.toInt)
        .toList

      val selCards = selections.map(i => player.hand(i - 1))
      val allSameRank = selCards.map(_.rank).distinct.size == 1
      
      if(allSameRank) {
        selCards
      } else {
        println("Bitte gleiche Kartenwerte!")
        getSelection(player)
      }
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
          //playOrder.insert()
          val spacesBot = " " * (22 - names(1).length - names(5).length)
          text(10) = spacesVert + " " + names(1) + spacesBot + names(5)
          if players.size > 6 then
            text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| "
            text(5) = s"${spacesVert} |${" " * 8}|  0 |${" " * 8}| ${names(3)} "
            text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| "
            if players.size > 7 then
              text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| ${names(3)}"
              text(5) = s"${spacesVert} |${" " * 8}|  0 |${" " * 8}| "
              text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| ${names(7)}"
      numberPos = sizeVert + 11
    }

    def printGrid(number: Int) = {
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
