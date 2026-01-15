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
    val gui = new GuiView()

    // Wir lagern ALLES, was blockiert oder rechnet, in den Thread aus
    val gameLogicThread = new Thread(() => {
      // 1. Erst das Spiel initialisieren
      controller.initGame()

      // 2. Kurz warten, bis das Fenster der GUI wirklich da ist
      Thread.sleep(200)

      // 3. Dann die TUI-Eingabeschleife starten
      while (true) {
        tui.handleInput()
      }
    })
    gameLogicThread.setDaemon(false)
    gameLogicThread.start()

    try {
      gui.main(Array.empty)
    } catch {
      case e: Throwable =>
        println("\n[System] GUI konnte im Docker nicht gestartet werden")
    }

  }
}