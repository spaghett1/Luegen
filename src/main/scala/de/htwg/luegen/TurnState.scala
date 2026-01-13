package de.htwg.luegen

enum TurnState {
  case Played
  case NoTurn
  case NoChallenge
  case ChallengedLieWon
  case ChallengedLieLost
  case Invalid
  case NeedsPlayerCount
  case NeedsPlayerNames
  case NeedsRankInput
  case NeedsCardInput
  case NeedsChallengeDecision
}

object TurnState {
  import play.api.libs.json._
  implicit val tunStateFormat: Format[TurnState] = new Format[TurnState] {
    override def reads(json: JsValue): JsResult[TurnState] = json.asOpt[String] match {
      case Some(name) =>
        try {
          JsSuccess(TurnState.valueOf(name))
        } catch {
          case _: IllegalArgumentException => JsError("Unbekannter TurnState")
        }

      case None => JsError("String erwartet")
    }

    override def writes(o: TurnState): JsValue = JsString(o.toString)
  }

}