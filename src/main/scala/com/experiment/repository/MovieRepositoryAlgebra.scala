package com.experiment.repository

import com.experiment.model.Movie

import scala.language.higherKinds

trait MovieRepositoryAlgebra[F[_]] {

  def insert(movie: Movie): F[Movie]

  def get(id: Int): F[Option[Movie]]

  def findAll: F[List[Movie]]

  def delete(id: Int): F[Option[Movie]]

}
