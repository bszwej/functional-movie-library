package com.experiment.repository.interpreter

import cats.effect.IO
import cats.syntax.option._
import com.amazonaws.services.dynamodbv2.model.{CreateTableRequest, _}
import com.amazonaws.services.dynamodbv2.util.TableUtils
import com.experiment.model.Movie

class DynamoMovieRepositoryTest extends BaseRepositoryTest {

  private val tableName = "movie-repository-test"
  private val dynamoMovieRepository = new DynamoMovieRepository[IO](tableName, dynamoClient)

  behavior of "inserting and getting by id"

  it should "insert and get by id" in {
    // when
    val maybeMovie = (for {
      insertResult ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, 1.some))
      movie ← dynamoMovieRepository.get(insertResult.id.value)
    } yield movie).unsafeRunSync()

    // then
    val movie = maybeMovie.value
    movie shouldBe Movie("pulp fiction", 1994, 1.some)
  }

  it should "generate ID when inserting if not given" in {
    // when
    val maybeMovie = (for {
      insertResult ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, None))
      movie ← dynamoMovieRepository.get(insertResult.id.value)
    } yield movie).unsafeRunSync()

    // then
    val movie = maybeMovie.value
    movie shouldBe Movie("pulp fiction", 1994, Some(movie.id.value))
  }

  it should "return None if movie is not found" in {
    // when
    val maybeMovie = (for {
      movie ← dynamoMovieRepository.get(-1)
    } yield movie).unsafeRunSync()

    // then
    maybeMovie shouldBe None
  }

  behavior of "finding"

  it should "find all" in {
    // when
    val movies = (for {
      _ ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, 1.some))
      _ ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, 2.some))
      movies ← dynamoMovieRepository.findAll
    } yield movies).unsafeRunSync()

    // then
    movies should contain allOf(Movie("pulp fiction", 1994, 1.some), Movie("pulp fiction", 1994, 2.some))
  }

  it should "return empty list if no movies found" in {
    // when
    val movies = (for {
      movies ← dynamoMovieRepository.findAll
    } yield movies).unsafeRunSync()

    // then
    movies shouldBe Nil
  }

  behavior of "deleting"

  it should "delete" in {
    // when
    val (deletedMovie, movies) = (for {
      _ ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, 1.some))
      _ ← dynamoMovieRepository.insert(Movie("pulp fiction", 1994, 2.some))
      deletedMovie ← dynamoMovieRepository.delete(1)
      movies ← dynamoMovieRepository.findAll
    } yield (deletedMovie, movies)).unsafeRunSync()

    // then
    deletedMovie.value shouldBe Movie("pulp fiction", 1994, 1.some)
    movies.size shouldBe 1
  }

  it should "return None if movie not found" in {
    // when
    val deletedMovie = (for {
      deletedMovie ← dynamoMovieRepository.delete(-1)
    } yield deletedMovie).unsafeRunSync()

    // then
    deletedMovie.isDefined shouldBe false
  }

  override protected def beforeEach(): Unit = {
    val primaryKey = new KeySchemaElement()
      .withAttributeName("id")
      .withKeyType(KeyType.HASH)

    val attributesDefinition = new AttributeDefinition()
      .withAttributeName("id")
      .withAttributeType("N")

    val throughput = new ProvisionedThroughput()
      .withReadCapacityUnits(5L)
      .withWriteCapacityUnits(5L)

    val createTableRequest = new CreateTableRequest()
      .withTableName(tableName)
      .withKeySchema(primaryKey)
      .withAttributeDefinitions(attributesDefinition)
      .withProvisionedThroughput(throughput)

    TableUtils.createTableIfNotExists(dynamoClient, createTableRequest)
  }

  override protected def afterEach(): Unit = dynamoClient.deleteTable(tableName)

}
