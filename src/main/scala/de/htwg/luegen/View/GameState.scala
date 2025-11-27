package de.htwg.luegen.View

trait GameState {
  def handle(controller: GameController, view: GameView): Unit
}

case object
