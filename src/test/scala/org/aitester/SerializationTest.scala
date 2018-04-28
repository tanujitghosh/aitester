package org.aitester

import org.aitester.exchange.SecurityInfo
import org.json4s.DefaultFormats

object SerializationTest extends App{

  implicit val formats = DefaultFormats

  val securityInfo = SecurityInfo("INTC", 10.67)

  println(securityInfo.asJson)

  val secJson = """{ "ticker" : "AMZN", "price":100.56}"""

  val securityInfo1 = SecurityInfo(secJson)

  println(securityInfo1.toString)
}
