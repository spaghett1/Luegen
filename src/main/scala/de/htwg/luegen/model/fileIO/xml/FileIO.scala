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

    val roundRank = (file \ "roundRank").text
    val playerCount = (file \ "playerCount").text.toInt
    val currentPlayerIndex = (file \ "currentPlayerIndex").text.toInt
    val turnState = TurnState.valueOf((file \ "turnState").text)
    val lastPlayerIndex = (file \ "lastPlayerIndex").text.toInt
    val lastAccusedIndex = (file \ "lastAccusedIndex").text.toInt
    val amountPlayed = (file \ "amountPlayed").text.toInt
    val players = (file \ "players" \ "player").map { playerNode =>
      val name = (playerNode \ "name").text
      val hand = (playerNode \ "hand" \ "card").map { cardNode =>
        Card(
          suit = (cardNode \"suit").text,
          rank = (cardNode \ "rank").text
        )
      }.toList

      val pType = (playerNode \ "type").text match {
        case "AI" => AI
        case _ => Human
      }

      Player(name, hand, pType)
    }.toList

    val lastPlayedCards = (file \ "lastPlayedCards" \ "card").map { cardNode =>
      Card((cardNode \ "suit").text, (cardNode \ "rank").text)
    }.toList

    val discarded = (file \ "discardedCards" \ "card").map { cardNode =>
      Card((cardNode \ "suit").text, (cardNode \ "rank").text)
    }.toList

    val order = (file \ "playOrder" \ "index").map(_.text.toInt).toList
    val ranks = (file \ "validRanks" \ "rank").map(_.text).toList

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
