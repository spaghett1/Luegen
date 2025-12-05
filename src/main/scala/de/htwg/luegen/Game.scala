package de.htwg.luegen

import de.htwg.luegen.Model.GameModel
import de.htwg.luegen.Controller.GameController
import de.htwg.luegen.View.GameView
import de.htwg.luegen.View.GuiView

object Game {

  def main(args: Array[String]): Unit = {

    // Debugging-Ausgabe (zur Kontrolle)
    println(s"DEBUG: Argumente empfangen: [${args.mkString(", ")}], Laenge: ${args.length}")
    println("Spiel wird initialisiert...")

    val model = new GameModel()
    val controller = new GameController(model)

    // KORREKTUR: Prüfe, ob die Argumentenliste "--gui" enthält
    val startGui = args.contains("--gui")

    if (startGui) {
      println("Starte Grafische Benutzeroberflaeche (GUI)...")

      // Startet die ScalaFX-Anwendung (GuiView)
      new GuiView(controller).main(args)
    } else {
      println("Starte Terminal Benutzeroberflaeche (TUI)...")

      // TUI-Start-Logik
      val view = new GameView(controller)
      controller.registerObserver(view)
      controller.initGame()
    }
  }
}