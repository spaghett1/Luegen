package de.htwg.luegen.controller

import de.htwg.luegen.model.IGameModel

trait GameCommand {
  def execute(model: IGameModel): IGameModel
}

case class LoggingCommandDecorator(wrappedCommand: impl1.GameCommand) extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    val logEntry = s"Command ausgefuehrt: ${wrappedCommand.getClass.getSimpleName}"
    val modelAfterExecution = wrappedCommand.execute(model)
    modelAfterExecution.addLog(logEntry)
  }
}

case object InitCommand extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = model
}

case class SetupPlayerCountCommand(numPlayers: Int) extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setPlayerCount(numPlayers).clearError()
  }
}

case class SetupPlayersCommand(names: List[String]) extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    val modelPlayerSetup = model.setupPlayers(names)
    val modelTurnOrderSetup = modelPlayerSetup.setupTurnOrder()
    modelTurnOrderSetup.dealCards().clearError()
  }
}

case class HandleRoundRankCommand(rank: String, prevRank: String = "") extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setupRank(rank).clearError()
  }
}

case class HandleCardPlayCommand(cardIndices: List[Int]) extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.playCards(cardIndices)
  }
}

case class HandleChallengeDecisionCommand(callsLie: Boolean) extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.playerTurn(callsLie).clearError()
  }
}

case class SetNextPlayerCommand() extends impl1.GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setNextPlayer()
  }
}


