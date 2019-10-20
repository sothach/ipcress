package ipcress.services

import ipcress.model.{DigestRequest, Format, IPv4, Series}
import play.api.libs.json.{JsString, Json}

import scala.collection.immutable.SortedMap

trait Formatter {
  protected def renderSingle(base: IPv4, series: Series): String
  protected def renderRange(base: IPv4, series: Series): String
  protected def serialize(results: Iterable[String]): String

  private def render(base: IPv4, series: Series): String = if (series.isSingular) {
    renderSingle(base, series)
  } else {
    renderRange(base, series)
  }

  def format[T](ipGroups: Map[IPv4, scala.Seq[Series]]): String =
    serialize(SortedMap(ipGroups.toSeq:_*) flatMap { case (base, items) =>
      items map (item => render(base, item))
    })
}
object Formatter {
  def format(frame: DigestRequest): String =
    (frame.format match {
      case Format.JSON => new JsonFormatter
      case Format.PLAIN => new PlainFormatter
    }).format(frame.result)
}

class PlainFormatter extends Formatter {
  protected def renderSingle(base: IPv4, series: Series): String =
    s"${base(4) = series.min}"
  protected def renderRange(base: IPv4, series: Series): String =
    s"${base(4) = series.min}-${base(4) = series.max}"
  protected def serialize(results: Iterable[String]): String = results.mkString("\n")
}

class JsonFormatter extends Formatter {
  private def asJson(values: Seq[IPv4]) =
    Json.stringify(Json.obj("ip" -> values.map(ip => JsString(ip.toString))))
  protected def renderSingle(base: IPv4, series: Series): String =
    asJson(Seq(base(4) = series.min))
  protected def renderRange(base: IPv4, series: Series): String =
    asJson(Seq(base(4) = series.min, base(4) = series.max))
  protected def serialize(results: Iterable[String]): String = "["+results.mkString(",")+"]"
}
