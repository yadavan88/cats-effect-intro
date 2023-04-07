package com.yadavan88.ce

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode

object CatsEffectApp extends IOApp {
  val io: IO[Unit] = IO(println("Welcome to Cats Effect 3"))
  def run(args: List[String]): IO[ExitCode] = io.map(_ => ExitCode.Success)
}
