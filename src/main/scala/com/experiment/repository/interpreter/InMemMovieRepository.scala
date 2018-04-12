package com.experiment.repository.interpreter

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.experiment.model.Movie
import com.experiment.repository.MovieRepositoryAlgebra

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds
import scala.util.Random

class InMemMovieRepository[F[_] : Applicative] extends MovieRepositoryAlgebra[F] {

  private val cache = TrieMap[Int, Movie]()

  override def insert(movie: Movie): F[Movie] = {
    val id = movie.id.fold(ifEmpty = Math.abs(Random.nextInt()))(identity)
    val movieWithId = movie.copy(id = Some(id))
    cache.putIfAbsent(id, movieWithId)
    movieWithId.pure[F]
  }

  override def get(id: Int): F[Option[Movie]] =
    cache.get(id).pure[F]

  override def findAll: F[List[Movie]] =
    cache.values.toList.pure[F]

  override def delete(id: Int): F[Option[Movie]] =
    cache.remove(id).pure[F]

}
