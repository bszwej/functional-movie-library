package com.experiment.http

import cats.effect.IO
import cats.syntax.option._
import com.experiment.BaseTest
import com.experiment.model.Movie
import com.experiment.repository.interpreter.InMemMovieRepository
import com.experiment.service.MovieService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.impl.Statuses
import org.http4s.{Method, Request, Response, Uri}
import org.scalatest.Matchers

class MovieEndpointsTest extends BaseTest with Matchers with Statuses {

  implicit val movieDecoder = jsonOf[IO, Movie]
  implicit val movieListDecoder = jsonOf[IO, List[Movie]]

  behavior of "POST /movies"

  it should "create movie" in new TestScope {
    // given
    val movie = Movie(title = "title", year = 2004, id = None)
    val createRequest =
      Request[IO](Method.POST, Uri(path = "/movies"))
        .withBody(movie.asJson)
        .unsafeRunSync()

    // when
    val createResult: Response[IO] = execute(createRequest)

    // then
    createResult.status shouldBe Ok

    val createdMovie = createResult.as[Movie].unsafeRunSync()
    val createdMovieId = createdMovie.id.value
    createdMovie shouldBe Movie(title = "title", year = 2004, id = createdMovieId.some)
    movieRepository.get(createdMovieId).unsafeRunSync().isDefined shouldBe true
  }

  behavior of "GET /movies/{id}"

  it should "get movie by id" in new TestScope {
    // given
    val request = Request[IO](Method.GET, Uri(path = "/movies/1"))
    movieRepository.insert(Movie("title", 2001, 1.some))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe Ok
    result.as[Movie].unsafeRunSync() shouldBe Movie("title", 2001, 1.some)
  }

  it should "return 404 if not found" in new TestScope {
    // given
    val request = Request[IO](Method.GET, Uri(path = "/movies/2"))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe NotFound
  }

  behavior of "GET /movies"

  it should "return a list of movies" in new TestScope {
    // given
    val request = Request[IO](Method.GET, Uri(path = "/movies"))
    movieRepository.insert(Movie("title", 2001, 1.some))
    movieRepository.insert(Movie("title2", 2002, 2.some))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe Ok
    result.as[List[Movie]].unsafeRunSync().size shouldBe 2
    result.as[List[Movie]].unsafeRunSync() should contain allOf(
      Movie("title", 2001, 1.some),
      Movie("title2", 2002, 2.some)
    )
  }

  it should "return empty list if no movies found" in new TestScope {
    // given
    val request = Request[IO](Method.GET, Uri(path = "/movies"))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe Ok
    result.as[List[Movie]].unsafeRunSync().size shouldBe 0
  }

  behavior of "DELETE /movies/{id}"

  it should "delete movie by id" in new TestScope {
    // given
    val request = Request[IO](Method.DELETE, Uri(path = "/movies/1"))
    movieRepository.insert(Movie("title", 2001, 1.some))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe NoContent
    movieRepository.get(1).unsafeRunSync() shouldBe None
  }

  it should "return 404 if movie not found" in new TestScope {
    // given
    val request = Request[IO](Method.DELETE, Uri(path = "/movies/2"))

    // when
    val result: Response[IO] = execute(request)

    // then
    result.status shouldBe NotFound
  }


  trait TestScope {
    val movieRepository = new InMemMovieRepository[IO]()
    val movieService = new MovieService[IO](movieRepository)
    val errorHandler = new ErrorHandler[IO]
    val httpService = new MovieEndpoints[IO](movieService, errorHandler).movieEndpoints

    def execute(request: Request[IO]): Response[IO] =
      httpService
        .run(request)
        .getOrElse(fail("Request wasn't handled"))
        .unsafeRunSync()
  }

}
