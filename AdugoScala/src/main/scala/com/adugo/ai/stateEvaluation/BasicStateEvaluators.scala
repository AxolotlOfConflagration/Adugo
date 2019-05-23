package com.adugo.ai.stateEvaluation

import com.adugo.game.Board
import com.adugo.game.Board.maxDogsCount

object BasicStateEvaluators {

  /**
    * Jaguar tries to:
    *   - kill as many dogs as possible
    *   - has as much space as possible
    */
  def simplestJaguarStateEvaluator(board: Board): Double = {
    (maxDogsCount - board.dogs.length) * 1.5  + board.moves(board.jaguar).length
  }

  def simpleJaguarStateEvaluator(board: Board): Double = {
    (maxDogsCount - board.dogs.length) + board.moves(board.jaguar).length + board.turn
  }

  def positionAwareStateEvaluator(killMultiplier: Double, playLengthMultiplier: Double): Board => Double = {
    board: Board => {
      val jaguar = board.jaguar

      positionMultiplier(jaguar) * // Jaguar position rating
        board.moves(jaguar).length + // Jaguar freedom rating
        (maxDogsCount - board.dogs.length) * killMultiplier + // Jaguar kill rating
        playLengthMultiplier * board.turn // Play length
    }
  }

  def scaredDogsEvaluator(board: Board): Double = {
    val jaguar = board.jaguar
    -positionMultiplier(jaguar)
  }

  def aggresiveDogsEvaluator(board: Board): Double = {
    val jaguar = board.jaguar
    positionMultiplier(jaguar)
  }

  // Jaguar border
  def jaguarTiresToLoose(board: Board): Double = {
    val moves = board.moves(board.jaguar).length
    if(moves == 0) 10000000
    else if (moves == 1) 100000
    else 1.0 / moves.toDouble * 30.0
  }

  def jaguarBorderWalker(board: Board): Double = {
    borderMultiplier(board.jaguar)
  }

  def politeJaguar(board: Board): Double = {
    Math.pow(board.dogs.length, 3)
  }

  val positionMultiplier: Array[Double] = Array[Double](xs =
    0.1, 0.1, 0.1, 0.1, 0.1,
    0.1, 0.8, 0.9, 0.8, 0.1,
    0.1, 0.7, 0.7, 0.7, 0.1,
    0.1, 0.6, 0.6, 0.6, 0.1,
    0.1, 0.3, 0.4, 0.3, 0.1,
    0.0, 0.2, 0.2, 0.2, 0.0,
    0.1, 0.0, 0.2, 0.0, 0.1,
  )

  val drawMultiplier: Array[Double] = Array[Double](xs =
    100, 100, 100, 100, 100,
    50, 50, 50, 50, 50,
    10, 10, 10, 10, 10,
    1, 1, 1, 1, 1,
    -1, -1, -1, -1, -1,
    0, -5, -5, -5, 0,
    -10, 0, -10, 0, -10,
  )

  val borderMultiplier: Array[Double] = Array[Double](xs =
    100, 100, 100, 100, 100,
    100, -50, -50, -50, 100,
    100, -50, -50, -50, 100,
    100, -50, -50, -50, 100,
    100, 100, 100, 100, 100,
    0, 100, -50, 100, 0,
    100, 0, 100, 0, 100,
  )
}
