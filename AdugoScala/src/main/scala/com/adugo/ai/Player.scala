package com.adugo.ai

import com.adugo.game.Board

/**
  * Player that produce pawn moves based on given game state.
  */
trait Player {

  /**
    * Chooses best possible move for dogs
    * @param board Current game state
    * @return Tuple (dog, position); dog = dog to move, position = position where to move dog
    */
  def getDogMove(board: Board): (Int, Int)

  /**
    * Chooses best possible move for jaguar
    * @param board Current game state
    * @return New jaguar position
    */
  def getJaguarMove(board: Board): Int
}
