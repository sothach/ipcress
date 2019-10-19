package ipcress.model

import scala.annotation.tailrec

case class Series(min: Int, max: Int) {
  val isSingular: Boolean = min == max
}
object Series {
  def apply(value: Int): Series = Series(value, value)

  @tailrec
  def rangify(items: Seq[Int], last: Option[Int] = None,
              acc: Seq[Series] = Seq.empty): Seq[Series] =
    if (items.nonEmpty) {
      val head = items.head
      rangify(items.drop(1), Some(head), last match {
        case None => Seq(Series(head))
        case Some(l) if head == l + 1 =>
          acc.dropRight(1) :+ acc.last.copy(max = head)
        case Some(_) => acc :+ Series(head)
      })
    } else {
      acc
    }
}