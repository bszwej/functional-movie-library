package com.experiment.http

import cats.effect.Sync
import cats.implicits._
import com.experiment.model.Movie
import com.experiment.service.MovieService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class MovieEndpoints[F[_] : Sync](movieService: MovieService[F], errorHandler: ErrorHandler[F]) extends Http4sDsl[F] {

  private implicit val movieEntityDecoder = jsonOf[F, Movie]

  //@formatter:off
  def movieEndpoints: HttpService[F] =
    createMovieEndpoint  <+>
    getMovieByIdEndpoint <+>
    findMoviesEndpoint   <+>
    deleteMovieEndpoint
  //@formatter:on

  private def createMovieEndpoint = HttpService[F] {
    case req @ POST -> Root / "movies" ⇒
      for {
        movie ← req.as[Movie]
        createdMovie ← movieService.create(movie)
        resp ← Ok(createdMovie.asJson)
      } yield resp
  }

  private def getMovieByIdEndpoint = HttpService[F] {
    case GET -> Root / "movies" / IntVar(id) ⇒
      movieService.getById(id).fold(
        error ⇒ errorHandler.handle(error),
        movie ⇒ Ok(movie.asJson)
      ).flatten
  }

  private def findMoviesEndpoint = HttpService[F] {
    case GET -> Root / "movies" ⇒
      movieService.findAll.flatMap(movies ⇒ Ok(movies.asJson))
  }

  private def deleteMovieEndpoint = HttpService[F] {
    case DELETE -> Root / "movies" / IntVar(id) ⇒
      movieService.delete(id).fold(
        error ⇒ errorHandler.handle(error),
        movie ⇒ NoContent()
      ).flatten
  }

}
