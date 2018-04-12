package com.experiment.model

import cats.data.EitherT
import cats.{Applicative, Functor}
import com.experiment.model.Result.Result

import scala.language.higherKinds

object Result {

  type Result[F[_], T] = EitherT[F, BusinessError, T]

  def fromOption[F[_] : Functor, T](option: F[Option[T]], ifNone: BusinessError): Result[F, T] =
    EitherT.fromOptionF(option, ifNone)

  def success[F[_] : Functor, T](value: F[T]): Result[F, T] =
    EitherT.liftF[F, BusinessError, T](value)

}

object ResultSyntax {

  import cats.implicits._

  implicit class ResultSyntax[F[_] : Applicative, T](value: T) {
    def success: Result[F, T] = Result.success[F, T](value.pure[F])
  }

}
