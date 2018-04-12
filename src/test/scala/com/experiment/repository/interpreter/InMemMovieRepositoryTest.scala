package com.experiment.repository.interpreter

import cats.Id
import cats.syntax.option._
import com.experiment.BaseTest
import com.experiment.model.Movie

class InMemMovieRepositoryTest extends BaseTest {

  behavior of "inserting and getting by id"

  it should "insert and get by id" in new TestScope {
    // when
    val insertResult: Movie = repository.insert(Movie("title", 2001, 1.some))
    val getResult: Option[Movie] = repository.get(1)

    // then
    getResult shouldBe Movie("title", 2001, 1.some).some
  }

  it should "generate ID when inserting if not given" in new TestScope {
    // when
    val insertResult: Movie = repository.insert(Movie("title", 2001, None))

    // then
    val movieId = insertResult.id.value
    val getResult: Option[Movie] = repository.get(movieId)
    getResult shouldBe Movie("title", 2001, movieId.some).some
  }

  it should "return None if movie is not found" in new TestScope {
    // when
    val getResult: Option[Movie] = repository.get(-1)

    // then
    getResult shouldBe None
  }

  behavior of "finding"

  it should "find all" in new TestScope {
    // given
    val insertResult1 = repository.insert(Movie("title1", 2001, None))
    val insertResult2 = repository.insert(Movie("title2", 2002, None))

    // when
    val findResult: List[Movie] = repository.findAll

    // then
    findResult should contain allOf(insertResult1, insertResult2)
  }

  it should "return empty list if no movies found" in new TestScope {
    // when
    val findResult: List[Movie] = repository.findAll

    // then
    findResult shouldBe Nil
  }

  behavior of "deleting"

  it should "delete" in new TestScope {
    // given
    val insertResult = repository.insert(Movie("movie title", 2002, 1.some))

    // when
    val deleteResult: Option[Movie] = repository.delete(1)

    // then
    deleteResult shouldBe Movie("movie title", 2002, 1.some).some
    repository.get(1) shouldBe None
  }

  it should "return None if movie not found" in new TestScope {
    // when
    val deleteResult: Option[Movie] = repository.delete(-1)

    // then
    deleteResult shouldBe None
  }

  trait TestScope {
    val repository = new InMemMovieRepository[Id]
  }

}
