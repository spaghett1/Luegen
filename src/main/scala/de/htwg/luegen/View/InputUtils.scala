package de.htwg.luegen.View

import de.htwg.luegen.Model.Utils.DeckUtils.Ranks
import de.htwg.luegen.Model.{Card, Player}
import de.htwg.luegen.Model.Utils.DeckUtils

object InputUtils {
  import DeckUtils.*

  import scala.annotation.tailrec
  import scala.io.StdIn
  import scala.util.Try


  @tailrec
  def retryUntilValid[T](prompt: String, parse: String => Option[T], validate: T => Boolean): T = {
    println(prompt)
    val input = Option(StdIn.readLine()).getOrElse("")
    val parsedOpt = parse(input)
    parsedOpt match {
      case Some(value) if validate(value) => value
      case _ =>
        println("Ungueltige Eingabe! Bitte erneut.")
        retryUntilValid(prompt, parse, validate)
    }
  }

  def getNum: Int = {
    retryUntilValid(
      prompt = "Wieviele Spieler? (2-8)",
      parse = str => Try(str.toInt).toOption,
      validate = n => n >= 2 && n <= 8)
  }

  def getPlayerName(i: Int): String = {
    retryUntilValid(
      prompt = s"Name von Spieler $i (max. 10 Zeichen)",
      parse = str => Option(str).map(_.trim),
      validate = name => name.nonEmpty && name.length <= 10
    )
  }

  def callRank(): String = {
    val valid = Ranks
    retryUntilValid(
      prompt = s"Gebe ein Symbol fuer die Runde ein, (2-10,B,D,K,A)",
      parse = Option(_).map(_.trim),
      validate = valid.contains(_)
    )
  }

  def selectCards(player: Player): List[Card] = {
    val input = retryUntilValid(
      prompt = "Waehle bis zu drei Karten (durch Kommas getrennt)",
      parse = input => Try(input.split(",").map(_.trim.toInt).toList).toOption,
      validate = sel => sel.forall(i => i >= 1 && i <= player.hand.size) && sel.size <= 3
    )
    input.map(i => player.hand(i - 1))
  }

  def readYesNo(player: Player): Boolean = {
    val input = retryUntilValid(
      prompt = s"Luege von Spieler $player aufdecken?",
      parse = Option(_).map(_.trim.toLowerCase),
      validate = s => s == "j" || s == "n"
    )
    input == "j"
  }

  def printPrompt(prompt: String): Unit = {
    print(prompt)
  }

  def printPlayerHand(player: Player): Unit = {
    val width = player.longestCardName

    val indices = (1 to player.hand.length)
      .map(i => String.format(s"%-${width}s", i.toString))
      .mkString

    val cards = player.hand
      .map(c => String.format(s"%-${width}s", c.toString))
      .mkString

    println(indices)
    println(cards)
    
  }

  def playerLaysCards(player: Player, cards: List[Card]) = {
    println(s"${player.name} legt ab: ${cards.mkString(", ")}")
  }

}
