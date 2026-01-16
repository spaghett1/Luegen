package de.htwg.luegen.model.impl1

enum PlayerType {
  case Human
  case AI
}

object PlayerType {
  import play.api.libs.json._

  implicit val playerTypeFormat: Format[PlayerType] = new Format[PlayerType] {
    override def reads(json: JsValue): JsResult[PlayerType] = json.asOpt[String] match {
      case Some("Human") => JsSuccess(Human)
      case Some("AI") => JsSuccess(AI)
      case _ => JsError("Unknown PLayerType")
    }

    override def writes(o: PlayerType): JsValue = JsString(o.toString)

  }
}
