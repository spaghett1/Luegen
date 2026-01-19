package de.htwg.luegen.Game

import de.htwg.luegen.LuegenModule.given
import de.htwg.luegen.view.GameView
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.GameModel
import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.controller.impl1.GameController
import de.htwg.luegen.view.GuiView

object Game {
  @main def init(): Unit = {

    val controller = summon[IGameController]
    val tui = new GameView()


    val gameLogicThread = new Thread(() => {
      controller.initGame()
      Thread.sleep(200)
      while (true) {
        tui.handleInput()
      }
    })
    gameLogicThread.setDaemon(false)
    gameLogicThread.start()

    try {
      val gui = new GuiView()
      gui.main(Array.empty)
    } catch {
      case e: Throwable =>
        println("\n[System] GUI konnte im Docker nicht gestartet werden")
    }

  }
}