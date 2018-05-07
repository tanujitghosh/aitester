package org.aitester.exchange

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

case class SecurityInfo(ticker: String, price : Double){
  implicit val formats = Serialization.formats(NoTypeHints)

  def asJson : String = write(this)
}

object SecurityInfo{
  implicit val formats = Serialization.formats(NoTypeHints)

  val prefix = "SecurityInfo"

  def fromJson(jsonStr: String) : SecurityInfo = read[SecurityInfo](jsonStr)

  def apply(jsonStr: String) : SecurityInfo = fromJson(jsonStr)

  def key(secInfo: SecurityInfo) : String = key(secInfo.ticker)

  def key(ticker: String) : String = s"${prefix}:${ticker}"
}

case class QuoteInfo(ticker:String, action: String, timestamp: Long, qty: Int, price: Double){
  implicit val formats = Serialization.formats(NoTypeHints)

  def asJson : String = write(this)
}

object QuoteInfo {
  implicit val formats = Serialization.formats(NoTypeHints)
  val prefix = "QuoteInfo"

  def fromJson(jsonStr: String) : QuoteInfo = read[QuoteInfo](jsonStr)

  def apply(jsonStr : String) : QuoteInfo = fromJson(jsonStr)

  def key(quoteInfo: QuoteInfo) : String = key(quoteInfo.ticker, quoteInfo.action)

  def key(ticker: String, action: String) : String = s"${prefix}:${ticker}:${action}"
}

case class QuotesList(ticker:String, quoteList: Seq[QuoteInfo]){
  implicit val formats = Serialization.formats(NoTypeHints)
  def asJson : String = write(this)
}

object QuotesList {
  implicit val formats = Serialization.formats(NoTypeHints)
  val prefix = "QuotesList"

  def fromJson(jsonStr: String) : QuotesList = read[QuotesList](jsonStr)

  def apply(jsonStr:String): QuotesList = fromJson(jsonStr)

  def key(quotesList: QuotesList) : String = key(quotesList.ticker)

  def key(ticker: String) : String = s"${prefix}:${ticker}"
}

case class TradeInfo(ticker:String, action: String, timestamp: Long, qty: Int, price: Double, status: String = "REQUESTED"){
  implicit val formats = Serialization.formats(NoTypeHints)
  def asJson : String = write(this)
}

object TradeInfo {
  implicit val formats = Serialization.formats(NoTypeHints)
  val prefix = "TradeInfo"

  def fromJson(jsonStr: String) : TradeInfo = read[TradeInfo](jsonStr)

  def apply(jsonStr : String) = fromJson(jsonStr)

  def key(tradeInfo: TradeInfo) : String = key(tradeInfo.ticker, tradeInfo.action)

  def key(ticker: String, action: String) : String = s"${prefix}:${ticker}:${action}"
}
