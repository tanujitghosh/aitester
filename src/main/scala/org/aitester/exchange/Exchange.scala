package org.aitester.exchange

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

case class SecurityInfo(ticker: String, price : Double){
  override def toString: String = s"securityInfo:${ticker}:${price}"

  def key : String = s"securityInfo:${ticker}"
}

case class QuoteInfo(ticker:String, action: String, timestamp: Long, qty: Int, price: Double){
  override def toString: String = s"quoteInfo:${ticker}:${action}:${timestamp}:${qty}:${price}"

  def key : String = s"quoteInfo:${ticker}:${action}:${timestamp}"
}

//dummy class to simulate the exchange
object Exchange extends App{
  implicit val system = ActorSystem("exchange")
  implicit val executionContext = system.dispatcher
  implicit val materialize = ActorMaterializer()
  val levelDBStore = new LevelDBStore("exchange-db")

  def covertFromDbValue(dbString : String) : Any = {
    val strArray = dbString.split(":")
    strArray(0) match {
      case "securityInfo" => SecurityInfo(strArray(1), strArray(2).toDouble)
    }
  }

  val route = path("ping") {
    get{
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)` ,"ALIVE\n"))
    }
  } ~ path ("security" / Segment / DoubleNumber ) { (securityName, price)  =>
    put {
      println(securityName + ":" + price)
      levelDBStore.putInDB(securityName, price.toString)
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "SUCCESS\n"))
    }
  } ~ path ("security" / Segment) { securityName =>
    get {
      println(securityName)
      val priceOption = levelDBStore.getFromDB(securityName)
      priceOption match {
        case Some(price) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, securityName + ":" + price + "\n"))
        case None => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, securityName + ": unknown\n"))
      }
    }
  } ~ path ( "quote" / Segment / Segment / IntNumber / DoubleNumber) { (securityName, action, qty, price) =>
    {
      println(s"${securityName} : ${action} : ${qty} : ${price}")
      put{
        complete("SUCCESS")
      }
    }
  } ~ path ( "trade" / Segment / Segment / IntNumber / DoubleNumber) { (securityName, action, qty, price) =>
    {
      println(s"${securityName} : ${action} : ${qty} : ${price}")
      put{
        //if incoming price for buy is 1 tick better than current price - perform trade
        //if incoming price for sell is 1 tick lesse than current price - perform trade
        complete("SUCCESS")
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println("server running on port 8080 \n press return to stop.")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
