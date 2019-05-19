package com.adugo

import com.adugo.ai.MinMaxAlphaBetaPruning
import com.adugo.ai.mcts.MonteCarloPlayer
import com.adugo.game.{Game, GameSaver}
import com.adugo.ai.stateEvaluation.BasicStateEvaluators._

object Program {
  def main(args: Array[String]): Unit = {

    val steps = if (args.length >= 2) args(1).toInt else 6
    val gameSaver = GameSaver("test.txt")

    val minmax = MinMaxAlphaBetaPruning(steps, simpleJaguarStateEvaluator)
    val positionAvareMinMax = MinMaxAlphaBetaPruning(steps, positionAvareStateEvaluator(0.9))
    val mcts = new MonteCarloPlayer()

    Game(positionAvareMinMax, 600).start()
  }
}
