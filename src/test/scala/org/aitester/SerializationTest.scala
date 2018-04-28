package org.aitester

import org.json4s.JsonDSL.WithDouble._
import org.json4s.native.JsonMethods._
import org.aitester.exchange.SecurityInfo

object SerializationTest extends App{

  val securityInfo = SecurityInfo("INTC", 10.67)

  val secInfoJson = ( "securityinfo" ->(
          ( "ticker" -> securityInfo.ticker)
          ~ ( "price" -> securityInfo.price)
    ))

  println(compact(render(secInfoJson)))
}
