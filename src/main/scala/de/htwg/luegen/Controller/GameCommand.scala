package de.htwg.luegen.Controller

import de.htwg.luegen.Model.GameModel

trait GameCommand {
  def execute(model: GameModel): GameModel
}

case class LoggingCommandDecorator(wrappedCommand: GameCommand) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val logEntry = s"Command ausgefuehrt: ${wrappedCommand.getClass.getSimpleName}"
    val modelAfterExecution = wrappedCommand.execute(model)
    modelAfterExecution.addLog(logEntry)
  }
}

case object InitCommand extends GameCommand {
  override def execute(model: GameModel): GameModel = model
}

case class SetupPlayerCountCommand(numPlayers: Int) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    model.setPlayerCount(numPlayers).clearError()
  }
}

case class SetupPlayersCommand(names: List[String]) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val modelPlayerSetup = model.setupPlayers(names)
    val modelTurnOrderSetup = modelPlayerSetup.setupTurnOrder()
    modelTurnOrderSetup.dealCards().clearError()
  }
}

case class HandleRoundRankCommand(rank: String, prevRank: String = "") extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    model.setupRank(rank).clearError()
  }
}

case class HandleCardPlayCommand(cardIndices: List[Int]) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    val modelAfterPlay = model.playCards(cardIndices)
    modelAfterPlay.setNextPlayer().clearError()
  }
}

case class HandleChallengeDecisionCommand(callsLie: Boolean) extends GameCommand {
  override def execute(model: GameModel): GameModel = {
    model.playerTurn(callsLie).clearError()

  }
}


