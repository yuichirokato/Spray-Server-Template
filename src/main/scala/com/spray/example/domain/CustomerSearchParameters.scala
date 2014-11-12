package com.spray.example.domain

import java.util.Date

case class CustomerSearchParameters(firstName: Option[String] = None,
                                    lastName: Option[String] = None,
                                    birthday: Option[Date] = None)
