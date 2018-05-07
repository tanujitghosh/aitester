package org.aitester

import java.io.File

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import org.aitester.exchange._
import org.apache.commons.io.FileUtils
import org.scalatest.{Matchers, WordSpec}

class ExchangeTest extends WordSpec with Matchers with ScalatestRouteTest {

  val exchange = new Exchange
  deleteLevelDbFolder("test/exchange-db")
  val levelDBStore = new LevelDBStore("test/exchange-db")
  val quotesFlowActorProbe = TestProbe()
  val tradesFlowActorProbe = TestProbe()
  val route = exchange.route(levelDBStore, quotesFlowActorProbe.ref , tradesFlowActorProbe.ref)

  "Exchange service" should {
    "return bad request" in {
      Get("/security/AMZN") ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
    "return json response" in {
      Put("/security/INTC/10.46") ~> route ~> check {
        val securityInfo = SecurityInfo(responseAs[String])
        securityInfo.ticker shouldEqual "INTC"
        securityInfo.price shouldEqual 10.46
        status shouldEqual StatusCodes.OK
      }
    }
    "return quoteinfo and message to probe" in {
      Put("/quote/INTC/buy/100/10.47") ~> route ~> check {
        val quoteInfo = QuoteInfo(responseAs[String])
        quoteInfo.ticker shouldEqual "INTC"
        quoteInfo.action shouldEqual "buy"
        quoteInfo.qty shouldEqual 100
        quoteInfo.price shouldEqual 10.47
        status shouldEqual StatusCodes.OK
      }
    }
    "return quoteslist" in {
      Get("/quoteslist/INTC") ~> route ~> check {
        val quotesList = QuotesList(responseAs[String])
        quotesList.ticker shouldEqual "INTC"
        quotesList.quoteList.size shouldEqual 1
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade buy success" in {
      Put("/trade/INTC/buy/100/10.48") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "buy"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.48
        tradeInfo.status shouldEqual "SUCCESS"
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade sell success" in {
      Put("/trade/INTC/sell/100/10.44") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "sell"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.44
        tradeInfo.status shouldEqual "SUCCESS"
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade buy failed(price less than current price)" in {
      Put("/trade/INTC/buy/100/10.44") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "buy"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.44
        tradeInfo.status shouldEqual "FAILED"
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade buy failed(price within 1 tick of current price)" in {
      Put("/trade/INTC/buy/100/10.47") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "buy"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.47
        tradeInfo.status shouldEqual "FAILED"
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade sell failed(price greater than current price)" in {
      Put("/trade/INTC/sell/100/10.49") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "sell"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.49
        tradeInfo.status shouldEqual "FAILED"
        status shouldEqual StatusCodes.OK
      }
    }
    "return trade sell failed(price within 1 tick of current price)" in {
      Put("/trade/INTC/sell/100/10.45") ~> route ~> check {
        val tradeInfo = TradeInfo(responseAs[String])
        tradeInfo.ticker shouldEqual "INTC"
        tradeInfo.action shouldEqual "sell"
        tradeInfo.qty shouldEqual 100
        tradeInfo.price shouldEqual 10.45
        tradeInfo.status shouldEqual "FAILED"
        status shouldEqual StatusCodes.OK
      }
    }
  }

  def deleteLevelDbFolder(filePathString: String) = {
    try{
      FileUtils.deleteDirectory(new File(filePathString))
    }catch{
      case e: Exception => println(e.getMessage)
    }
  }
}
