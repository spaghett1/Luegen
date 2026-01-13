package de.htwg.luegen.model.impl1

trait PlayerType
case object Human extends PlayerType
case object AI extends PlayerType

object PlayerType {
  import play.api.libs.json._

  implicit val playerTypeFormat: Format[PlayerType] = new Format[PlayerType] {
    override def reads(json: JsValue): JsResult[PlayerType] = json.as[String] match {
      case "Human" => JsSuccess(Human)
      case "AI" => JsSuccess(AI)
      case _ => JsError("Unbekannter PLayerType")
    }

    override def writes(o: PlayerType): JsValue = JsString(o.toString)
  }
}
