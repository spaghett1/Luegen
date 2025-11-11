package luegen

class Grid(players: List[Player]) {
  private val text = Array.fill(11)("")
  private var numberPos = 0
  
  initGrid(players)
  
  private def initGrid(players: List[Player]): Unit = {

    val names = players
      .map(p => if p.name.length % 2 == 0 then p.name else s" ${p.name}")
      .padTo(8, "")
      .toArray

    val sizeVert = (math.max(names(2).length, names(6).length))
    names(2) = " " * (sizeVert - names(2).length) + names(2)
    names(6) = " " * (sizeVert - names(6).length) + names(6)
    val spacesVert = " " * (sizeVert + 1)

    text(1) = spacesVert + "+" + "-" * 22 + "+"
    text(9) = text(1)
    text(2) = spacesVert + "|" + " " * 22 + "|"
    text(8) = text(2)
    text(4) = spacesVert + "|" + " " * 8 + "|" + " " * 4 + "|" + " " * 8 + "|"
    text(6) = text(4)

    val spacesTop = " " * ((24 - names(0).length) / 2)
    text(0) = spacesVert + spacesTop + names(0) + spacesTop
    text(3) = spacesVert + "|" + " " * 8 + "+----+" + " " * 8 + "|"
    text(7) = text(3)
    text(5) = s"${names(2)} |${" " * 8}|  0 |${" " * 8}| ${names(3)}"
    val spacesBot = " " * ((24 - names(1).length) / 2)
    text(10) = spacesVert + spacesBot + names(1) + spacesBot
    if (players.size > 4) {
      val spacesTop = " " * (22 - names(0).length - names(4).length)
      text(0) = spacesVert + " " + names(0) + spacesTop + names(4) + " "
      if (players.size > 5) {
        val spacesBot = " " * (22 - names(1).length - names(5).length)
        text(10) = spacesVert + " " + names(1) + spacesBot + names(5)
        if (players.size > 6) {
          text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| "
          text(5) = s"${spacesVert} |${" " * 8}|  0 |${" " * 8}| ${names(3)} "
          text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| "
          if (players.size > 7) {
            text(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| ${names(3)}"
            text(5) = s"${spacesVert}|${" " * 8}|  0 |${" " * 8}| "
            text(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| ${names(7)}"
          }
        }
      }
    }

    numberPos = sizeVert + 11
  }

  def printGrid(discardedCards: Int): Unit = {
    val line = text(5)
    val card = if (discardedCards > 9) s" $discardedCards " else s"  $discardedCards "
    val newline = line.patch(numberPos, card, 4)
    text(5) = newline
    println(text.mkString("\n"))
  }

  def clearScreen(): Unit = {
    print("\u001b[2J")
    print("\u001b[H")
    println("\n" * 20)
  }
}
