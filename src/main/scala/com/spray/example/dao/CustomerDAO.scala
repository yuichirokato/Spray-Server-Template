package com.spray.example.dao

/**
 * Created by you on 2014/11/09.
 */

import java.sql.SQLException

import com.spray.example.config.Configuration
import com.spray.example.domain._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

class CustomerDAO extends Configuration {
  val db = Database.forURL(url = "jdbc:mysql://%s:%d/%s".format(dbHost, dbPort, dbName),
    user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  val customers = TableQuery[CustomerTag]

  db.withTransaction { implicit session =>
    if (MTable.getTables("customers").list.isEmpty) {
      customers.ddl.create
    }
  }

  def create(c: Customer): Either[Failure, Customer] = {
    try {
      val id = db.withSession { implicit session =>
        customers.insert(c)
      }
      Right(c.copy(id = Some(id)))
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def update(id: Long, c: Customer): Either[Failure, Customer] = {
    try {
      db.withSession { implicit session =>
        customers.filter(_.id === c.id).update(c.copy(id = Some(id))) match {
          case 0 => Left(notFoundError(id))
          case _ => Right(c.copy(id = Some(id)))
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def delete(id: Long): Either[Failure, Customer] = {
    try {
      db.withTransaction { implicit session =>
        val query = customers.filter(_.id === id)
        val c = query.asInstanceOf[List[Customer]]
        c.size match {
          case 0 => Left(notFoundError(id))
          case _ => {
            query.delete
            Right(c.head)
          }
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def get(id: Long): Either[Failure, Customer] = {
    try {
      db.withSession { implicit session =>
        CustomerTag.findById(id).firstOption match {
          case Some(customer: Customer) => Right(customer)
          case None => Left(notFoundError(id))
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def search(params: CustomerSearchParameters): Either[Failure, List[Customer]] = {
    implicit val typeMapper = CustomerTag.dateTypedMapper

    try {
      db.withSession { implicit session =>
        val query = for {
          customer <- customers if {
          Seq(
            params.firstName.map(customer.firstName == _),
            params.lastName.map(customer.lastName == _),
            params.birthday.map(customer.birthday == _)
          ).flatten match {
            case Nil => true
            case seq => seq.reduce(_ && _)
          }
        }
        } yield customer

        Right(query.run(session).toList)
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  protected def databaseError(e: SQLException) = {
    Failure("%d: %s".format(e.getErrorCode, e.getMessage), FailureType.DatabaseFailure)
  }

  protected def notFoundError(customerId: Long) = {
    Failure("Customer with id=%d does not exist".format(customerId), FailureType.NotFound)
  }
}
