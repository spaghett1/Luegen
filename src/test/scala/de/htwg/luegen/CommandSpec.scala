package de.htwg.luegen

import de.htwg.luegen.Controller.{GameCommand, GameController, HandleRoundRankCommand, InitCommand, LoggingCommandDecorator, Observer}
import de.htwg.luegen.Model.{Card, GameModel, Player}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommandSpec extends AnyWordSpec with Matchers {

  // Dummy Command für Tests
  case class DummyCommand(value: Int) extends GameCommand {
    override def execute(model: GameModel): GameModel = {
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
      newModel.logHistory.last shouldBe "Command ausgefuehrt: DummyCommand"
    }

    "einen Logging-Eintrag hinzufügen" in {
      val initialModel = GameModel()
      val decorated = LoggingCommandDecorator(HandleRoundRankCommand("A"))

      val newModel = decorated.execute(initialModel)

      // Prüfen, ob der Decorator-Eintrag vorhanden ist (erster Log-Eintrag)
      newModel.logHistory.head should include ("Command ausgefuehrt: HandleRoundRankCommand")
    }
  }
}