package com.yadavan88.ce

import cats.effect.IO

extension [A](io: IO[A])
  def trace: IO[A] = for {
    res <- io
    _ = println(s"[${Thread.currentThread.getName}] " + res)
  } yield res
