package com.experiment.config

import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.error.ConfigReaderException

import scala.language.higherKinds

case class AppConfig(port: PortNumber, host: NonEmptyString, dynamo: Dynamo)
case class Dynamo(tableName: NonEmptyString, endpoint: NonEmptyString)

object AppConfig {

  private val mainNamespace = "functional-movie-library"

  def load[F[_]](implicit F: Sync[F]): F[AppConfig] =
    F.delay(pureconfig.loadConfig[AppConfig](mainNamespace))
      .flatMap {
        case Left(err) ⇒ F.raiseError(ConfigReaderException[AppConfig](err))
        case Right(appConfig) ⇒ F.pure(appConfig)
      }
}
