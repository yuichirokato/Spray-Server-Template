package com.spray.example.config

import com.typesafe.config.ConfigFactory
// TODO: scala.util.Tryが何か調べる
import scala.util.Try

/**
 * Created by you on 2014/11/09.
 */
trait Configuration {
  // Application config object
  val config = ConfigFactory.load()

  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  // Posrt to start service on
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)

  lazy val dbHost = Try(config.getString("db.host")).getOrElse("localhost")

  lazy val dbPort = Try(config.getInt("db.port")).getOrElse(3306)

  lazy val dbName = Try(config.getString("db.name")).getOrElse("rest")

  // User name used to access database
  lazy val dbUser = Try(config.getString("db.user")).toOption.orNull

  lazy val dbPassword = Try(config.getString("db.password")).toOption.orNull
}