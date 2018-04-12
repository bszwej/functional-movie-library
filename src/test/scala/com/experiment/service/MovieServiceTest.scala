package com.experiment.service

import cats.Id
import cats.syntax.option._
import com.experiment.BaseTest
import com.experiment.model.Result.Result
import com.experiment.model.ResultSyntax._
import com.experiment.model.{Movie, MovieNotFoundError}
import com.experiment.repository.interpreter.InMemMovieRepository

class MovieServiceTest extends BaseTest {

  behavior of "creating"

  it should "create a movie" in new TestScope {
    // when
    val createResult: Movie = service.create(Movie("title", 2001, 1.some))

    // then
    val getResult = repository.get(1)
    getResult.value shouldBe Movie("title", 2001, 1.some)
  }

  it should "generate ID when creating for a movie if not given" in new TestScope {
    // when
    val createResult: Movie = service.create(Movie("title", 2001, None))

    // then
    val movieId = createResult.id.value
    val getResult = repository.get(movieId)
    getResult.value shouldBe Movie("title", 2001, movieId.some)
  }

  behavior of "getting by id"

  it should "get by id" in new TestScope {
    // given
    val createResult: Movie = service.create(Movie("title", 2001, 1.some))

    // when
    val getResult: Result[Id, Movie] = service.getById(1)

    // then
    getResult shouldBe Movie("title", 2001, 1.some).success
  }

  it should "return error if movie is not found" in new TestScope {
    // when
    val result: Result[Id, Movie] = service.getById(-1)

    // then
    val error = result.value.left.value
    error shouldBe a[MovieNotFoundError]
    error.msg should not be empty
  }

  behavior of "finding"

  it should "find all" in new TestScope {
    // given
    val createResult1 = service.create(Movie("title1", 2001, 1.some))
    val createResult2 = service.create(Movie("title2", 2002, 2.some))

    // when
    val findResult: List[Movie] = service.findAll

    // then
    findResult should contain allOf(createResult1, createResult2)
  }

  it should "return empty list if no movies found" in new TestScope {
    // when
    val findResult: List[Movie] = service.findAll

    // then
    findResult shouldBe Nil
  }

  behavior of "deleting"

  it should "delete" in new TestScope {
    // given
    val createResult = service.create(Movie("movie title", 2002, 1.some))

    // when
    val deleteResult: Result[Id, Movie] = service.delete(1)

    // then
    deleteResult shouldBe Movie("movie title", 2002, 1.some).success
    service.getById(1).value.isLeft shouldBe true
  }

  trait TestScope {
    val repository = new InMemMovieRepository[Id]
    val service = new MovieService[Id](repository)
  }

}
