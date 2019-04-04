import Utils.clear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn._

case class Game(depth: Int, sleepTime: Int = 100) {
  private var board = Board.start
  private var saveGame = SaveGame("test.txt")

  def start = {
    clear

    println("Want to play as jaguar(j), dogs(d) or ai battle(a)?")
    val choice = readChar()

    if (choice == 'j') {
      do {
        humanJaguar()
        saveGame.appendToFile(board.returnBoardState().toString())
        aiDogs()
        saveGame.appendToFile(board.returnBoardState().toString())
      } while (!board.isGameOver)
    } else if (choice == 'd') {
      do {
        aiJaguar()
        saveGame.appendToFile(board.returnBoardState().toString())
        humanDogs()
        saveGame.appendToFile(board.returnBoardState().toString())
      } while (!board.isGameOver)
    } else {
      do {
        aiJaguar()
        saveGame.appendToFile(board.returnBoardState().toString())
        Thread.sleep(sleepTime)
        aiDogs()
        saveGame.appendToFile(board.returnBoardState().toString())
        Thread.sleep(sleepTime)
      } while (!board.isGameOver)
    }

    if (board.isJaguarDefeated) {
      println("Dogs wins!")
    } else {
      println("Jaguar wins!")
    }
    saveGame.closeFile()
  }

  def humanDogs() = if (!board.isGameOver) {
    var dog = -1
    var destination = -1

    board.print()

    do {
      println(s"Select dog to move: ${board.movableDogs}")
      dog = readInt()
    } while (!board.movableDogs.contains(dog))

    do {
      println(s"Select dog $dog destination: ${board.moves(dog)}")
      destination = readInt()
    } while (!board.moves(dog).contains(destination))

    board = board.moveDog(dog, destination)
  }

  def humanJaguar() = if (!board.isGameOver) {
    var destination = -1

    board.print()

    if (board.jumps.nonEmpty) {
      do {
        println(s"Select destination of Jaguar jump: ${board.jumps}")
        destination = readInt()
      } while (!board.jumps.map(_.to).contains(destination))
    } else {
      do {
        println(s"Select destination of Jaguar move: ${board.moves(board.jaguar)}")
        destination = readInt()
      } while (!board.moves(board.jaguar).contains(destination))
    }

    board = board.moveJaguar(destination)
  }

  def aiDogs() = if (!board.isGameOver) {

    board.print()

    val futureMoves = for {
      dog <- board.movableDogs
      move <- board.moves(dog)
    } yield Future {
      (dog, move, Board.minMax(board.moveDog(dog, move), depth, Int.MinValue, Int.MaxValue, isJaguarMove = false))
    }

    val future = Future.sequence(futureMoves).map(_.minBy(_._3))
    val (dog, destination, _) = Await.result(future, Duration.Inf)

    board = board.moveDog(dog, destination)
  }

  def aiJaguar() = if (!board.isGameOver) {

    board.print()

    val destination = if (board.hasToJump) {
      (for (jump <- board.jumps) yield (jump, Board.minMax(board.moveJaguar(jump.to), depth, Int.MinValue, Int.MaxValue, true))).maxBy(_._2)._1.to
    }
    else {
      (for (move <- board.moves(board.jaguar)) yield (move, Board.minMax(board.moveJaguar(move), depth, Int.MinValue, Int.MaxValue, true))).maxBy(_._2)._1
    }

    board = board.moveJaguar(destination)
  }
}
