package com.spray.example.boot

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.spray.example.config.Configuration
import com.spray.example.rest.RestServiceActor
import spray.can.Http

object Boot extends App with Configuration {
  implicit val system = ActorSystem("spray-server-template")

  val restService = system.actorOf(Props[RestServiceActor], "rest-endpoint")

  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
}
