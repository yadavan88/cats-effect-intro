package com.yadavan88.ce
package part8

import cats.effect.IOApp
import cats.effect.IO
import java.util.concurrent.ExecutorService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Success

object BlockingOperations extends IOApp.Simple {
  val blockingPoolExec = for {
    _ <- IO(s"Hello World, welcome to CE Threadpool!").trace
    _ <- IO.blocking {
      println(
        s"[${Thread.currentThread.getName}] This is a blocking call, should run on different threadpool"
      )
    }
  } yield ()

  val customThreadPool = scala.concurrent.ExecutionContext.global
  val ioOnDiffPool = blockingPoolExec.evalOn(customThreadPool) >> IO(
    "This should be in computee pool"
  ).trace

  val aLongBlockingOperation = IO.interruptible {
    println("blocking operation started")
    Thread.sleep(1500)
    println("blocking operation completed")
  }

  val op = for {
    _ <- IO("Initialising").trace
    fib <- aLongBlockingOperation.start
    _ <- IO.sleep(500.millis) >> IO(
      "canceling blocking call"
    ).trace >> fib.cancel
    _ <- IO("Operation ended... ").trace
  } yield ()

  import scala.concurrent.ExecutionContext.Implicits.global
  val future = scala.concurrent.Future {
    Thread.sleep(100)
    println(s"[${Thread.currentThread.getName}] Executing the future operation")
    100
  }

  val asyncIO = IO.async_[Int] { callback =>
    future.onComplete { result =>
      callback(result.toEither)
    }
  }

  val simpleIO = IO("Hello World!")
  val attemptedIO: IO[Either[Throwable, String]] = simpleIO.attempt // becomes Right
  val faiureIO = IO.raiseError[String](new Exception("Some exception"))
  val attemptedFailureIOn: IO[Either[Throwable, String]] = faiureIO.attempt // becomes Left

  override def run: IO[Unit] = attemptedFailureIOn.trace.void
}
