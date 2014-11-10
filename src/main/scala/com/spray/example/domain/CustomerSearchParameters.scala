package com.spray.example.domain

import java.util.Date

/**
 * Created by you on 2014/11/09.
 */
case class CustomerSearchParameters(firstName: Option[String] = None,
                                    lastName: Option[String] = None,
                                    birthday: Option[Date] = None)
