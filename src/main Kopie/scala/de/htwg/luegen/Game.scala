package luegen

import de.htwg.luegen.model.GameModel
import de.htwg.luegen.view.GameView
import de.htwg.luegen.controller.GameController
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.view.GuiView

object Game {
  @main def init(): Unit = {
    val model: IGameModel = GameModel()
    val controller: IGameController = GameController(model)
    val tui = new GameView(controller)
    val gui = new GuiView(controller)

    // TUI in den Hintergrund schieben
    val tuiThread = new Thread(() => {
      // Warte kurz, bis das GUI-Fenster stabil steht
      Thread.sleep(1000)
      while (true) {
        // Der Controller triggert hier über notifyObservers die GUI mit
        tui.handleInput()
      }
    })
    tuiThread.setDaemon(true)
    tuiThread.start()

    // GUI muss auf Thread 0 laufen
    gui.main(Array.empty)
    controller.initGame()
  }
}