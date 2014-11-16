package com.spray.example.rest

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Actor
import com.spray.example.dao.CustomerDAO
import com.spray.example.domain.{Customer, CustomerSearchParameters, Failure}
import net.liftweb.json.JsonParser.ParseException
import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._
import spray.util.LoggingContext

class RestServiceActor extends Actor with RestService {
  implicit def actorRefFactory = context

  override def receive = runRoute(rest)
}

trait RestService extends HttpService {
  val customerService = new CustomerDAO
  val logger = LoggingContext.fromActorRefFactory

  // implicit val executionContext = actorRefFactory.dispatcher

  implicit val liftJsonFormats = new Formats {
    val dateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      override def parse(s: String): Option[Date] = try {
        Some(sdf.parse(s))
      } catch {
        case e: Exception => None
      }

      override def format(d: Date): String = sdf.format(d)
    }
  }

  implicit val string2Date = new FromStringDeserializer[Date] {
    def apply(value: String) = {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      try Right(sdf.parse(value))
      catch {
        case e: ParseException => Left(MalformedContent(s"'$value' is not a valid Date value", e))
      }
    }
  }

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse { response: HttpResponse =>
      response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`), write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  implicit val customUnmarshaller = Unmarshaller[Customer](MediaTypes.`application/json`) {
    case httpEntity: HttpEntity => read[Customer](httpEntity.asString(HttpCharsets.`UTF-8`))
  }

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    path("customer") {
      post {
        entity(as[Customer]) { customer =>
          ctx: RequestContext =>
            handleRequest(ctx, StatusCodes.Created) {
              logger.debug(s"Creating customer: $customer")
              customerService.create(customer)
            }
        }
      } ~
        get {
          parameters('firstName.as[String] ?, 'lastName.as[String] ?, 'birthday.as[String] ?).as(CustomerSearchParameters) {
            searchParameters: CustomerSearchParameters => {
              ctx: RequestContext =>
                handleRequest(ctx) {
                  logger.debug(s"Searching for customers with parameters: $searchParameters")
                  customerService.search(searchParameters)
                }
            }
          }
        }
    } ~
      path("customer" / LongNumber) { customerId =>
        put {
          entity(as[Customer]) { customer =>
            ctx: RequestContext =>
              handleRequest(ctx) {
                logger.debug(s"Updating customer with id $customerId : $customer")
                println(s"Updating customer with id $customerId : $customer")
                customerService.update(customerId, customer)
              }
          }
        } ~
          delete {
            ctx: RequestContext =>
              handleRequest(ctx) {
                logger.debug(s"Deleting customer with id $customerId")
                println(s"Deleting customer with id $customerId")
                customerService.delete(customerId)
              }
          } ~
          get {
            ctx: RequestContext =>
              handleRequest(ctx) {
                logger.debug(s"Retrieving customer with id $customerId")
                println(s"Retrieving customer with id $customerId")
                customerService.get(customerId)
              }
          }
      }
  }

  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[Failure, _]) = {
    action match {
      case Right(result: Object) => ctx.complete(successCode, write(result))
      case Left(error: Failure) => ctx.complete(error.getStatuCode, write(Map("error" -> error.message)))
      case _ => ctx.complete(StatusCodes.InternalServerError)
    }
  }
}
