package de.htwg.luegen.Controller

import de.htwg.luegen.Model.Utils.Memento

case class HistoryEntry(model: Memento, command: GameCommand)
