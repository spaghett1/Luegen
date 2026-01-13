package de.htwg.luegen.controller.impl1

import de.htwg.luegen.controller.impl1.GameCommand
import de.htwg.luegen.model.Memento

case class HistoryEntry(model: Memento, command: GameCommand)
 