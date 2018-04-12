package com.experiment.http

import cats.Monad
import com.experiment.model.{BusinessError, MovieNotFoundError}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class ErrorHandler[F[_] : Monad] extends Http4sDsl[F] {

  val handle: BusinessError => F[Response[F]] = {
    case MovieNotFoundError(msg) => NotFound(ErrorResponse(msg).asJson)
  }

}
