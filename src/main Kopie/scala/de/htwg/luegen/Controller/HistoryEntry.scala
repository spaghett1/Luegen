package de.htwg.luegen.controller

import de.htwg.luegen.model.Utils.Memento

case class HistoryEntry(model: Memento, command: GameCommand)
