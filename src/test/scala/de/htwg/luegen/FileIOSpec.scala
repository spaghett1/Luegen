package de.htwg.luegen.model.fileIO

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.model.impl1.{GameModel, Card, Player, PlayerType}
import de.htwg.luegen.model.impl1.PlayerType.{Human, AI}
import de.htwg.luegen.model.fileIO.xml.FileIO as XmlIO
import de.htwg.luegen.model.fileIO.json.FileIO as JsonIO
import de.htwg.luegen.TurnState

class FileIOSpec extends AnyWordSpec with Matchers {

  "FileIO" should {

    // Test-Daten: Wir nutzen die toString-Repräsentation der Karten
    val card1 = Card("♠", "A")   // "♠A"
    val card2 = Card("♥", "10")  // "♥10"
    val card3 = Card("♦", "7")   // "♦7"

    def createComplexModel = {
      val player1 = Player("Alice", List(card1), Human)
      val player2 = Player("Bot", List(card2), AI)

      GameModel(
        playerCount = 2,
        players = List(player1, player2),
        discardedCards = List(card3),
        lastPlayedCards = List(card2),
        roundRank = "10",
        playOrder = List(0, 1),
        currentPlayerIndex = 0,
        turnState = TurnState.NeedsChallengeDecision
      )
    }

    "save and load via XML (mit Karten als String-Reihe)" in {
      val xmlIO = new XmlIO
      val originalModel = createComplexModel
      println(originalModel.playOrder)

      xmlIO.save(originalModel)
      val loaded = xmlIO.load

      // 1. Check der Spieler und ihrer Hand (als Reihe geladen)
      loaded.getPlayers.size shouldBe 2
      loaded.getPlayers.head.name shouldBe "Alice"
      // Hier prüfen wir, ob die Karte ♠A wieder korrekt aus dem String erzeugt wurde
      loaded.getPlayers.head.hand should contain (card1)

      // 2. Check der anderen Kartenlisten
      loaded.getPlayedCards should contain (card2)
      loaded.getDiscardedCards should contain (card3)

      // 3. Check der restlichen im Screenshot sichtbaren Logik
      loaded.getRoundRank shouldBe "10"
      val memento = loaded.createMemento()
      memento.playOrder shouldBe List(0, 1)
    }

    "save and load via JSON" in {
      val jsonIO = new JsonIO
      val originalModel = createComplexModel
      jsonIO.save(originalModel)
      val loaded = jsonIO.load

      loaded.getPlayerCount shouldBe originalModel.getPlayerCount
      loaded.getPlayedCards shouldBe originalModel.getPlayedCards
    }
  }
}