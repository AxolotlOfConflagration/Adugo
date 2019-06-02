package com.adugo.ai
import com.adugo.game.Board

import scala.io.StdIn.readInt

object HumanPlayer extends Player {
  /**
    * Chooses best possible move for dogs
    *
    * @param board Current game state
    * @return Tuple (dog, position); dog = dog to move, position = position where to move dog
    */
  override def getDogMove(board: Board): (Int, Int) = {
    var dog = -1
    var destination = -1

    do {
      println(s"Select dog to move: ${board.movableDogs}")
      dog = readInt()
    } while (!board.movableDogs.contains(dog))

    do {
      println(s"Select dog $dog destination: ${board.moves(dog)}")
      destination = readInt()
    } while (!board.moves(dog).contains(destination))

    dog -> destination
  }

  /**
    * Chooses best possible move for jaguar
    *
    * @param board Current game state
    * @return New jaguar position
    */
  override def getJaguarMove(board: Board): Int = {
    var destination = -1

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

    destination
  }
}
