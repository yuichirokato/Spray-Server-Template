package com.spray.example.domain

/**
 * Created by you on 2014/11/09.
 */
import scala.slick.driver.MySQLDriver.simple._

// dbのモデルクラス
case class Customer(id: Option[Long],
                    firstName: String,
                    lastName: String,
                    birthday: Option[java.util.Date])

object CustomerTag extends TableQuery(new CustomerTag(_)) {
  val findById = this.findBy(_.id)

  implicit val dateTypedMapper = MappedColumnType.base[java.util.Date, java.sql.Date](
  { ud => new java.sql.Date(ud.getTime) },
  { sd => new java.sql.Date(sd.getTime) }
  )
}

class CustomerTag(tag: Tag) extends Table[Customer](tag, "customers"){
  implicit val dateTypeMapper = CustomerTag.dateTypedMapper

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def birthday = column[java.util.Date]("birthday")

  def * = (id.?, firstName, lastName, birthday.?) <> (Customer.tupled, Customer.unapply)
}


