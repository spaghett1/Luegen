package de.htwg.luegen.model.fileIO.xml

import de.htwg.luegen.model.{IGameModel, Memento}
import de.htwg.luegen.model.fileIO.IFileIO
import de.htwg.luegen.TurnState
import de.htwg.luegen.model.impl1.{Card, PlayerType}
import de.htwg.luegen.model.impl1.*
import de.htwg.luegen.model.impl1.PlayerType.{AI, Human}

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

  def stringToCards(s: String): List[Card] = {
    if (s.trim.isEmpty) Nil
    else s.split(",").map { str =>
      val suit = str.trim.take(1)
      val rank = str.trim.drop(1)
      Card(suit, rank)
    }.toList
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
      val hand = stringToCards((playerNode \ "hand").text.trim)

      val pType = (playerNode \ "type").text.trim match {
        case "AI" => AI
        case _ => Human
      }

      Player(name, hand, pType)
    }.toList

    val lastPlayedCards = stringToCards((file \ "lastPlayedCards").text.trim)

    val discarded = stringToCards((file \ "discardedCards").text.trim)

    val order = (file \ "playOrder").text.trim match {
      case "" => Nil
      case s => s.split(",").map(_.trim.toInt).toList
    }

    val ranks = (file \ "validRanks").text.trim match {
      case "" => Nil
      case s => s.split(",").map(_.trim).toList
    }

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
