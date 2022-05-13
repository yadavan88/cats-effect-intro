package com.yadavan88.ce
package part5

import cats.effect.IOApp
import cats.effect.IO
import scala.concurrent.duration._
import scala.io.Source
import java.io.FileNotFoundException
import cats.effect.kernel.Resource
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.kernel.Outcome.Errored
import cats.effect.kernel.Outcome.Canceled

object ResourceHandling extends IOApp.Simple {

  // acquire resource
  def getSource(fileName: String): IO[Source] =
    IO(Source.fromResource(fileName))

  // use resource
  def readFile(src: Source): IO[String] =
    IO(src.getLines.mkString).trace <* IO("Processing completed").trace

  // close/release resource
  def closeSource(src: Source): IO[Unit] =
    IO(src.close) <* IO("Source closed successfully").trace

  /** Bracket based resource handling * */
  val fileName = "bla.txt"
  // acquire resource
  val fileIO = getSource(fileName) <* IO("Acquired Source successfully").trace
  val fileContent: IO[String] =
    fileIO.bracket(src => readFile(src))(src => closeSource(src))

  // close the resource automatically even if error occurred during processing
  def failedProcessingIO(src: Source): IO[String] = IO {
    println("Started processing file contents")
    throw new RuntimeException("Something went wrong!")
  }

  val fileProcessingWithFailure: IO[String] =
    fileIO.bracket(src => failedProcessingIO(src))(src => closeSource(src))

  val bracketWithCase: IO[String] = fileIO.bracketCase(src => failedProcessingIO(src))((src, outcome) =>
    outcome match {
      case Succeeded(s) =>
        IO("[Bracket Case] successfully processed the resource").trace >> closeSource(src) >> IO.unit
      case Errored(s) =>
        IO("[Bracket Case] Failed while processing").trace >> closeSource(src) >> IO.unit
      case Canceled() =>
        IO("[Bracket Case] Canceled the execution").trace >> closeSource(src) >> IO.unit
    }
  )
  // using Resource for handling files
  val resource: Resource[IO, Source] =
    Resource.make(getSource(fileName))(src => closeSource(src))

  val fileContentUsingResource1: IO[String] =
    resource.use(src =>
      IO("Reading file content from 1st file ").trace >> readFile(src)
    )

  val anotherResource: Resource[IO, Source] =
    Resource.make(getSource("another_file.txt"))(src => closeSource(src))

  val fileContentUsingResource2: IO[String] =
    anotherResource.use(src =>
      IO("Reading file content from 2nd file ").trace >> readFile(src)
    )

  val combinedResource: Resource[IO, (Source, Source)] = for {
    res1 <- resource
    res2 <- anotherResource
  } yield (res1, res2)

  val combinedContent: IO[String] = combinedResource.use((src1, src2) =>
    for {
      content1 <- readFile(src1)
      content2 <- readFile(src2)
    } yield content1 + content2
  )

  val successIO: IO[String] = IO("Simple IO").trace
  val successIOWithFinaliser = successIO.guarantee(
    IO("The IO execution finished and this finaliser is applied").trace.void
  )
  val failedIO: IO[String] = successIO >> IO
    .raiseError(new Exception("Failed during execution"))
    .trace >> IO("IO completed")
  val failedIOWithFinaliser = failedIO.guarantee(
    IO("The IO execution finished and this finaliser is applied").trace.void
  )

  def applyGuaranteeCase[A](io: IO[A]): IO[A] = {
    io.guaranteeCase {
      case Succeeded(success) =>
        success.flatMap(msg =>
          IO("IO successfully completed with value: " + msg).trace.void
        )
      case Errored(ex) =>
        IO("Error occurred while processing, " + ex.getMessage).trace.void
      case Canceled() => IO("Processing got cancelled in between").trace.void
    }
  }

  override def run: IO[Unit] = bracketWithCase.void
}
