package com.experiment

import cats.effect.IO
import com.experiment.config.AppConfig
import com.experiment.dynamo.DynamoClientProvider
import com.experiment.http.{ErrorHandler, MovieEndpoints}
import com.experiment.repository.interpreter.DynamoMovieRepository
import com.experiment.service.MovieService
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
    for {
      config ← Stream.eval(AppConfig.load[IO])
      dynamoClientProvider = new DynamoClientProvider(config.dynamo.endpoint.value)
      movieRepository = new DynamoMovieRepository[IO](config.dynamo.tableName.value, dynamoClientProvider.client)
      movieService = new MovieService[IO](movieRepository)
      errorHandler = new ErrorHandler[IO]
      endpoints = new MovieEndpoints[IO](movieService, errorHandler).movieEndpoints
      exitCode ← BlazeBuilder[IO]
        .mountService(endpoints)
        .bindHttp(config.port.value, config.host.value)
        .serve
    } yield exitCode

}
