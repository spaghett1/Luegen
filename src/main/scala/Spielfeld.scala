import scala.io.StdIn._
@main def Spielfeld(): Unit =
  print("Spieleranzahl: ")
  val playerCount = readInt()
  val deckSize = 32 / playerCount
  var newdeckSize = 0

  if (deckSize < 8)
    newdeckSize = deckSize * (playerCount / 4)
  else
    newdeckSize = deckSize



  println(newdeckSize)

  

