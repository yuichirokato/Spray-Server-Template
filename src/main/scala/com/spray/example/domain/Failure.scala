package com.spray.example.domain

import spray.http.{StatusCodes, StatusCode}

/**
 * Created by you on 2014/11/09.
 */
case class Failure(message: String, errorType: FailureType.Value) {

  def getStatuCode: StatusCode = {
    FailureType.withName(this.errorType.toString) match {
      case FailureType.BadRequest => StatusCodes.BadRequest
      case FailureType.NotFound => StatusCodes.NotFound
      case FailureType.Duplicate => StatusCodes.Forbidden
      case FailureType.DatabaseFailure => StatusCodes.InternalServerError
      case _ => StatusCodes.InternalServerError
    }
  }
}

object FailureType extends Enumeration {
  type Failure = Value

  val BadRequest = Value("bad_request")
  val NotFound = Value("not_found")
  val Duplicate = Value("entity_exists")
  val DatabaseFailure = Value("database_error")
  val InternalError = Value("internal_error")
}
