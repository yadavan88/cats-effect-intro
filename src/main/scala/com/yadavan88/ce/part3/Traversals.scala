package com.yadavan88.ce
package part3

import cats.effect.IOApp
import cats.effect.IO
import cats.syntax.apply._
import cats.syntax.parallel._
import cats.Traverse

object Traversals extends IOApp.Simple {

  val io1 = IO("Hello")
  val io2 = IO("World")
  val forCombined = for {
    res1 <- io1.trace
    res2 <- io2.trace
  } yield ()

  val parCombined = io1.trace &> io2.trace

  val catMapN: IO[(String, String)] = (io1, io2).mapN((i, j) => (i, j))
  val catTupled: IO[(String, String)] = (io1, io2).tupled

  val parMapIO: IO[String] = (io1.trace, io2.trace).parMapN(_ + _).trace
  val parTupledIO = (io1.trace, io2.trace).parTupled.trace

  val listTraverse = Traverse[List]

  val ioList: List[IO[String]] = List(io1.trace, io2.trace)
  val insideOutIOs: IO[List[String]] = listTraverse.sequence(ioList)
  val traversedList: IO[List[String]] =
    listTraverse.traverse(ioList)(io => io.map(_ + "!"))

  val seqAsTraverse: IO[List[String]] = listTraverse.traverse(ioList)(identity)

  val parTraverseIOs: IO[List[String]] = ioList.parTraverse(identity)
  val parSeq: IO[List[String]] = ioList.parSequence

  override def run: IO[Unit] = parSeq.trace.void
}
