package org.aitester.exchange

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

//dummy class to simulate the exchange

object Exchange extends App{
  implicit val system = ActorSystem("exchange")
  implicit val executionContext = system.dispatcher
  implicit val materialize = ActorMaterializer()
  val levelDBStore = new LevelDBStore("exchange-db")

  val route = path("ping") {
    get{
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>I'm alive</h1>"))
    }
  } ~ path ("security") {
    path(Segment) { securityName =>
      path(Segment) { price =>
        put {
          levelDBStore.putInDB(securityName, price)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "success"))
        }
      }~
      pathEndOrSingleSlash {
        get {
          levelDBStore.getFromDB(securityName) match {
            case Some(price) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, price))
            case None => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, securityName + ": price unknown"))
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println("server running on port 8080 \n press return to stop.")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
