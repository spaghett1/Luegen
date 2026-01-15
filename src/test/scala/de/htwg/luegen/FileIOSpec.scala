package de.htwg.luegen.model.fileIO

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.GameModel
import de.htwg.luegen.model.fileIO.xml.FileIO as XmlIO
import de.htwg.luegen.model.fileIO.json.FileIO as JsonIO

class FileIOSpec extends AnyWordSpec with Matchers {
  "FileIO" should {
    val model = GameModel()
    model.handlePlayerCount(2)
    model.handlePlayerNames(List("Alice", "Bob"))

    "save and load via XML" in {
      val xmlIO = new XmlIO
      xmlIO.save(model)
      val loaded = xmlIO.load
      loaded.playerCount should be(2)
      loaded.getCurrentPlayer.name should be("Alice")
    }

    "save and load via JSON" in {
      val jsonIO = new JsonIO
      jsonIO.save(model)
      val loaded = jsonIO.load
      loaded.playerCount should be(2)
    }
  }
}
