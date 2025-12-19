package de.htwg.luegen.controller.impl1

import de.htwg.luegen.model.impl1.Utils.Memento
import de.htwg.luegen.controller.impl1.GameCommand

case class HistoryEntry(model: Memento, command: GameCommand)
 