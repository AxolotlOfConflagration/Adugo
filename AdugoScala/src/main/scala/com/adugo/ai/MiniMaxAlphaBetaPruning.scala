package com.adugo.ai
import com.adugo.game.Board

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


case class MiniMaxAlphaBetaPruning(depth: Int, evaluator: Board => Double) extends Player {
  /**
    * Chooses best possible move for dogs
    *
    * @param board Current game state
    * @return Tuple (dog, position); dog = dog to move, position = position where to move dog
    */
  override def getDogMove(board: Board): (Int, Int) = {
    val futureMoves = for {
      dog <- board.movableDogs
      move <- board.moves(dog)
    } yield Future {
      (dog, move, minMax(board.moveDog(dog, move), depth, Int.MinValue, Int.MaxValue, isJaguarMove = false))
    }

    val future = Future.sequence(futureMoves).map(_.minBy(_._3))

    val (dog, position, _) = Await.result(future, Duration.Inf)

    dog -> position
  }

  /**
    * Chooses best possible move for jaguar
    *
    * @param board Current game state
    * @return New jaguar position
    */
  override def getJaguarMove(board: Board): Int = {
    if (board.hasToJump) {(
      for (jump <- board.jumps)
        yield (jump, minMax(board.moveJaguar(jump.to), depth, Int.MinValue, Int.MaxValue, isJaguarMove = true))
      ).maxBy(_._2)._1.to
    }
    else {(
      for (move <- board.moves(board.jaguar))
        yield (move, minMax(board.moveJaguar(move), depth, Int.MinValue, Int.MaxValue, isJaguarMove = true))
      ).maxBy(_._2)._1
    }
  }

  private def minMax(state: Board, currentDepth: Int, alpha: Double, beta: Double, isJaguarMove: Boolean): Double = {
    var a = alpha
    var b = beta

    if (currentDepth == 0 || state.isGameOver) evaluator(state)
    else if (isJaguarMove) {
      var max = Double.MinValue
      if (state.hasToJump) {
        state.jumps.foreach { jump =>
          val futureMax = minMax(state.moveJaguar(jump.to), currentDepth - 1, a, b, isJaguarMove = false)
          max = Math.max(max, futureMax)
          a = Math.max(a, futureMax)
          if (b <= a)
            return max
        }
      }
      else {
        state.emptyNeighbours(state.jaguar)
          .foreach { to =>
            val futureMax = minMax(state.moveJaguar(to), currentDepth - 1, a, b, isJaguarMove = false)
            max = Math.max(max, futureMax)
            a = Math.max(a, futureMax)
            if (b <= a)
              return max
          }
      }
      max
    } else {
      var min = Double.MaxValue
      for {
        dog <- state.movableDogs
        move <- state.moves(dog)
      } {
        val futureMin = minMax(state.moveDog(dog, move), currentDepth - 1, a, b, isJaguarMove = true)
        min = Math.min(min, futureMin)
        b = Math.min(b, futureMin)
        if (b <= a)
          return min
      }
      min
    }
  }
}
