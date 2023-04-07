package com.yadavan88.ce
package part6

import cats.effect.IOApp
import scala.concurrent.duration._
import cats.effect.IO
import scala.util.Random
import cats.effect.OutcomeIO
import cats.effect.FiberIO

object Cancellation extends IOApp.Simple {

  val longRunningIO =
    (IO("Start processing").trace >> IO.sleep(500.millis) >> IO(
      "Task completed"
    ).trace)
      .onCancel(IO("This IO got cancelled").trace.void)

  val fiberOps = for {
    fib <- longRunningIO.start
    _ <- IO.sleep(200.millis) >> IO("cancelling task!").trace
    _ <- fib.cancel
    res <- fib.join
  } yield ()

  val io1 = IO("Task 1 starting..").trace >> IO
    .sleep(Random.nextInt(1000).millis)
    .trace >> IO("Task 1 completed").trace
  val io2 = IO("Task 2 starting..").trace >> IO
    .sleep(Random.nextInt(1000).millis)
    .trace >> IO("Task 2 completed").trace

  val raceIOs: IO[Either[String, String]] = IO.race(io1, io2)
  val raceResult = raceIOs.map {
    _.match {
      case Right(res) => println(s"io2 finished first: `${res}` ")
      case Left(res)  => println(s"io1 finished first: `${res}` ")
    }
  }

  val racePairResult: IO[Either[
    (OutcomeIO[String], FiberIO[String]),
    (FiberIO[String], OutcomeIO[String])
  ]] = IO.racePair(io1, io2)

  val maybeSlowIO = (IO("Task is starting..").trace >> IO
    .sleep(Random.nextInt(1000).millis)
    .trace >> IO("Task is completed").trace)
    .onCancel(IO("Cancelled this IO since it is slow").trace.void)
  val ioExec: IO[String] = maybeSlowIO.timeout(500.millis)
  val timeoutWithFallback =
    maybeSlowIO.timeoutTo(500.millis, IO("Fallback IO executed").trace)

  val ioCancelled =
    IO("Ongoing IO").trace >> IO.canceled >> IO("completed").trace

  val step1 = IO("Step 1").trace
  val importantTask = IO.uncancelable(unmask =>
    IO("uncancelable start").trace >> IO.sleep(1.second) >> IO(
      "task completed"
    ) >> IO("uncancelable end").trace
  )
  val step3 = IO("Final step ").trace

  val tryCancel = for {
    _ <- step1
    fib <- importantTask.start
    res <- IO.sleep(400.millis) >> IO(
      "trying to cancel importantTask"
    ).trace >> fib.cancel
    _ <- fib.join
  } yield ()

  val fullUncancelable = importantTask.uncancelable

  val maskBlocks = IO.uncancelable(unmask =>
    unmask(IO("Step1")) >> IO("Step2") >> unmask(IO("Step3"))
  )

  override def run: IO[Unit] = tryCancel.void
}
