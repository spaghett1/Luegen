package de.htwg.luegen.Controller

import de.htwg.luegen.Model.*
import de.htwg.luegen.Model.Utils.*
import de.htwg.luegen.View.GameView
import de.htwg.luegen.View.Observer

import scala.util.Random
import scala.annotation.tailrec

// Definiere Observable-Trait (kann in interfaces.scala oder hier sein)
trait Observable {
  def registerObserver(o: Observer): Unit
  def notifyObservers(): Unit
}

class GameController(val model: GameModel) extends Observable {

  // Referenz zur View wird später gesetzt (zirkuläre Abhängigkeit)
  private var view: GameView = _
  private val observers:
    scala.collection.mutable.ListBuffer[Observer] =
    scala.collection.mutable.ListBuffer()

  // Steuerung des Spielablaufs
  var validOrder: List[Int] = List.empty
  var currentIndex: Int = 0

  // -------------------------------------------------------------------------
  // Observable-Implementierung
  // -------------------------------------------------------------------------
  override def registerObserver(o: Observer): Unit = {
    observers += o
    if (o.isInstanceOf[GameView]) view = o.asInstanceOf[GameView]
  }

  override def notifyObservers(): Unit = observers.foreach(_.updateDisplay())

  // -------------------------------------------------------------------------
  // Logik aus Game.scala (wird Teil des Controllers)
  // -------------------------------------------------------------------------

  def initializeGame(): Unit = {
    // 1. Initialisierung (View holt Input)
    val numPlayers = view.getNum
    val playersList = (1 to numPlayers).map(view.getPlayerName).toList

    // 2. Model-Aktion
    model.players = playersList.map(name => Player(name))
    val deck = DeckUtils.shuffle(DeckUtils.createDeck())
    PlayerUtils.dealCards(model.players, deck)

    // 3. Game-Ablauf-Logik
    validOrder = TurnOrderUtils.validOrder(model.players)
    val startIndex = Random.nextInt(validOrder.size)
    validOrder = TurnOrderUtils.determinePlayOrder(validOrder, startIndex)

    // 4. View-Ausgabe & Start des Loops
    println(s"Das Spiel startet mit ${model.players(validOrder.head).name}!")
    playGameLoop()
  }

  // Beispiel für eine Action-Methode, die Input, Model-Logik und View-Output koordiniert
  private def firstRoundAction(player: Player): Outcomes = {
    val chosenRank = view.callRank()

    // View holt Input
    view.displayPlayerHand(player)
    val selectedCards = view.selectCards(player)

    // Model ändert Zustand
    val outcome = model.firstPlayerRound(player, chosenRank, selectedCards)
    view.displayLayedCards(player, selectedCards) // View-Output

    outcome
  }

  // -------------------------------------------------------------------------
  // Methoden, um der View Daten zu geben (fetch Data)
  // -------------------------------------------------------------------------
  def getCurrentPlayers: List[Player] = model.players

  def getDiscardedCardCount: Int = model.getDiscardedCardCount

  def getCurrentPlayerIndex: Int = validOrder(currentIndex)
}



