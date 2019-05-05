import Utils.clear
import botkop.numsca.Tensor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn._

case class Game(depth: Int, sleepTime: Int = 100, saveGame: Boolean = false) {
  private var board = Board.start
  private val gameSaver = SaveGame("savedGames.txt")

  def start = {
    clear

//    println("Want to play as jaguar(j), dogs(d) or ai battle(a)?")
    val choice = 'q'//readChar()

    if (choice == 'j') {
      do {
        humanJaguar()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
        aiDogs()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
      } while (!board.isGameOver)
    } else if (choice == 'd') {
      do {
        aiJaguar()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
        humanDogs()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
      } while (!board.isGameOver)
    } else if (choice == 'q') {
      qJaguar()
    } else {
      do {
        aiJaguar()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
//        Thread.sleep(sleepTime)
        aiDogs()
        if(saveGame)gameSaver.appendToFile(board.toJson().toString())
//        Thread.sleep(sleepTime)
      } while (!board.isGameOver)
    }

    if (board.isJaguarDefeated) {
      println("Dogs wins!")
    } else {
      println("Jaguar wins!")
    }
    gameSaver.closeFile()
  }

  def qJaguar(num_episodes: Int = 1, epsilon: Float = 0.7f, learning_rate: Float = 0.1f, gamma: Float = 0.9f) = {
    var rewards = Seq[Int]()
    val ql = QLearning()

    for(i <- 0 to num_episodes){
      print(i.toString+"------------------------------\n\n")
      board = Board.start
      var done = false
      var reward_sum = 0

      do{
        board.print()
        val action = ql.policy(board, epsilon)
        val step = ql.step(board, action) //(next_state, reward, done)
        reward_sum += step._2
        val max = {
          var allActions = Array[Double]()
          for(action <- ql.ACTIONS){
            allActions :+= ql.q_values(action, board.jaguar).squeeze()
          }
          allActions.max
        }
        ql.q_values(action, board.jaguar) += Tensor(learning_rate*(step._2 + gamma*(max - ql.q_values(action, board.jaguar).squeeze())))
        done = step._3
        if(step._1 != board.jaguar){
          var jump = false
          if(action == 1){
            jump = true
          }
          board = board.moveJaguarQ(step._1, jump)
          aiDogs()
        }
      }while(!done)
      rewards :+= reward_sum
    }

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

//    board.print()

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
