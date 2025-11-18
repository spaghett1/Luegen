package de.htwg.luegen.View

import de.htwg.luegen.Controller.Observer
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.View.InputUtils.*
import de.htwg.luegen.Model.*
import scala.util.Try

class GameView(controller: GameController) extends Observer {
  private val grid = new Grid
  
  controller.registerObserver(this)
  
  override def updateDisplay(): Unit = {
    val players = controller.getCurrentPlayers
    val discardedCount = controller.getDiscardedCount
    val currentPlayer = controller.getCurrentPlayer
    
    grid.printGrid(discardedCount)
    displayPlayerHand(currentPlayer)
  }
  
  def initGrid(players: List[Player]) = {
    grid.initGrid(players)
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

  def callRank(valid: List[String]): String = {
    retryUntilValid(
      prompt = s"Gebe ein Symbol fuer die Runde ein, (2-10,B,D,K,A)",
      parse = Option(_).map(_.trim),
      validate = valid.contains(_)
    )
  }

  def selectCards(player: Player): List[Int] = {
    retryUntilValid(
      prompt = "Waehle bis zu drei Karten (durch Kommas getrennt)",
      parse = input => Try(input.split(",").map(_.trim.toInt).toList).toOption,
      validate = sel => sel.forall(i => i >= 1 && i <= player.hand.size) && sel.size <= 3
    )
  }

  def readYesNo(player: Player): Boolean = {
    val input = retryUntilValid(
      prompt = s"Luege von Spieler ${player.name} aufdecken?",
      parse = Option(_).map(_.trim.toLowerCase),
      validate = s => s == "j" || s == "n"
    )
    input == "j"
  }

  def printPrompt(prompt: String): Unit = {
    print(prompt)
  }

  def displayPlayerHand(player: Player): Unit = {
    val width = player.longestCardName

    val indices = (1 to player.hand.length)
      .map(i => String.format(s"%-${width}s", i.toString))
      .mkString

    val cards = player.hand
      .map(c => String.format(s"%-${width}s", c.toString))
      .mkString

    // todo
    println(indices)
    println(cards)

  }

  def printLayedCards(player: Player, cards: List[Card]) = {
    println(s"${player.name} legt ab: ${cards.mkString(", ")}")
  }
  
  def startGamePrompt(player: Player) = {
    println(s"Das Spiel startet mit ${player.name}!")
  }

  def challengerWonMessage(player: Player, prevPlayer: Player) = {
    println(s"${prevPlayer.name} hat gelogen!")
    println("Er zieht alle Karten.")
  }

  def challengerLostMessage(player: Player, prevPlayer: Player) = {
    println(s"${prevPlayer.name} hat die Wahrheit gesagt!")
    println(s"${player.name} zieht alle Karten!")
  }
}
