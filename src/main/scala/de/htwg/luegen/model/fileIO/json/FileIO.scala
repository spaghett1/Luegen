package de.htwg.luegen.model.fileIO.json

import de.htwg.luegen.model.{IGameModel, Memento}
import de.htwg.luegen.model.fileIO.IFileIO
import de.htwg.luegen.model.impl1.GameModel
import play.api.libs.json.*

import java.io.*

class FileIO extends IFileIO {
  override def save(game: IGameModel): Unit = {
    val memento = game.createMemento()
    val json = Json.toJson(memento)
    val pw = new PrintWriter(new File("game.json"))
    pw.write(Json.prettyPrint(json))
    pw.close()
  }

  override def load: IGameModel = {
    import scala.io.Source
    val source: String = Source.fromFile("game.json").getLines.mkString
    val json: JsValue = Json.parse(source)
    val memento = json.as[Memento]
    GameModel().restoreMemento(memento)
  }
}
