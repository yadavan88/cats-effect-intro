package com.yadavan88.ce.part2

import cats.effect.IOApp
import cats.effect.IO
import scala.concurrent.duration._

object IOMethods extends IOApp.Simple {

  val io1 = IO("Scala")
  val io2 = IO("Cats")

  val mapPgm: IO[String] = io1.map(_.toUpperCase)
  val flatMapPgm: IO[String] = io1.flatMap(a => io2.map(a + _))
  val forComp: IO[String] = for {
    a <- io1
    b <- io2
  } yield a + b

  val strIO: IO[String] = IO("Cats Effect")
  val voidIO: IO[Unit] = strIO.void
  val asIntIO: IO[Int] = strIO as 100

  val printIO = IO.println("Hello World")
  val deferredIO: IO[String] = IO.defer(IO("String"))

  val aIO: IO[String] = IO("Hello")
  val anotherIO: IO[String] = IO(aIO).flatten

  val aFailedIO: IO[String] = IO.raiseError[String](new Exception("Failed IO"))

  val aFailedIntIO = IO.raiseError[Int](new Exception("Not a valid number"))
  val handledIO: IO[Int] = aFailedIntIO.handleError(ex => 0)
  val handledWithIO: IO[Int] = aFailedIntIO.handleErrorWith(_ => IO.pure(0))

  val intIO: IO[Int] = IO(100)
  val redeemedIO: IO[String] = intIO.redeem(_ => "failed", _ => "success")

  val num = scala.util.Random.nextInt(4)
  val raisedIO: IO[Unit] =
    IO.raiseWhen(num == 0)(new RuntimeException("Number can not be 0"))

  val firstIO: IO[Int] = IO(100)
  val secondIO: IO[String] = IO("Millions")

  val firstSecond: IO[String] = firstIO *> secondIO
  val secondFirst: IO[Int] = firstIO <* secondIO

  val anotherCombinator: IO[String] = firstIO >> secondIO

  val sleepingIO = IO.sleep(100.millis)
  val neverEndingIO = IO.println("Start") >> IO.never >> IO.println("Done")

  override def run: IO[Unit] = neverEndingIO
}
