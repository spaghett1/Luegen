package luegen

import scala.io.StdIn._

object Luegen {
  case class Player(
      var number: Int,
      name: String,
      hand: List[String] = List.empty,
      var score: Int = 0,
      var queen: Int = 0
  )

  def getScore(players: Seq[Player]): Seq[String] =
    players.map(p => s"${p.name}: ${p.score} Punkte")

  def addScore(players: Seq[Player], playerNumber: Int, scoreToAdd: Int): Unit =
    players.find(_.number == playerNumber).foreach(_.score += scoreToAdd)
  /*
def reduceLives(players: Seq[Player],playerIndex: Int): Unit =
  players(playerIndex).lives -= 1
   */
  def printTable(players: Seq[Player]): Unit =
    val arr = Array(
      "╔" + "═".repeat(20) + "╗",
      "║" + " ".repeat(20) + "║",
      "║" + " ".repeat(20) + "║",
      "║" + " ".repeat(20) + "║",
      "╚" + "═".repeat(20) + "╝"
    )
  
    arr.foreach(println)

  @main def myApp(): Unit =
    println("Wilkommen bei Lügen!!!")

    // val players = playerName.map(name => Player(name))

    // Spieleranzahl
    var playerCount = 0
    while playerCount < 2 || playerCount > 8 do
      print("Spieleranzahl (2-8): ")
      try
        playerCount = readInt()
        if playerCount < 2 || playerCount > 8 then
          println("Bitte Spieleranzahl neu eingenen(2-8):")
      catch
        case _: NumberFormatException =>
          println("Ungültige Eingabe. Bitte eine Zahl eingeben.")

    // Spieler Namen abfragen
    val playerName = for i <- 1 to playerCount yield
      print(s"Name Spieler $i: ")
      readLine().trim

    val players = playerName.zipWithIndex.map { case (name, index) =>
      Player(index + 1, name)
    }

    // Spielerübersicht ausgeben
    println("\nSpielerübersicht:")
    for i <- 0 until playerCount do
      println(s"Spieler ${i + 1}: ${playerName(i)}")

    val deckSize = 32 / playerCount
    var newdeckSize = 0

    if (deckSize < 8)
      newdeckSize = deckSize * (playerCount / 4)
    else
      newdeckSize = deckSize * 4

    println(s"Deckgröße: $newdeckSize")

    addScore(players, 1, 20)
    getScore(players).foreach(println)
    printTable(players)
}
