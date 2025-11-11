package de.htwg.luegen.Model

import scala.collection.mutable.Stack

// Dies kapselt den Zustand, der vorher in GameData war
class GameModel(var players: List[Player]) {

  var discardedCards: Stack[Card] = Stack.empty
  var roundRank: String = ""
  var amountPlayed: Int = 0

  // Methoden, um Daten abzurufen (Getters)
  def getDiscardedCardCount: Int = discardedCards.length

  // -------------------------------------------------------------------------
  // Logik aus GameUtils.scala (wird Teil des Models)
  // -------------------------------------------------------------------------

  // Hilfsmethode, um alle Karten auf den Ablagestapel zu legen
  def drawAll(player: Player): Unit = {
    player.addCards(discardedCards.toList)
    discardedCards.clear()
  }

  // Logik zur Auswertung einer Lüge (braucht keine View/Input)
  def evaluateReveal(challenger: Player, accused: Player): Outcomes = {
    val lied = discardedCards.take(amountPlayed).exists(_.rank != roundRank)

    if (lied) {
      // InputUtils.printPrompt(s"${accused.name} hat gelogen!") // VIEW-Logik entfernt
      drawAll(accused)
      Outcomes.ChallengedLieWon
    } else {
      // InputUtils.printPrompt(s"${accused.name} hat die Wahrheit gesagt!") // VIEW-Logik entfernt
      drawAll(challenger)
      Outcomes.ChallengedLieLost
    }
  }

  // Logik für den ersten Zug (braucht jetzt den Rank als Argument, nicht als I/O)
  def firstPlayerRound(player: Player, chosenRank: String, selectedCards: List[Card]): Outcomes = {
    this.roundRank = chosenRank
    player.playSelectedCards(selectedCards, this) // Übergibt Model-Referenz
    Outcomes.Played
  }

  // Logik für einen normalen Zug (braucht selectedCards als Argument)
  def playerRound(player: Player, selectedCards: List[Card]): Outcomes = {
    player.playSelectedCards(selectedCards, this) // Übergibt Model-Referenz
    Outcomes.Played
  }
}
