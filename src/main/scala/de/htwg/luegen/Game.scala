package luegen

import de.htwg.luegen.Model.GameModel
import de.htwg.luegen.View.GameView
import de.htwg.luegen.Controller.GameController

object Game {
  @main def init(): Unit = {
    println("Spiel wird initialisiert...")

    // 1. Instanziierung des Models (Zustand)
    val model = new GameModel()

    // 2. Instanziierung des Controllers (Ablaufsteuerung/Observable)
    // Der Controller erhält die Model-Referenz.
    val controller = new GameController(model)

    // 3. Instanziierung der View (Ein-/Ausgabe/Observer)
    // Die View erhält die Controller-Referenz und registriert sich im Konstruktor selbst
    // als Observer beim Controller, wodurch die zirkuläre Abhängigkeit aufgelöst wird.
    val view = new GameView(controller)

    // 4. Start des Spiels
    controller.initGame()
  }
}