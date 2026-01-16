package de.htwg.luegen

import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.impl1.GameModel

import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.controller.impl1.GameController

import de.htwg.luegen.model.fileIO._

object LuegenModule {
  given IGameModel = GameModel()

  given IGameController = GameController()

  given IFileIO = xml.FileIO()
}
