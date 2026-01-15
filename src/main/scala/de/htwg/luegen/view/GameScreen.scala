package de.htwg.luegen.view

import de.htwg.luegen.model.impl1.{AI, Human, Player}
import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.view.GameView

import scala.io.StdIn
import scala.util.Try
import scala.util.Failure
import scala.util.Success

trait GameScreen {
  def display(using controller: IGameController): Unit
  def processInput(input: String)(using controller: IGameController): Unit

  def handleGlobalCommand(using controller: IGameController)(rawInput: String): Boolean = {
    rawInput match {
      case "undo" => 
        controller.undo()
        true
      case "redo" => 
        controller.redo()
        true
      case "save" =>
        controller.save
        true
      case "load" =>
        controller.load
        true
      case _ => false
    }
  }
}

case object NeedsPlayerCountScreen extends GameScreen {

  def validateInput(rawInput: String): Try[Int] = {
    Try(rawInput.trim.toInt).flatMap { n =>
      if (n >= 2 && n <= 8) Success(n)
      else Failure(new Exception("Gebe eine gueltige Zahl ein!"))
    }
  }

  override def display(using controller: IGameController): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Wieviele Spieler? (2 - 8)")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    if (!handleGlobalCommand(input)) {
      validateInput(input) match {
        case Success(count) => controller.handlePlayerCount(count)
        case Failure(e) => controller.handleError(e)
      }
    }
  }
}

case object NeedsPlayerNamesScreen extends GameScreen {
  
  def validateInput(rawInput: String, playerCount: Int): Try[List[String]] = {
    val names = rawInput.split(",").map(_.trim).toList
    val isValidCount = names.size == playerCount
    val isValidName = names.nonEmpty && names.forall(name => name.nonEmpty && name.length <= 10)

    if (isValidCount && isValidName) Success(names)
    else Failure(new Exception(s"Gebe $playerCount Namen durch Komma getrennt ein! (max. 10 Zeichen"))
  }

  override def display(using controller: IGameController): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Gebe die Spielernamen ein(getrennt durch Kommas)")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    
    if (!handleGlobalCommand(input)) {
      validateInput(input, controller.getPlayerCount) match {
        case Success(names) => controller.handlePlayerNames(names)
        case Failure(e) => controller.handleError(e)
      }
    }
  }
}

case object NeedsRankInputScreen extends GameScreen {
  
  def validateInput(rawInput: String, validRanks: List[String]): Try[String] = {
    val rank = rawInput.trim
    if (validRanks.contains(rank)) {
      Success(rank)
    } else {
      Failure(new Exception("Gebe einen gueltigen Rang ein!"))
    }
  }

  override def display(using controller: IGameController): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Sage einen Rang fuer die Runde an (2-10, B,D,K,A): ")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    
    if (!handleGlobalCommand(input)) {
      val playerType = controller.getCurrentPlayerType
      val finalInput = playerType match {
        case AI => controller.isValidRanks.head
        case Human => input
      }
      
      validateInput(finalInput, controller.isValidRanks) match {
        case Success(rank) => controller.handleRoundRank(rank)
        case Failure(e) => controller.handleError(e)
      }
    }
  }
}

case object NeedsCardInputScreen extends GameScreen {
  
  def validateInput(rawInput: String, playerHandSize: Int): Try[List[Int]] = {
    val parsedTry = Try {rawInput.split(",").map(_.trim.toInt).toList }
    parsedTry.flatMap { selIndices => 
      val isValidQuantity = selIndices.size >= 1 && selIndices.size <= 3
      val isValidRange = selIndices.forall(i => i >= 1 && i <= playerHandSize)
      
      if (isValidQuantity && isValidRange) {
        Success(selIndices)
      } else {
        Failure(new Exception("Gebe gueltige Indices ein! (max 3 Zahlen)"))
      }
    }
  }

  override def display(using controller: IGameController): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)
    println("Gebe Kartenindices ein, getrennt durch Kommas (max. 10 Zeichen): ")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val finalInput = playerType match {
      case Human => input
      case AI => "1"
    }
    
    if (!handleGlobalCommand(finalInput)) {
      validateInput(finalInput, currentPlayer.hand.size) match {
        case Success(indices) => controller.handleCardInput(indices)
        case Failure(e) => controller.handleError(e)
      }
    }
  }
}

case object ChallengedLieWonScreen extends GameScreen {
  
  override def display(using controller: IGameController): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val prevPlayer = controller.getPrevPlayer
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    if (!handleGlobalCommand(input)) {
      controller.setNextPlayer()
    }
  }
}

case object NeedsChallengeDecisionScreen extends GameScreen {
  def validateInput(rawInput: String): Try[Boolean] = {
    rawInput.toLowerCase.trim match {
      case "j" => Success(true)
      case "n" => Success(false)
      case _ => Failure(new Exception("Gebe 'j' oder 'n' ein"))
    }
  }

  override def display(using controller: IGameController): Unit = {
    val error = controller.getInputError.getOrElse("")
    println(error)

    val prevPLayer = controller.getPrevPlayer
    println(s"Luege von ${prevPLayer.name} aufdecken? (j/n): ")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    
    val currentPlayer = controller.getCurrentPlayer
    val playerType = controller.getCurrentPlayerType
    val rawInput = playerType match {
      case Human => input
      case AI => "n"
    }
    
    if (!handleGlobalCommand(rawInput)) {
      validateInput(rawInput) match {
        case Success(decision) => controller.handleChallengeDecision(decision)
        case Failure(e) => controller.handleError(e)
      }
    }
  }
}

case object ChallengedLieLostScreen extends GameScreen {
  override def display(using controller: IGameController): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val prevPlayer = controller.getPrevPlayer
    println("TODO")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    if (!handleGlobalCommand(input)) {
      controller.setNextPlayer()
    }
  }
}

case object PlayedScreen extends GameScreen {
  override def display(using controller: IGameController): Unit = {
    val currentPlayer = controller.getCurrentPlayer
    val playedCards = controller.getPlayedCards
    
    println("TODO")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    if (!handleGlobalCommand(input)) {
      controller.setNextPlayer()
    }
  }
}

case object GameOverScreen extends GameScreen {
  override def display(using controller: IGameController): Unit = {
    println("###############################")
    println("          GAME OVER           ")
    println(controller.getInputError.getOrElse(""))
    println("###############################")
    println("Tippe 'new' für ein neues Spiel oder 'exit'.")
  }

  override def processInput(input: String)(using controller: IGameController): Unit = {
    input match {
      case "new" => controller.handlePlayerCount(0) // Oder deine Reset-Logik
      case _ => System.exit(0)
    }
  }
}


 