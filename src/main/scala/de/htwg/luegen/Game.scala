package luegen

import de.htwg.luegen.Model.GameModel
import de.htwg.luegen.View.GameView
import de.htwg.luegen.Controller.GameController

object Game {
  @main def init(): Unit = {
    println("Spiel wird initialisiert...")
    
    val model = new GameModel()

    val controller = new GameController(model)
    
    val view = new GameView(controller)
    
    controller.initGame()
  }
}