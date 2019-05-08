package pipelines.data

import io.circe.{Decoder, Json, ObjectEncoder}
import monix.reactive.Observable
import org.scalatest.{Matchers, WordSpec}
import pipelines.data.ComputerTest.{SomeType, computer}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

class ComputerTest extends WordSpec with Matchers {

  class ObsMap[A, B](f: A => B) {
    def doit(obs: Observable[A]): Observable[B] = {
      obs.map(f)
    }
  }
  class ObsFilter[A](f: A => Boolean) {
    def doit(obs: Observable[A]): Observable[A] = {
      obs.filter(f)
    }
  }
  class ObsLimit(d: FiniteDuration) {
    def doit[A](obs: Observable[A]): Observable[A] = {
      obs.delayOnNext(d)
    }
  }

  "Computer.paths" should {
    "calculate how to get a Double from an array of bytes with out computer" in {
      val input = Shape.of[Array[Byte]]

      val Seq(solution) = ComputerTest.computer.findPaths(input, _.toString.toLowerCase.contains("double"), 10)

      val explanation = solution.map(_.value).mkString("'", "' and then '", s"'")
      println(explanation)

      val byteArrayToTryDouble: Compute = solution.map(_.compute).reduce(_ andThen _)

      import io.circe.syntax._
      val jsonBytes: Array[Byte] = SomeType(123.456, "test").asJson.noSpaces.getBytes("UTF-8")
      val result                 = byteArrayToTryDouble.applyUnsafe(jsonBytes)
      result shouldBe Success(Success(123.456))

    }
  }
}

object ComputerTest {

  case class SomeType(value: Double, name: String)
  object SomeType {
    implicit val encoder: ObjectEncoder[SomeType] = io.circe.generic.semiauto.deriveEncoder[SomeType]
    implicit val decoder: Decoder[SomeType]       = io.circe.generic.semiauto.deriveDecoder[SomeType]
  }

  def parse(json: String)                            = io.circe.parser.parse(json).toTry
  def as[T: Decoder](json: Json)                     = json.as[T].toTry
  def doFlattenTry[A](two: Try[Try[A]]): Try[A]      = two.flatten
  def doMapTry[A, B](tri: Try[A], f: A => B): Try[B] = tri.map(f)

  val utf8ToString     = Compute[Array[Byte], String](bytes => new String(bytes, "UTF-8")).withName("bytes to UTF8")
  val jsonForString    = Compute[String, Try[Json]](parse).withName("json parser")
  val jsonToSomeType   = Compute[Json, Try[SomeType]](as[SomeType](_)).withName("sometype json decoder")
  val valueForSomeType = Compute[SomeType, Double](_.value).withName("get value")
  val flattenTry = Compute
    .partial {
      case ParameterizedShape("Try", Seq(inner @ ParameterizedShape("Try", s))) =>
        inner
    }
    .of(doFlattenTry _)
    .withName("flatten try")

//  val mapTry = Compute.partial {
//      case ParameterizedShape("Try", Seq(inner)) => inner
//    }.of(doMapTry).withName("map try")

  val computer = Computer(
    utf8ToString,
    jsonForString,
    jsonToSomeType,
    flattenTry,
    valueForSomeType
  )

}
