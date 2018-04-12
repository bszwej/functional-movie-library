package com.experiment.service

import cats.Functor
import com.experiment.model.Result.Result
import com.experiment.model.{Movie, MovieNotFoundError, Result}
import com.experiment.repository.MovieRepositoryAlgebra

import scala.language.higherKinds

class MovieService[F[_] : Functor](movieRepositoryAlg: MovieRepositoryAlgebra[F]) {

  def create(movie: Movie): F[Movie] =
    movieRepositoryAlg.insert(movie)

  def getById(id: Int): Result[F, Movie] =
    Result.fromOption(
      movieRepositoryAlg.get(id),
      MovieNotFoundError(s"Movie with id '$id' has not been found.")
    )

  def findAll: F[List[Movie]] =
    movieRepositoryAlg.findAll

  def delete(id: Int): Result[F, Movie] =
    Result.fromOption(
      movieRepositoryAlg.delete(id),
      MovieNotFoundError(s"Movie with id '$id' has not been found.")
    )

}
