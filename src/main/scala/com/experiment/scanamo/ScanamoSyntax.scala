package com.experiment.scanamo

import cats.effect.Async
import cats.implicits._
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.error.DynamoReadError.describe

import scala.language.higherKinds

object ScanamoSyntax {

  implicit class ScanamoGetResultOps[F[_], A](result: F[Option[Either[DynamoReadError, A]]])(implicit F: Async[F]) {
    def subsumeFailure: F[Option[A]] =
      result.flatMap {
        case None => F.pure(None)
        case Some(Left(dynamoReadError)) => F.raiseError(new Exception(describe(dynamoReadError)))
        case Some(Right(value)) => F.pure(Some(value))
      }
  }

  type EitherDynamoReadError[A] = Either[DynamoReadError, A]

  implicit class ScanamoScanResultOps[F[_], A](result: F[List[Either[DynamoReadError, A]]])(implicit F: Async[F]) {
    def subsumeFailure: F[List[A]] =
      result.flatMap {
        case Nil => F.pure(Nil)
        case xs ⇒ xs.sequence[EitherDynamoReadError, A].fold(
          error ⇒ F.raiseError(new Exception(describe(error))),
          result ⇒ F.pure(result)
        )
      }
  }
}
