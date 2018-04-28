package org.aitester.exchange

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory

import scala.io.StdIn


//dummy class to simulate the exchange
object Exchange extends App{
  val log = LoggerFactory.getLogger(Exchange.getClass.getName)
  implicit val system = ActorSystem("exchange")
  implicit val executionContext = system.dispatcher
  implicit val materialize = ActorMaterializer()
  val levelDBStore = new LevelDBStore("exchange-db")

  val route = path("ping") {
    get{
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)` ,"ALIVE\n"))
    }
  } ~ path ("security" / Segment / DoubleNumber ) { (ticker, price)  =>
    put {
      val securityInfo = SecurityInfo(ticker, price)
      val key = SecurityInfo.key(ticker)
      log.info(s"SecurityInfo - PUT : ${securityInfo.asJson}")
      levelDBStore.putInDB(key, securityInfo.asJson)
      complete(HttpEntity(ContentTypes.`application/json`, securityInfo.asJson))
    }
  } ~ path ("security" / Segment) { ticker =>
    get {
      log.info(s"SecurityInfo GET : ${ticker}")
      val key = SecurityInfo.key(ticker)
      val secInfoOption = levelDBStore.getFromDB(key)
      secInfoOption match {
        case Some(secInfo) => complete(HttpEntity(ContentTypes.`application/json`,secInfo))
        case None => complete(HttpResponse(StatusCodes.BadRequest))
      }
    }
  } ~ path ( "quote" / Segment / Segment / IntNumber / DoubleNumber) { (ticker, action, qty, price) =>
    {
      put {
        val quoteInfo = QuoteInfo(ticker, action, System.currentTimeMillis(), qty, price)
        log.info(s"QuoteInfo PUT : ${quoteInfo.asJson}")
        val key = QuoteInfo.key(ticker, action)
        levelDBStore.putInDB(key, quoteInfo.asJson)
        complete(HttpEntity(ContentTypes.`application/json`, quoteInfo.asJson))
      }
    }
  } ~ path ( "trade" / Segment / Segment / IntNumber / DoubleNumber) { (securityName, action, qty, price) =>
    {
      put{
        log.info(s"${securityName} : ${action} : ${qty} : ${price}")
        //if incoming price for buy is 1 tick better than current price - perform trade
        //if incoming price for sell is 1 tick lesse than current price - perform trade
        complete("SUCCESS")
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  log.info("server running on port 8080. Press return to stop.")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
