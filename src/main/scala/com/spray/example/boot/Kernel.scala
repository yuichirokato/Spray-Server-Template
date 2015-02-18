package com.spray.example.boot

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.kernel.Bootable
import com.spray.example.config.Configuration
import com.spray.example.rest.RestServiceActor
import spray.can.Http

class Kernel extends Bootable with Configuration {

  implicit val system = ActorSystem("spray-server-template")

  def startup() = {
    val restService = system.actorOf(Props[RestServiceActor], "rest-endpoint")

    IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
  }

  def shutdown() = {
    system.shutdown()
  }

}
