package luegen

import de.htwg.luegen.Model.Player
import de.htwg.luegen.Model.Utils.{DeckUtils, GameUtils, PlayerUtils, TurnOrderUtils}
import de.htwg.luegen.View.Grid

import scala.annotation.tailrec

object Game {
  @main def init(): Unit = {
    import scala.util.Random
    println("Spiel wird initialisiert...")

    val players = PlayerUtils.initPlayers()
    val deck = DeckUtils.shuffle(DeckUtils.createDeck())
    
    PlayerUtils.dealCards(players, deck)

    val validOrder = TurnOrderUtils.validOrder(players)
    val startIndex = Random.nextInt(validOrder.size)
    val playOrder = TurnOrderUtils.determinePlayOrder(validOrder, startIndex)

    val grid1 = new Grid(players)
    println(s"Das Spiel startet mit ${players(playOrder.head).name}!")
    playGame(players, playOrder, 0, grid1)
  }

  @tailrec
  def playGame(players: List[Player], validOrder: List[Int], currentIndex: Int, grid: Grid): Unit = {

    val player = players(validOrder(currentIndex))
    val prevPlayer = players(validOrder((currentIndex - 1 + validOrder.size) % validOrder.size))

    println(s"${player.name} ist dran")
    grid.printGrid(discardedCards.length)
    val outcome =
      if (currentIndex == 0) GameUtils.firstPlayerRound(player)
      else GameUtils.turn(player, prevPlayer)

    val nextIndex = TurnOrderUtils.nextPlayerIndex(outcome, currentIndex, validOrder.length)
    playGame(players, validOrder, nextIndex, grid)
  }
}
