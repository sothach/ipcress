import ipcress.model.{IPv4, Series}
import ipcress.services.IpDigester
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.collection.immutable.SortedMap

class IpGroupSpec extends WordSpecLike with MustMatchers {

  "An IPv4 address" should {
    val subject = IPv4("127.0.0.1")
    "be represented as a long integer" in {
      subject.value must be(2130706433)
    }
    "return an octet value" in {
      subject(1) must be(127)
    }
    "be transformed" in {
      (subject(4) = 151) must be(IPv4("127.0.0.151"))
    }
  }

  "An IPv4 with a large octet in the first position" should {
    val subject = IPv4("212.0.0.1")
    "be represented as a long integer" in {
      subject.value must be(3556769793L)
    }
    "return an octet value" in {
      subject(1) must be(212)
    }
    "be transformed" in {
      (subject(4) = 151) must be(IPv4("212.0.0.151"))
    }
  }

  "useful error messages" should {
    "be returned when an attempt to update IP with an invalid octet" in {
      the [IllegalArgumentException] thrownBy {
        IPv4("127.0.0.1")(4) = 256
      } must have message "requirement failed: 256 should be 0 to 255"
    }
    "be returned when an attempt to parse an IP string with an invalid octet is made" in {
      the [IllegalArgumentException] thrownBy {
        IPv4("123.123.123.456")
      } must have message "requirement failed: 456 should be 0 to 255"
    }
    "be returned when an attempt to parse an IP string with an incorrect number of octets is made" in {
      the [IllegalArgumentException] thrownBy {
        IPv4("123.123.123")
      } must have message "requirement failed: An IP string must have exactly 4 octets: 123.123.123"
    }
  }

  "A list of IP addresses" should {
    "be digested" in {
      val ipNumbers = loadDataFile("ipnumbers.dat")
      val grouped = IpDigester(ipNumbers)
      list(grouped) must be(expected)
    }
  }

  "A second list of IP addresses" should {
    "also be digested" in {
      val ipNumbers = loadDataFile("ipnumbers2.dat")
      val grouped = IpDigester(ipNumbers)
      list(grouped) must be(expected2)
    }
  }

  private val expected = Seq(
    "99.243.62.20",
    "99.243.64.40-99.243.64.42",
    "99.244.84.79",
    "99.244.106.35",
    "99.244.121.1",
    "99.244.121.59-99.244.121.61",
    "99.244.156.149",
    "99.244.178.5",
    "99.244.198.185",
    "99.245.55.191",
    "99.245.86.16",
    "99.245.110.185",
    "99.245.157.208-99.245.157.210",
    "99.245.157.220-99.245.157.221",
    "99.245.157.230-99.245.157.231",
    "99.245.157.240",
    "99.245.159.212",
    "99.245.168.213",
    "99.246.19.132",
    "99.246.32.68",
    "99.246.47.117",
    "99.246.163.95",
    "99.246.183.89",
    "99.246.189.50",
    "126.89.72.1-126.89.72.255"
  )

  private val expected2 = Seq(
    "80.85.101.12",
    "159.20.25.208-159.20.25.215",
    "159.20.31.44-159.20.31.47",
    "212.56.134.24-212.56.134.31",
    "212.56.137.88-212.56.137.95"
  )

  private def loadDataFile(resource: String): Seq[String] = {
    val stream = getClass.getResourceAsStream(s"/$resource")
    scala.io.Source.fromInputStream(stream).getLines.toSeq
  }

  private def list(groups: Map[IPv4, Seq[Series]]) =
    SortedMap(groups.toSeq:_* )flatMap {
      case (base, items) =>
        items map {
          case series if series.isSingular =>
            s"${base(4) = series.min}"
          case series =>
            s"${base(4) = series.min}-${base(4) = series.max}"
        }
    }
}

