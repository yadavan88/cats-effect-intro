package com.yadavan88.ce
package part9

import cats.effect.{IO, IOApp}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
object ErrorHandling extends IOApp.Simple {

  val failedIO: IO[Int] = IO.raiseError(new RuntimeException("Boom!"))

  val recoveredFailedIO: IO[Int] = failedIO.recover {
    case ex: ArithmeticException => 0
  }
  val recoveredWithFailedIO: IO[Int] = failedIO.recoverWith { case ex =>
    IO(0)
  }

  val loggedFailedError: IO[Int] = failedIO.onError(ex =>
    IO.println("It failed with message: " + ex.getMessage)
  )

  val orElseResult1: IO[Int] = IO(100).orElse(IO(-1)) //returns IO(100)
  val orElseResult2: IO[Int] = failedIO.orElse(IO(-1)) //returns IO(-1)

  val attemptedIO: IO[Either[Throwable, Int]] = loggedFailedError.attempt
  val handledError = failedIO.handleError(ex => 500)
  val handledErrorWith = failedIO.handleErrorWith(ex => IO(-2))

  val eitherValue: IO[Either[Throwable, String]] = IO.pure(Right("Hello World"))
  val rethrownValue: IO[String] = eitherValue.rethrow

  override def run: IO[Unit] = rethrownValue.void
}
