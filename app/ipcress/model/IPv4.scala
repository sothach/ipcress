package ipcress.model

case class IPv4(value: Long) extends AnyVal with Ordered[IPv4] {
  override def compare(other: IPv4): Int = this.value compare other.value
  override def toString: String = ((1 to 4) map apply).mkString(".")
  def apply(pos: Int): Int = {
    require(pos > 0 && pos <= 4)
    val shift = (4-pos) << 3
    ((value & (0xff << shift)) >> shift).toInt
  }
  def update(pos: Int, octet: Int): IPv4 = {
    require(pos > 0 && pos <= 4)
    require(octet >= 0 && octet <= 255, s"$octet should be 0 to 255")
    val shift = (4-pos) << 3
    IPv4((value & ~(0xff << shift)) + (octet.toLong << shift))
  }
}

object IPv4 {
  def apply(ip: String): IPv4 = {
    val octets = ip.split("\\.")
    require(octets.size == 4, s"An IP string must have exactly 4 octets: $ip")
    IPv4(octets.map(_.trim.toInt)
      .map { octet =>
        require(octet >= 0 && octet <= 255, s"$octet should be 0 to 255")
        octet.toLong
      }
      .reduceLeft((acc, item) => (acc << 8) + item))
  }
}