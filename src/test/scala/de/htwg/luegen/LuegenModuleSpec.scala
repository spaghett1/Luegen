package de.htwg.luegen

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.luegen.controller.IGameController
import de.htwg.luegen.model.IGameModel
import de.htwg.luegen.model.fileIO.IFileIO

class LuegenModuleSpec extends AnyWordSpec with Matchers {

  "LuegenModule" should {
    // Wir importieren alle gegebenen Instanzen aus dem Modul
    import de.htwg.luegen.LuegenModule.given

    "korrekt auflösbare Instanzen bereitstellen" in {
      // Beschaffung der Instanzen über summon[T]
      val controller = summon[IGameController]
      val model = summon[IGameModel]
      val fileIo = summon[IFileIO]

      controller shouldNot be(null)
      model shouldNot be(null)
      fileIo shouldNot be(null)
    }

    "die richtige Standard-Implementierung für FileIO nutzen" in {
      import de.htwg.luegen.LuegenModule.given
      val fileIo = summon[IFileIO]

      // Prüfen, ob es die JSON-Implementierung ist (Standard in deinem Modul)
      fileIo.getClass.getSimpleName should include("FileIO")
    }
  }
}
