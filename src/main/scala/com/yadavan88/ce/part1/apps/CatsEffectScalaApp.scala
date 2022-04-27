package com.yadavan88.ce

import cats.effect.IO
import cats.effect.unsafe.implicits.global

object CatsEffectScalaApp {

  val scalaIO = IO(println("Welcome to Cats Effect 3"))
  def main(args: Array[String]): Unit = {
    scalaIO.unsafeRunSync()
  }
}
