package luegen

import luegen.model.GameModel
import luegen.view.GameView
import luegen.controller.GameController

object Game {
  @main def init(): Unit = {
    println("Spiel wird initialisiert...")

    // 1. Instanziierung des Models (Zustand)
    val model = new GameModel(List.empty)

    // 2. Instanziierung des Controllers (Ablaufsteuerung/Observable)
    val controller = new GameController(model)

    // 3. Instanziierung der View (Ein-/Ausgabe/Observer)
    // Die View registriert sich automatisch beim Controller im Konstruktor
    val view = new GameView(controller)

    // 4. Start des Spiels
    controller.initializeGame()
  }
}

