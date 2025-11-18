package de.htwg.luegen.View

import de.htwg.luegen.Model.Utils.DeckUtils
import de.htwg.luegen.Model.Utils.DeckUtils.Ranks
import de.htwg.luegen.Model.{Card, Player}

object InputUtils {
  import DeckUtils.*

  import scala.annotation.tailrec
  import scala.io.StdIn
  import scala.util.Try


  @tailrec
  def retryUntilValid[T](prompt: String, parse: String => Option[T], validate: T => Boolean): T = {
    println(prompt)
    val input = Option(StdIn.readLine()).getOrElse("")
    val parsedOpt = parse(input)
    parsedOpt match {
      case Some(value) if validate(value) => value
      case _ =>
        println("Ungueltige Eingabe! Bitte erneut.")
        retryUntilValid(prompt, parse, validate)
    }
  }

}
