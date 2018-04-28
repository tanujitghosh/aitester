package org.aitester.exchange

import org.json4s.DefaultFormats
import org.json4s.JsonDSL.WithDouble._
import org.json4s.native.JsonMethods._

case class SecurityInfo(ticker: String, price : Double){
  override def toString: String = s"securityInfo:${ticker}:${price}"

  def asJson : String = compact(render( (( "ticker" -> ticker)
    ~ ( "price" -> price)
    )))
}
object SecurityInfo{
  implicit val formats = DefaultFormats

  def fromJson(jsonStr: String) : SecurityInfo = parse(jsonStr).extract[SecurityInfo]

  def apply(jsonStr: String) = fromJson(jsonStr)

  def key(secInfo: SecurityInfo) : String = s"securityInfo:${secInfo.ticker}"

  def key(ticker: String) : String = s"securityInfo:${ticker}"
}

case class QuoteInfo(ticker:String, action: String, timestamp: Long, qty: Int, price: Double){
  override def toString: String = s"quoteInfo:${ticker}:${action}:${timestamp}:${qty}:${price}"


  def asJson : String = compact(render((
            ("ticker" -> ticker)
            ~ ( "action" -> action)
            ~ ( "timestamp" -> timestamp)
            ~ ( "qty" -> qty)
            ~ ( "price" -> price )
    )))
}

object QuoteInfo {
  implicit val formats = DefaultFormats

  def fromJson(jsonStr: String) : QuoteInfo = parse(jsonStr).extract[QuoteInfo]

  def apply(jsonStr : String) = fromJson(jsonStr)

  def key(quoteInfo: QuoteInfo) : String = s"quoteInfo:${quoteInfo.ticker}:${quoteInfo.action}"

  def key(ticker: String, action: String) : String = s"quoteInfo:${ticker}:${action}"
}

