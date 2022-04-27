package com.yadavan88.ce.apps

import cats.effect.IOApp
import cats.effect.IO

object CatsEffectSimpleApp extends IOApp.Simple {
  val io: IO[Unit] = IO(println("Welcome to Cats Effect 3"))
  override def run: IO[Unit] = io
}
