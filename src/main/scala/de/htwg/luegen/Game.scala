package luegen

import de.htwg.luegen.Model.GameModel
import de.htwg.luegen.View.GameView
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.Model.IGameModel
import de.htwg.luegen.Controller.IGameController
import de.htwg.luegen.View.GuiView

object Game {
  @main def init(): Unit = {
    val model: IGameModel = GameModel()
    val controller: IGameController = GameController(model)
    val tui = new GameView(controller)
    val gui = new GuiView(controller)
    
    val tuiThread = new Thread(() => {
      Thread.sleep(1000)
      while (true) {
        tui.handleInput()
      }
    })
    tuiThread.setDaemon(true)
    tuiThread.start()
    
    gui.main(Array.empty)
    controller.initGame()
  }
}