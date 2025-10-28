import scala.io.StdIn._

case class Player(name: String, hand: List[String] = List.empty, var score: Int = 0, var lives: Int = 3)

val players = playerName.map(name => Player(name))

def getScore(players: Seq[Player]): Map[String, Int] =
  players.map(p => p.name -> p.score).toMap

def addscore(players: Seq[Player], playerName: String, scoreToAdd: Int): Unit =
  players.find(_.name == playerName).foreach(_.score += scoreToAdd)



@main def Spielfeld(): Unit =
  println("Wilkommen bei Lügen!!!")

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


  

