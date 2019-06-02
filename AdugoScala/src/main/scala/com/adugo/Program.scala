package com.adugo

import com.adugo.ai.MiniMaxAlphaBetaPruning
import com.adugo.ai.mcts.MonteCarloPlayer
import com.adugo.game.{Game, GameSaver}
import com.adugo.ai.stateEvaluation.BasicStateEvaluators._

object Program {
  def main(args: Array[String]): Unit = {

    val steps = if (args.length >= 2) args(1).toInt else 6
    val gameSaver = GameSaver("test.txt")

    val minmax = MiniMaxAlphaBetaPruning(steps, simpleJaguarStateEvaluator)
    val mcts = new MonteCarloPlayer()

    println(
      """
        |Choose AI type:
        | * (0) standard
        | * (1) positionAwareAggresive
        | * (2) draw
        | * (3) aggresiveDogs
        | * (4) jaguarLoser
        | * (5) borderJaguar
        | * (6) politeJaguar
        | * (7) simple
        | * (8) positionAwarePolite
      """.stripMargin)

    val rule = io.StdIn.readLine() match {
      case "standard" | "0" => minmax
      case "positionAwareAggresive" | "1" => MiniMaxAlphaBetaPruning(steps, positionAwareStateEvaluator(1.5, -1.5))
      case "draw" | "2" => MiniMaxAlphaBetaPruning(steps, scaredDogsEvaluator)
      case "aggresiveDogs" | "3" => MiniMaxAlphaBetaPruning(steps, aggresiveDogsEvaluator)
      case "jaguarLoser" | "4" => MiniMaxAlphaBetaPruning(steps, jaguarTiresToLoose)
      case "borderJaguar" | "5" => MiniMaxAlphaBetaPruning(steps, jaguarBorderWalker)
      case "politeJaguar" | "6" => MiniMaxAlphaBetaPruning(steps, politeJaguar)
      case "simple" | "7" => MiniMaxAlphaBetaPruning(steps, simplestJaguarStateEvaluator)
      case "positionAwarePolite" | "8" => MiniMaxAlphaBetaPruning(steps, positionAwareStateEvaluator(0.5, 1.5))
      case u =>
        println(s"Unknown AI type: $u. Using standard AI type.")
        minmax
    }

    Thread.sleep(1000)
    Game(rule, 500).start()
  }
}
