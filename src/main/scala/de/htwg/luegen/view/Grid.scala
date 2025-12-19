package de.htwg.luegen.view

import de.htwg.luegen.model.impl1.Player

case class Grid(
  text: Array[String] = Array.fill(11)(""),
  numberPos: Int = 0,
  lastPlayers: List[Player] = Nil
) {
  
  def initGrid(players: List[Player]): (Array[String], Int) = {

    val newText = Array.fill(11)("")

    val names = players
      .map(p => if p.name.length % 2 == 0 then p.name else s" ${p.name}")
      .padTo(8, "")
      .toArray

    val sizeVert = (math.max(names(2).length, names(6).length))
    names(2) = " " * (sizeVert - names(2).length) + names(2)
    names(6) = " " * (sizeVert - names(6).length) + names(6)
    val spacesVert = " " * (sizeVert + 1)

    newText(1) = spacesVert + "+" + "-" * 22 + "+"
    newText(9) = newText(1)
    newText(2) = spacesVert + "|" + " " * 22 + "|"
    newText(8) = newText(2)
    newText(4) = spacesVert + "|" + " " * 8 + "|" + " " * 4 + "|" + " " * 8 + "|"
    newText(6) = newText(4)

    val spacesTop = " " * ((24 - names(0).length) / 2)
    newText(0) = spacesVert + spacesTop + names(0) + spacesTop
    newText(3) = spacesVert + "|" + " " * 8 + "+----+" + " " * 8 + "|"
    newText(7) = newText(3)
    newText(5) = s"${names(2)} |${" " * 8}|  0 |${" " * 8}| ${names(3)}"
    val spacesBot = " " * ((24 - names(1).length) / 2)
    newText(10) = spacesVert + spacesBot + names(1) + spacesBot
    if (players.size > 4) {
      val spacesTop = " " * (22 - names(0).length - names(4).length)
      newText(0) = spacesVert + " " + names(0) + spacesTop + names(4) + " "
      if (players.size > 5) {
        val spacesBot = " " * (22 - names(1).length - names(5).length)
        newText(10) = spacesVert + " " + names(1) + spacesBot + names(5)
        if (players.size > 6) {
          newText(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| "
          newText(5) = s"${spacesVert} |${" " * 8}|  0 |${" " * 8}| ${names(3)} "
          newText(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| "
          if (players.size > 7) {
            newText(3) = s"${names(2)} |${" " * 8}+----+${" " * 8}| ${names(3)}"
            newText(5) = s"${spacesVert}|${" " * 8}|  0 |${" " * 8}| "
            newText(7) = s"${names(6)} |${" " * 8}+----+${" " * 8}| ${names(7)}"
          }
        }
      }
    }

    val newNumberPos = sizeVert + 11

    (newText, newNumberPos)
  }

  def updateGridWithPlayers(players: List[Player]): Grid = {
    if (players == lastPlayers) {
      this
    } else {
      val (newText, newNumberPos) = initGrid(players)
      this.copy(
        text = newText,
        numberPos = newNumberPos,
        lastPlayers = players
      )
    }
  }

  def updateGridWithNumber(discardedCards: Int): String = {
    val line = text(5)
    val card = if (discardedCards > 9) s" $discardedCards " else s"  $discardedCards "
    val newline = line.patch(numberPos, card, 4)
    text(5) = newline
    text.mkString("\n")
  }

  def clearScreen(): Unit = {
    print("\n" * 20)
  }
}
