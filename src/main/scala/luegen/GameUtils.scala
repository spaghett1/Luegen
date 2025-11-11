package luegen

object GameUtils {

  import GameData.*
  
  
  def turn(player: Player, prevPlayer: Player): Outcomes = {
    val input = InputUtils.readYesNo(player)
    if (input) {
      evaluateReveal(player, prevPlayer)
    } else {
      playerRound(player)
      Outcomes.Played
    }
  }
  
  def evaluateReveal(challenger:Player, accused: Player): Outcomes = {
    val lied = discardedCards.take(amountPlayed).exists(_.rank != roundRank)
    
    if (lied) {
      InputUtils.printPrompt(s"${accused.name} hat gelogen!")
      drawAll(accused)
      Outcomes.ChallengedLieWon
    } else {
      InputUtils.printPrompt(s"${accused.name} hat die Wahrheit gesagt!")
      drawAll(challenger)
      Outcomes.ChallengedLieLost
    }
  }
  
  def drawAll(player: Player): Unit = {
    player.addCards(discardedCards.toList)
    discardedCards.clear()
  }
  
  def playerRound(player: Player): Unit = {
    InputUtils.printPlayerHand(player)
    val selected = InputUtils.selectCards(player)
    player.playSelectedCards(selected)
  }
  
  def firstPlayerRound(player: Player): Outcomes = {
    val chosenRank = InputUtils.callRank()
    GameData.roundRank = chosenRank
    playerRound(player)
    Outcomes.Played
  }
  
}
