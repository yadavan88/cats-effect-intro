package com.yadavan88.ce
package part7

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.kernel.Ref
import java.util.UUID
import scala.concurrent.duration._
import cats.effect.kernel.Deferred
import scala.util.Random
import cats.effect.std.Semaphore
import cats.Traverse
import cats.syntax.all.toTraverseOps
import java.time.LocalDateTime
import cats.effect.std.CountDownLatch
import cats.effect.std.CyclicBarrier
object ConcurrentSync extends IOApp.Simple {

  val mutableState: IO[Ref[IO, Int]] = Ref[IO].of(100)
  val mutableStateV2: IO[Ref[IO, Int]] = IO.ref(100)

  val simpleRefOps = for {
    ref <- IO.ref(0) // initialises a Ref
    value <- ref.get.trace // returns 0
    _ <- ref.set(1) // sets the ref with value 1
    oldValue <- ref.getAndSet(5).trace // returns 1 and set 5
    _ <- ref.get.trace // returns 5
  } yield ()

  val refWithFns = for {
    ref <- IO.ref("Hello World")
    cap <- ref
      .getAndUpdate(_.toUpperCase)
      .trace // returns str and then make ref upper case
    str <- ref.get.trace // prints str in uppercase
    _ <- ref.update(_.toLowerCase) // update the state
    strV2 <- ref.get.trace // prints str in all lower
    firstWord <- ref.updateAndGet(_.split("\\s").head.toUpperCase()).trace
  } yield ()

  val refWithModify = for {
    ref <- IO.ref("Hello World")
    currentStr <- ref.get.trace // prints Hello World
    _ = println(
      "Length of current string is :" + currentStr.length
    ) // returns 11
    len <- ref
      .modify(value => (value.toUpperCase + "!", value.length))
      .trace // similar to getAndUpdate, but returns length
    newStr <- ref.get.trace // prints HELLO WORLD!
    _ = println("Length of updated string is: " + newStr.length) // returns 12
  } yield ()

  def developer(coffeeSignal: Deferred[IO, String]) = for {
    _ <- IO(
      "Developer wants to drink coffee and waiting for it to be ready"
    ).trace
    _ <-
      coffeeSignal.get.trace // impatiantly waiting on coffee machine for it to be prepared
    _ <- IO("Started sipping the divine coffee.. ").trace
  } yield ()

  def coffeeMachine(coffeeSignal: Deferred[IO, String]) = for {
    _ <- IO("Verifying water and coffee beans").trace
    grindingFib <- (IO("Grinding Coffee Beans").trace >> IO.sleep(
      Random.nextInt(500).millis
    ) >> IO("Grinding complete").trace).trace.start
    boilingFib <- (IO("Boiling Water").trace >> IO.sleep(
      Random.nextInt(500).millis
    ) >> IO("Boiling complete").trace).start
    _ <- grindingFib.join
    _ <- boilingFib.join
    _ <- IO("Adding milk").trace
    _ <- IO.sleep(100.millis)
    _ <- coffeeSignal.complete("Coffee Ready!")
  } yield ()

  def makeCoffee = for {
    coffeeSignal <- IO.deferred[String]
    fib1 <- developer(coffeeSignal).start
    fib2 <- coffeeMachine(coffeeSignal).start
    _ <- fib1.join
    _ <- fib2.join
  } yield ()

  def currentTime = LocalDateTime.now
 
  def accessWashroom(person: String, sem: Semaphore[IO]): IO[Unit] = for {
    _ <- IO(s"[$currentTime] $person wants to access the washroom, waiting for getting the access").trace
    _ <- sem.acquire
    _ <- IO(s"[$currentTime] $person got access to washroom and using it now").trace
    _ <- IO.sleep(5.second)
    _ <- IO(s"[$currentTime] $person left the washroom").trace
    _ <- sem.release
  } yield ()

  val persons = (1 to 5).map("Person-" + _).toList
  val washroomAccessPgm = for {
    washrooms <- Semaphore[IO](2)
    fibers <- persons.map(p => accessWashroom(p, washrooms).start).sequence
    _ <- fibers.map(_.join).sequence
  } yield ()

  def accessSafeLocker(approvals: CountDownLatch[IO]) = for {
    _ <- IO("Need to access safe locker.").trace
    _ <- approvals.await
    _ <- IO("Safe Locker opened and accessing the contents now").trace
  } yield ()

  def getApprovals(approvals: CountDownLatch[IO]) = for {
    _ <- IO("Requesting approvals for safe access").trace
    _ <- IO("Officer 1 Approval in progress").trace >> IO.sleep(Random.between(500, 1500).millis) >> IO("Officer 1 Approved").trace
    _ <- approvals.release
    _ <- IO("Officer 2 Approval in progress").trace >> IO.sleep(Random.between(500, 1500).millis) >> IO("Officer 2 Approved").trace
    _ <- approvals.release
    _ <- IO("Officer 3 Approval in progress").trace >> IO.sleep(Random.between(500, 1500).millis) >> IO("Officer 3 Approved").trace
    _ <- approvals.release
  } yield ()

  def safeAccessProcess = for {
    approvals <- CountDownLatch[IO](3)
    fib <- accessSafeLocker(approvals).start
    _ <- getApprovals(approvals)
    _ <- fib.join
  } yield ()

def participant1(barrier: CyclicBarrier[IO]) = for {
_ <- IO("await invoked by participant 1").trace
_ <- IO("Waiting for 2nd participant").trace
_ <-  barrier.await
_ <- IO("All participants ready, so now staring").trace
} yield ()

def participant2(barrier: CyclicBarrier[IO]) = for {
_ <- IO.sleep(1000.millis)
_ <- IO("2nd participant joined").trace
_ <-  barrier.await
} yield ()

val cyclicBarrierPgm = for {
   cyclicBarrier <- CyclicBarrier[IO](2)
   fib1 <- participant1(cyclicBarrier).start
   fib2 <- participant2(cyclicBarrier).start
   _ <- fib1.join
   _ <- fib2.join
} yield ()
  


  override def run: IO[Unit] = cyclicBarrierPgm
}
