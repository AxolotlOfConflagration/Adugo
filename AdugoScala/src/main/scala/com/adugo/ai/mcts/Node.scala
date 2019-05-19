package com.adugo.ai.mcts

import com.adugo.game.Board

import collection.mutable.ListBuffer
import scala.util.Random

case class Node
(
  board: Board,
  isJaguarMove: Boolean,
  var visitCount: Int = 0,
  var winScore: Double = 0.0,
  parent: Node = null,
  childs: ListBuffer[Node] = new ListBuffer[Node]()
){

  def randomChild: Node = {
    Random.shuffle(childs).head
  }

  def childWithMaxScore: Node = {
    childs.maxBy(c => c.visitCount)
  }

  def addScore(score: Double): Unit = {
    if(winScore != Int.MinValue) winScore += score
  }

  def incrementVisit(): Unit = visitCount += 1

  def randomPlay(): Node = {
    val rand = new Random(42)
    println(allPossibleStates)
    if(allPossibleStates.isEmpty) null
    else rand.shuffle(allPossibleStates).head
  }

  def allPossibleStates: List[Node] = {
    if(isJaguarMove) {
      if(board.hasToJump) {
        board.jumps.toList.map(jump => {
          val newBoard = board.moveJaguar(jump.to)
          Node(newBoard, !isJaguarMove, 0, 0.0, this)
        })
      } else {
        board.moves(board.jaguar).toList.map(move => {
          val newBoard = board.moveJaguar(move)
          Node(newBoard, !isJaguarMove, 0, 0.0, this)
        })
      }
    }
    else {
      board.movableDogs.toList.flatMap(dog => {
        board.moves(dog).map(move => {
          val newBoard = board.moveDog(dog, move)
          Node(newBoard, !isJaguarMove, 0, 0.0, this)
        })
      })
    }
  }
}
