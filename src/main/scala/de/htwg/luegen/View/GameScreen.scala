package de.htwg.luegen.View
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{AI, Human}
import de.htwg.luegen.View.GameView

trait GameScreen {
  def renderAndHandleInput(controller: GameController, view: GameView): Unit
}

case object NeedsRankInputScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val playerType = controller.getCurrentPlayerType
    val rank = playerType match {
      case Human => view.callRank(controller.isValidRanks)
      case AI => controller.isValidRanks.head
    }
    controller.handleRoundRank(rank)
  }
}

case object NeedsCardInputScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val input = playerType match {
      case Human => view.selectCards(currentPlayer)
      case AI => List(1)
    }
    controller.handleCardPlay(input)
  }
}

case object ChallengedLieWonScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val prevPlayer = controller.getPrevPlayer
    view.challengerWonMessage(currentPlayer, prevPlayer)
  }
}

case object NeedsChallengeDecisionScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val callsLie = playerType match {
      case Human => view.readYesNo(currentPlayer)
      case AI => false
    }
    controller.handleChallengeDecision(callsLie)
  }
}

case object ChallengedLieLostScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val prevPlayer = controller.getPrevPlayer
    view.challengerLostMessage(currentPlayer, prevPlayer)
  }
}

case object PlayedScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val playedCards = controller.getPlayedCards
    
    view.printLayedCards(currentPlayer, playedCards)
  }
}
