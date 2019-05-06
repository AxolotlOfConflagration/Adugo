package com.adugo.ai.stateEvaluation

import com.adugo.game.Board
import com.adugo.game.Board.maxDogsCount

object BasicStateEvaluators {

  /**
    * Jaguar tries to:
    *   - kill as many dogs as possible
    *   - has as much space as possible
    */
  def simpleJaguarStateEvaluator(board: Board): Double = {
    (maxDogsCount - board.dogs.length) + board.moves(board.jaguar).length + board.turn
  }

  def positionAvareStateEvaluator(killMultiplier: Double): Board => Double = {
    board: Board => {
      val jaguar = board.jaguar

      positionMultiplier(jaguar) * // Jaguar position rating
        board.moves(jaguar).length + // Jaguar freedom rating
        (maxDogsCount - board.dogs.length) * killMultiplier + // Jaguar kill rating
        board.turn // Play length
    }
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
}
