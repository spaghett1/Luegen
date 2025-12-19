package de.htwg.luegen

import de.htwg.luegen.controller.impl1.{GameCommand, GameController, HandleRoundRankCommand, InitCommand, LoggingCommandDecorator}
import de.htwg.luegen.controller.Observer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.{Card, GameModel, Player}

class CommandSpec extends AnyWordSpec with Matchers {

  // Dummy Command für Tests
  case class DummyCommand(value: Int) extends GameCommand {
    override def execute(model: IGameModel): IGameModel = {
      // Fügt eine spezielle Log-Meldung hinzu, um die Ausführung zu bestätigen
      model.addLog(s"Dummy executed with value $value")
    }
  }

  "GameCommand" should {
    "InitCommand should return model unchanged" in {
      val model = GameModel()
      InitCommand.execute(model) shouldBe model
    }
  }

  "LoggingCommandDecorator" should {
    "die Logik des umhüllten Commands ausführen" in {
      val initialModel = GameModel()
      val decorated = LoggingCommandDecorator(DummyCommand(10))

      val newModel = decorated.execute(initialModel)

      // Prüfen, ob der Dummy Command ausgeführt wurde (letzter Log-Eintrag)
      newModel.getLogHistory.last shouldBe "Command ausgefuehrt: DummyCommand"
    }

    "einen Logging-Eintrag hinzufügen" in {
      val initialModel = GameModel()
      val decorated = LoggingCommandDecorator(HandleRoundRankCommand("A"))

      val newModel = decorated.execute(initialModel)

      // Prüfen, ob der Decorator-Eintrag vorhanden ist (erster Log-Eintrag)
      newModel.getLogHistory.head should include ("Command ausgefuehrt: HandleRoundRankCommand")
    }
  }
}