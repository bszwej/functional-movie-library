package com.experiment.scanamo

import cats.data.NonEmptyList
import cats.effect.Async
import cats.~>
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.ops._
import com.gu.scanamo.request._

import scala.language.higherKinds

object ScanamoAsync {
  def exec[F[_] : Async, A](client: AmazonDynamoDBAsync)(op: ScanamoOps[A]) =
    op.foldMap(ScanamoAsyncInterpreter[F](client))
}

private[scanamo] object ScanamoAsyncInterpreter {

  def apply[F[_]](client: AmazonDynamoDBAsync)(implicit F: Async[F]) = new (ScanamoOpsA ~> F) {
    private def asyncOf[X <: AmazonWebServiceRequest, T](call: (X, AsyncHandler[X, T]) => java.util.concurrent.Future[T], req: X): F[T] = {
      F.async { cb =>
        val h = new AsyncHandler[X, T] {
          override def onError(exception: Exception): Unit = cb(Left(exception))

          override def onSuccess(request: X, result: T): Unit = cb(Right(result))
        }

        call(req, h)
      }
    }

    override def apply[A](op: ScanamoOpsA[A]): F[A] = op match {
      case Put(req) =>
        asyncOf(client.putItemAsync, JavaRequests.put(req))
      case Get(req) =>
        asyncOf(client.getItemAsync, req)
      case Scan(req) =>
        asyncOf(client.scanAsync, JavaRequests.scan(req))
      case Delete(req) =>
        asyncOf(client.deleteItemAsync, JavaRequests.delete(req))
      case _ ⇒
        F.raiseError(new RuntimeException("Operation cannot be interpreted."))
    }
  }
}

private[scanamo] object JavaRequests {

  import collection.JavaConverters._

  def scan(req: ScanamoScanRequest): ScanRequest = {
    def queryRefinement[T](o: ScanamoScanRequest => Option[T])(rt: (ScanRequest, T) => ScanRequest): ScanRequest => ScanRequest =
      qr => o(req).foldLeft(qr)(rt)

    NonEmptyList.of(
      queryRefinement(_.index)(_.withIndexName(_)),
      queryRefinement(_.options.limit)(_.withLimit(_)),
      queryRefinement(_.options.exclusiveStartKey)((r, k) => r.withExclusiveStartKey(k.asJava)),
      queryRefinement(_.options.filter)((r, f) => {
        val requestCondition = f.apply(None)
        val filteredRequest = r.withFilterExpression(requestCondition.expression)
          .withExpressionAttributeNames(requestCondition.attributeNames.asJava)
        requestCondition.attributeValues.fold(filteredRequest)(avs =>
          filteredRequest.withExpressionAttributeValues(avs.asJava)
        )
      })
    ).reduceLeft(_.compose(_))(
      new ScanRequest().withTableName(req.tableName).withConsistentRead(req.options.consistent)
    )
  }

  def put(req: ScanamoPutRequest): PutItemRequest =
    req.condition.foldLeft(
      new PutItemRequest().withTableName(req.tableName).withItem(req.item.getM)
    )((r, c) =>
      c.attributeValues.foldLeft(
        r.withConditionExpression(c.expression).withExpressionAttributeNames(c.attributeNames.asJava)
      )((cond, values) => cond.withExpressionAttributeValues(values.asJava))
    )

  def delete(req: ScanamoDeleteRequest): DeleteItemRequest =
    req.condition.foldLeft(
      new DeleteItemRequest().withTableName(req.tableName).withKey(req.key.asJava)
    )((r, c) =>
      c.attributeValues.foldLeft(
        r.withConditionExpression(c.expression).withExpressionAttributeNames(c.attributeNames.asJava)
      )((cond, values) => cond.withExpressionAttributeValues(values.asJava))
    )

}
