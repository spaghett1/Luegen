package de.htwg.luegen.model.fileIO.xml

import de.htwg.luegen.model.{IGameModel, Memento}
import de.htwg.luegen.model.fileIO.IFileIO
import de.htwg.luegen.TurnState
import de.htwg.luegen.model.impl1.{Card, PlayerType}
import de.htwg.luegen.model.impl1.*

import scala.xml.{PrettyPrinter, XML}
import java.io.{File, PrintWriter}

class FileIO extends IFileIO {
  override def save(game: IGameModel): Unit = {
    val xml = game.createMemento().toXml
    val prettyPrinter = new PrettyPrinter(120, 4)
    val formatted = prettyPrinter.format(xml)

    val pw = new java.io.PrintWriter(new java.io.File("game.xml"))
    pw.write(formatted)
    pw.close()
  }

  override def load: IGameModel = {
    val file = scala.xml.XML.loadFile("game.xml")

    val roundRank = (file \ "roundRank").text.trim
    val temp = (file \ "playerCount")
    val playerCount = (file \ "playerCount").text.trim.toInt
    val currentPlayerIndex = (file \ "currentPlayerIndex").text.trim.toInt
    val turnState = TurnState.valueOf((file \ "turnState").text.trim)
    val lastPlayerIndex = (file \ "lastPlayerIndex").text.trim.toInt
    val lastAccusedIndex = (file \ "lastAccusedIndex").text.trim.toInt
    val amountPlayed = (file \ "amountPlayed").text.trim.toInt
    val players = (file \ "players" \ "player").map { playerNode =>
      val name = (playerNode \ "name").text.trim
      val hand = (playerNode \ "hand" \ "card").map { cardNode =>
        Card(
          suit = (cardNode \"suit").text.trim,
          rank = (cardNode \ "rank").text.trim
        )
      }.toList

      val pType = (playerNode \ "type").text.trim match {
        case "AI" => AI
        case _ => Human
      }

      Player(name, hand, pType)
    }.toList

    val lastPlayedCards = (file \ "lastPlayedCards" \ "card").map { cardNode =>
      Card((cardNode \ "suit").text.trim, (cardNode \ "rank").text.trim)
    }.toList

    val discarded = (file \ "discardedCards" \ "card").map { cardNode =>
      Card((cardNode \ "suit").text.trim, (cardNode \ "rank").text.trim)
    }.toList

    val order = (file \ "playOrder" \ "index").map(_.text.trim.toInt).toList
    val ranks = (file \ "validRanks" \ "rank").map(_.text.trim).toList

    val memento = Memento(
      discardedCards = discarded,
      roundRank = roundRank,
      lastPlayedCards = lastPlayedCards,
      playerCount = playerCount,
      players = players,
      currentPlayerIndex = currentPlayerIndex,
      lastPlayerIndex = lastPlayerIndex,
      validRanks = ranks,
      playOrder = order,
      turnState = turnState,
      amountPlayed = amountPlayed,
      lastAccusedIndex = lastAccusedIndex
    )

    GameModel().restoreMemento(memento)
  }
}
