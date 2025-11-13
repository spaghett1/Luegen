package de.htwg.luegen.View

import de.htwg.luegen.model.* // Importiert Model-Klassen (Player, Card, etc.)
import luegen.controller.GameController // Braucht den Controller, um Daten abzurufen

// Definiere Observer-Trait (kann in interfaces.scala oder hier sein)
trait Observer {
  def updateDisplay(): Unit
}

class GameView(controller: GameController) extends Observer {
  // View hält Instanz von Grid
  private val grid = new Grid(controller.getCurrentPlayers)

  // Die View registriert sich beim Controller (Observer-Muster)
  controller.registerObserver(this)

  // -------------------------------------------------------------------------
  // Logik aus InputUtils.scala und Grid.scala (wird Teil der View)
  // -------------------------------------------------------------------------

  // Implementiert die Observer-Schnittstelle
  override def updateDisplay(): Unit = {
    // 1. Hole aktuelle Daten vom Controller (fetch Data)
    val players = controller.getCurrentPlayers
    val discardedCount = controller.getDiscardedCardCount

    // 2. Aktualisiere die Anzeige
    displayPlayerHand(players(controller.getCurrentPlayerIndex)) // Beispiel: Aktuellen Spieler anzeigen
    grid.printGrid(discardedCount)

    // (Weitere Ausgaben wie Spielstand, Fehlermeldungen)
  }

  // Alle I/O-Methoden aus InputUtils werden hier zu Methoden der View
  // Die Methoden geben nun die Rohdaten an den Controller zurück.
  def getNum: Int = InputUtils.getNum // Delegate an das interne InputUtils Objekt
  def getPlayerName(i: Int): String = InputUtils.getPlayerName(i)
  def callRank(): String = InputUtils.callRank()
  def selectCards(player: Player): List[Card] = InputUtils.selectCards(player)
  def readYesNo(player: Player): Boolean = InputUtils.readYesNo(player)

  def displayPlayerHand(player: Player): Unit = InputUtils.printPlayerHand(player) // Ausgabe
  def displayLayedCards(player: Player, cards: List[Card]): Unit = InputUtils.playerLaysCards(player, cards) // Ausgabe
  // Andere Ausgaben
  def displayPrompt(prompt: String): Unit = InputUtils.printPrompt(prompt)
}
