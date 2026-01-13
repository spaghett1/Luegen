package de.htwg.luegen.model.fileIO

import de.htwg.luegen.model.IGameModel

trait IFileIO {
  def load: IGameModel
  def save(game: IGameModel): Unit
}
