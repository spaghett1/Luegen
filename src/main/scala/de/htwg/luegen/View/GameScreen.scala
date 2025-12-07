package de.htwg.luegen.View
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.{AI, Human}
import de.htwg.luegen.View.GameView

import scala.io.StdIn

trait GameScreen {
  def renderAndHandleInput(controller: GameController, view: GameView): Unit
}

case object NeedsPlayerCountScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Wieviele Spieler? (2 - 8)")
    val input = StdIn.readLine()
    controller.handleRawInput(input)
  }
}

case object NeedsPlayerNamesScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Gebe die Spielernamen ein(getrennt durch Kommas)")
    val input = StdIn.readLine()
    controller.handleRawInput(input)
  }
}

case object NeedsRankInputScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Sage einen Rang fuer die Runde an (2-10, B,D,K,A): ")
    val playerType = controller.getCurrentPlayerType
    val rank = playerType match {
      case Human => StdIn.readLine()
      case AI => controller.isValidRanks.head
    }
    controller.handleRawInput(rank)
  }
}

case object NeedsCardInputScreen extends GameScreen {
  override def renderAndHandleInput(controller: GameController, view: GameView): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Gebe Kartenindices ein, getrennt durch Kommas (max. 10 Zeichen): ")
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val input = playerType match {
      case Human => StdIn.readLine()
      case AI => "1"
    }
    controller.handleRawInput(input)
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
    val error = controller.getInputError.getOrElse("")
    println(error)

    val prevPLayer = controller.getPrevPlayer
    println(s"Luege von ${prevPLayer.name} aufdecken? (j/n): ")
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val callsLie = playerType match {
      case Human => StdIn.readLine()
      case AI => "n"
    }
    controller.handleRawInput(callsLie)
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
