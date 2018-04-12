package com.experiment.repository.interpreter

import cats.effect.Async
import cats.implicits._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.experiment.scanamo.ScanamoSyntax._
import com.experiment.model.Movie
import com.experiment.repository.MovieRepositoryAlgebra
import com.experiment.scanamo.ScanamoAsync
import com.gu.scanamo.Table
import com.gu.scanamo.syntax._

import scala.language.higherKinds
import scala.util.Random

class DynamoMovieRepository[F[_] : Async](tableName: String, client: AmazonDynamoDBAsync) extends MovieRepositoryAlgebra[F] {

  private val table = Table[Movie](tableName)
  private val primaryKey: Symbol = 'id

  override def insert(movie: Movie): F[Movie] = {
    val id = movie.id.fold(ifEmpty = Math.abs(Random.nextInt()))(identity)
    val movieWithId = movie.copy(id = Some(id))
    ScanamoAsync.exec(client)(table.put(movieWithId)).map(_ ⇒ movieWithId)
  }

  override def get(id: Int): F[Option[Movie]] =
    ScanamoAsync.exec(client)(table.get(primaryKey → id)).subsumeFailure

  override def findAll: F[List[Movie]] =
    ScanamoAsync.exec(client)(table.scan()).subsumeFailure

  override def delete(id: Int): F[Option[Movie]] = {
    val operations = for {
      movie <- table.get(primaryKey → id)
      _ <- table.delete(primaryKey → id)
    } yield movie

    ScanamoAsync.exec(client)(operations).subsumeFailure
  }

}
