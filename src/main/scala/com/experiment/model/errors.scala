package com.experiment.model

sealed abstract class BusinessError(val msg: String)

case class MovieNotFoundError(override val msg: String) extends BusinessError(msg)
