package idemia.zio

import java.io.IOException

import zio.{DefaultRuntime, IO, Ref, Task, UIO, ZIO}
import zio.console._


object Sandbox extends App {
  val runtime: DefaultRuntime = new DefaultRuntime {}

  runtime.unsafeRun(putStrLn("Hello World!"))

  //creating Effects
  val s1 = ZIO.succeed(42)
  val s2: Task[Int] = Task.succeed(42)


  // lazy Effects
  lazy val bigList = (0 to 1000000).toList
  lazy val bigString = bigList.map(_.toString).mkString("\n")

  val s3 = ZIO.effectTotal(bigString)


  //failing Effects
  val f1 = ZIO.fail("Uh oh!")
  val f2 = Task.fail(new Exception("Uh oh!"))

  //From Scala values
  val zoption: ZIO[Any, Unit, Int] = ZIO.fromOption(Some(2))
  val zoption2: ZIO[Any, String, Int] = zoption.mapError(_ => "It wasn't there!")

  //from Either
  val zeither = ZIO.fromEither(Right("Success!"))

  // from Try
  import scala.util.Try

  val ztry: Task[Int] = ZIO.fromTry(Try(42 / 0))

  //mapping
  //say that UIO - infalliable.
  val succeded: UIO[Int] = IO.succeed(21).map(_ * 2)

  val failed: IO[Exception, Unit] =
    IO.fail("No no!").mapError(msg => new Exception(msg))

  val failedWithRefining: IO[Exception, Unit] =
    IO.fail(new Exception("Boom!")).refineToOrDie[Exception]


  //chaining
  val sequenced =
    getStrLn.flatMap(input => putStrLn(s"You entered: $input"))


  //error handling
  //again UIO inffaliable

  val zeither2: UIO[Either[String, Int]] =
    IO.fail("Uh oh!").either

  val catchAll: IO[IOException, Array[Byte]] =
    openFile("primary.json").catchAll(_ =>
      openFile("backup.json"))

 private def openFile(str: String): IO[IOException ,Array[Byte]] = ???

  /*  Ref
    Ref[A] models a mutable reference to a value of type A.
    The two basic operations are set, which fills the Ref with a new value, and get, which retrieves its current content.
    All operations on a Ref are atomic and thread-safe, providing a reliable foundation for synchronizing concurrent programs.
    */

  val refValue = for {
    ref <- Ref.make(100)
    v1 <- ref.get
    v2 <- ref.set(v1 - 50)
  } yield ref

  //updating a Ref
  def repeat[E, A](n: Int)(io: IO[E, A]): IO[E, Unit] =
    Ref.make(0).flatMap { iRef =>
      def loop: IO[E, Unit] = iRef.get.flatMap { i =>
        if (i < n)
          io *> iRef.update(_ + 1) *> loop
        else
          IO.unit
      }
      loop
    }






























}



