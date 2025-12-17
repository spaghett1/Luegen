package de.htwg.luegen.Controller

import de.htwg.luegen.Model.IGameModel

trait GameCommand {
  def execute(model: IGameModel): IGameModel
}

case class LoggingCommandDecorator(wrappedCommand: GameCommand) extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    val logEntry = s"Command ausgefuehrt: ${wrappedCommand.getClass.getSimpleName}"
    val modelAfterExecution = wrappedCommand.execute(model)
    modelAfterExecution.addLog(logEntry)
  }
}

case object InitCommand extends GameCommand {
  override def execute(model: IGameModel): IGameModel = model
}

case class SetupPlayerCountCommand(numPlayers: Int) extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setPlayerCount(numPlayers).clearError()
  }
}

case class SetupPlayersCommand(names: List[String]) extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    val modelPlayerSetup = model.setupPlayers(names)
    val modelTurnOrderSetup = modelPlayerSetup.setupTurnOrder()
    modelTurnOrderSetup.dealCards().clearError()
  }
}

case class HandleRoundRankCommand(rank: String, prevRank: String = "") extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setupRank(rank).clearError()
  }
}

case class HandleCardPlayCommand(cardIndices: List[Int]) extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.playCards(cardIndices)
  }
}

case class HandleChallengeDecisionCommand(callsLie: Boolean) extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.playerTurn(callsLie).clearError()
  }
}

case class SetNextPlayerCommand() extends GameCommand {
  override def execute(model: IGameModel): IGameModel = {
    model.setNextPlayer()
  }
}


