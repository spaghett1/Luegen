package de.htwg.luegen.View

import de.htwg.luegen.Controller.Observer
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.View.InputUtils.*
import de.htwg.luegen.Model.*
import de.htwg.luegen.Outcomes
import scala.util.Try

class GameView(controller: GameController) extends Observer {
  private val grid = new Grid
  
  controller.registerObserver(this)
  
  override def updateDisplay(): Unit = {
    val players = controller.getCurrentPlayers
    val discardedCount = controller.getDiscardedCount
    val currentPlayer = controller.getCurrentPlayer
    val roundRank = controller.getRoundRank

    val outcome = controller.getLastActionOutcome
    val details = controller.getLastActionDetails

    if (players.isEmpty) {
      val numPlayers = getNum
      val playerNames = (1 to numPlayers).map(getPlayerName).toList

      controller.setupGame(numPlayers, playerNames)
      return
    }

    if (players.nonEmpty && grid.text(0).isEmpty) {
      initGrid(players)
    }

    grid.printGrid(discardedCount)

    if (players.nonEmpty) {
      displayPlayerHand(currentPlayer)
      println(s"Aktueller Rang: ${if (roundRank.isEmpty) "Keiner" else roundRank }")
    }

    val prevPlayer = controller.getPrevPlayer

    (outcome, details) match {
      case (Outcomes.Played, Some(d)) if d.isGameStart =>
        startGamePrompt(currentPlayer)

      case (Outcomes.Played, Some(d)) if d.playedPlayer.isDefined && d.playedPlayer.nonEmpty =>
        d.playedPlayer.foreach(p => printLayedCards(p, d.playedCards))

      case (Outcomes.ChallengedLieWon, _) =>
        challengerWonMessage(currentPlayer, prevPlayer)

      case (Outcomes.ChallengedLieLost, _) =>
        challengerLostMessage(currentPlayer, prevPlayer)

      case _ =>
    }

    if (controller.isFirstTurn) {
      val rank = callRank(controller.isValidRanks)
      controller.handleRoundRank(rank)
      return
    }

    if (currentPlayer != prevPlayer) {
      val callsLie = readYesNo(prevPlayer)

      controller.handleChallengeDecision(callsLie)
      return
    } else {
      printPrompt(s" ${currentPlayer.name} ist am Zug")
      val cardIndices = selectCards(currentPlayer)

      controller.handleCardPlay(cardIndices)
      return
    }
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
