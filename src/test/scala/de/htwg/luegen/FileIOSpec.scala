package de.htwg.luegen.model.fileIO

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.GameModel
import de.htwg.luegen.model.fileIO.xml.FileIO as XmlIO
import de.htwg.luegen.model.fileIO.json.FileIO as JsonIO

class FileIOSpec extends AnyWordSpec with Matchers {
  "FileIO" should {
    val model = GameModel()
    val modelWithCount = model.setPlayerCount(2)
    val modelWithPlayers = modelWithCount.setupPlayers(List("Alice", "Bob"))

    "save and load via XML" in {
      val xmlIO = new XmlIO
      xmlIO.save(modelWithPlayers)
      val loaded = xmlIO.load
      loaded.getPlayerCount should be(2)
      loaded.getPlayers(loaded.getCurrentPlayerIndex).name should be("Alice")
    }

    "save and load via JSON" in {
      val jsonIO = new JsonIO
      jsonIO.save(modelWithPlayers)
      val loaded = jsonIO.load
      loaded.getPlayerCount should be(2)
      loaded.getPlayers(loaded.getCurrentPlayerIndex).name should be("Alice")
    }
  }
}
