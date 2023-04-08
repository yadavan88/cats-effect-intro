package com.yadavan88.ce
package part4

import cats.effect.IOApp
import cats.effect.IO
import scala.concurrent.duration._
import scala.util.Random
import cats.effect.kernel.Outcome
import cats.effect.kernel.Outcome._

object Fibers extends IOApp.Simple {

  val io: IO[Int] = IO("Long computation").trace >> IO.sleep(1.second) >> IO(
    Random.nextInt(1000)
  ) <* IO("Computation done!").trace

  val fibersIO: IO[Outcome[IO, Throwable, Int]] = for {
    fiber <- io.start
    fiberOutcome <- fiber.join
    _ <- fiberOutcome match {
      case Succeeded(fa) =>
        IO.println("Fiber succeeded with result: ") >> fa.trace
      case Errored(e) =>
        IO.println("Fiber execution failed with exception: " + e)
      case Canceled() => IO("Fiber operation got cancelled").trace
    }
  } yield fiberOutcome

  val fiberCancellation = for {
    fiber <- io.start
    _ <- IO.sleep(400.millis) >> fiber.cancel
    result <- fiber.join
    _ <- result match {
      case Succeeded(fa) =>
        IO.println("Fiber succeeded with result: ") >> fa.trace
      case Errored(e) =>
        IO.println("Fiber execution failed with exception: " + e)
      case Canceled() => IO("Fiber operation got cancelled").trace
    }
  } yield ()

  val ioWithHook =
    io.onCancel(IO("Finaliser for IO Fiber cancellation executed").trace.void)
  val fiberCancellationV2 = for {
    fiber <- ioWithHook.start
    _ <- IO.sleep(100.millis) >> fiber.cancel
    result <- fiber.join
  } yield result

  override def run: IO[Unit] = fiberCancellationV2.void
}
