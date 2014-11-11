package com.spray.example.boot

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.spray.example.config.Configuration
import com.spray.example.rest.RestServiceActor
import spray.can.Http

/**
 * Created by you on 2014/11/09.
 */
object Boot extends App with Configuration {
  implicit val system = ActorSystem("rest-service-example")

  val restService = system.actorOf(Props[RestServiceActor], "rest-endpoint")

  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
}
