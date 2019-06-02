package com.adugo.game

import botkop.numsca.Tensor
import com.adugo.ai.qlearning.QLearning
import com.adugo.ai.{HumanPlayer, Player}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import com.adugo.utils.Utils._

import io.StdIn._

case class Game(ai: Player, sleepTime: Int = 100, private val gameSaver: Option[GameSaver] = None) {
  private var board = Board.start

  def start(): Unit = {
    clear

    println("Want to play as jaguar(j), dogs(d), qlearning jaguar (q) or ai battle(a)?")
    val choice = readChar()

    if (choice == 'j') {
      do {
        humanJaguar()
        saveGameState()
        aiDogs()
        saveGameState()
      } while (!board.isGameOver)
    } else if (choice == 'd') {
      do {
        aiJaguar()
        saveGameState()
        humanDogs()
        saveGameState()
      } while (!board.isGameOver)
    } else if (choice == 'q') {
      qJaguar()
    } else {
      do {
        aiJaguar()
        saveGameState()
        Thread.sleep(sleepTime)
        aiDogs()
        saveGameState()
        Thread.sleep(sleepTime)
      } while (!board.isGameOver)
    }

    board.print()
    if (board.isJaguarDefeated) {
      println("Dogs wins!")
    } else {
      println("Jaguar wins!")
    }

    closeGameSaver()
  }

  private def saveGameState(): Unit = {
    gameSaver.foreach(_.appendToFile(board.toJson.toString()))
  }

  private def closeGameSaver(): Unit = {
    gameSaver.foreach(_.closeFile())
  }

  def qJaguar(num_episodes: Int = 10, epsilon: Float = 0.7f, learning_rate: Float = 0.1f, gamma: Float = 0.9f): Unit = {
    var rewards = Seq[Int]()
    val ql = QLearning()

    for (i <- 0 to num_episodes) {
      board = Board.start
      print(i.toString + "------------------------------\n\n")
      var done = false
      var reward_sum = 0

      do {
        board.print()
        println(ql.q_values(0))
        val action = ql.policy(board, epsilon)
        val step = ql.step(board, action) //(next_state, reward, done)

        if (step._1 != 100) {

          reward_sum += step._2
          val max = {
            var allActions = Array[Double]()
            for (action <- ql.ACTIONS) {
              allActions :+= ql.q_values(action, board.jaguar).squeeze()
            }
            allActions.max
          }
          ql.q_values(action, board.jaguar) += Tensor(learning_rate * (step._2 + gamma * (max - ql.q_values(action, board.jaguar).squeeze())))
          done = step._3
          if (step._1 != board.jaguar) {
            var jump = false
            if (action == 1) {
              jump = true
            }
            board = board.moveJaguarQ(step._1, jump)
            aiDogs()
          }
        }
      } while (!done)
      rewards :+= reward_sum
    }

  }

  def humanDogs(): Unit = if (!board.isGameOver) {
    board.print()
    val (dog, destination) = HumanPlayer.getDogMove(board)
    board = board.moveDog(dog, destination)
  }

  def humanJaguar(): Unit = if (!board.isGameOver) {
    board.print()
    val destination = HumanPlayer.getJaguarMove(board)
    board = board.moveJaguar(destination)
  }

  def aiDogs(): Unit = if (!board.isGameOver) {
    board.print()
    val (dog, destination) = ai.getDogMove(board)
    board = board.moveDog(dog, destination)
  }

  def aiJaguar(): Unit = if (!board.isGameOver) {
    board.print()
    val destination = ai.getJaguarMove(board)
    board = board.moveJaguar(destination)
  }
}
