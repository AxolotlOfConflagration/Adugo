package com.adugo.game

import com.adugo.ai.{HumanPlayer, Player}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import com.adugo.utils.Utils._

import io.StdIn._

case class Game(ai: Player, sleepTime: Int = 100, private val gameSaver: Option[GameSaver] = None) {
  private var board = Board.start

  def start(): Unit = {
    clear

    println("Want to play as jaguar(j), dogs(d) or ai battle(a)?")
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

    if (board.isJaguarDefeated) {
      println("Dogs wins!")
    } else {
      println("Jaguar wins!")
    }

    closeGameSaver()
  }

  private def saveGameState(): Unit = {
    gameSaver.foreach(_.appendToFile(board.toJson().toString()))
  }

  private def closeGameSaver(): Unit = {
    gameSaver.foreach(_.closeFile())
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
