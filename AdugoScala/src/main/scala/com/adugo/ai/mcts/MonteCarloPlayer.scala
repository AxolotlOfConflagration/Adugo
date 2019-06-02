package com.adugo.ai.mcts

import com.adugo.ai.Player
import com.adugo.game.Board
import com.adugo.ai.mcts.MonteCarloPlayer._

import scala.annotation.tailrec

class MonteCarloPlayer extends Player {

  val WIN_SCORE = 10

  /**
    * Chooses best possible move for dogs
    *
    * @param board Current game state
    * @return Tuple (dog, position); dog = dog to move, position = position where to move dog
    */
  override def getDogMove(board: Board): (Int, Int) = {
    val root = Node(board, isJaguarMove = false)
    val tree = Tree(root)

    mcts(tree, root)
    val newBoard = mcts(tree, root)

    val dogMove = board.dogs.diff(newBoard.dogs)
    (dogMove.head, dogMove(1))
  }

  /**
    * Chooses best possible move for jaguar
    *
    * @param board Current game state
    * @return New jaguar position
    */
  override def getJaguarMove(board: Board): Int = {
    val root = Node(board, isJaguarMove = true)
    val tree = Tree(root)

    val newBoard = mcts(tree, root)
    newBoard.jaguar
  }

  private def mcts(tree: Tree, root: Node): Board = {
    val end = System.currentTimeMillis + 3000

    while(System.currentTimeMillis < end ){
      val promisingNode = selectPromisingNode(root)
      if(!promisingNode.board.isGameOver) {
        expandNode(promisingNode)
      }
      var nodeToExplore = promisingNode
      if(promisingNode.childs.nonEmpty) {
        nodeToExplore = promisingNode.randomChild
      }
      val playout = simulateRandomPlayout(nodeToExplore)
      backPropagation(nodeToExplore, playout)
    }

    val winner = root.childWithMaxScore
    winner.board
  }

  @tailrec
  private def selectPromisingNode(node: Node): Node = {
    if(node.childs.isEmpty) node
    else selectPromisingNode(nodeWithHighestUtc(node))
  }

  private def expandNode(node: Node): Unit = {
    val possibleStates = node.allPossibleStates
    node.childs.append(possibleStates: _*)
  }

  private def backPropagation(nodeToExplore: Node, isJaguarMove: Boolean): Unit = {
    var tempNode = nodeToExplore
    while(tempNode != null) {
      tempNode.incrementVisit()
      if(tempNode.isJaguarMove == isJaguarMove){
        tempNode.addScore(WIN_SCORE)
      }
      tempNode = tempNode.parent
    }
  }

  private def simulateRandomPlayout(node: Node): Boolean = {
    var tempNode = node.copy()
    var status = false
    if(node.isJaguarMove && node.board.isJaguarDefeated) {
      node.parent.winScore = Int.MinValue
      return false
    }
    var x = 0
    while(!node.board.isGameOver && x < 10) {
      tempNode = tempNode.randomPlay()
      status = tempNode.isJaguarMove
      x += 1
    }
    status
  }
}

object MonteCarloPlayer {
  def utcValue(totalVisits: Int, nodeWinSore: Double, nodeVisits: Int): Double = {
    if (nodeVisits == 0) Int.MaxValue.toDouble
    else (nodeWinSore.toDouble / nodeVisits.toDouble) +
      1.41 * Math.sqrt(Math.log(totalVisits) / nodeVisits.toDouble)
  }

  def nodeWithHighestUtc(node: Node): Node = {
    val parentVisit = node.visitCount
    node.childs.maxBy(child => utcValue(parentVisit, child.winScore, child.visitCount))
  }
}
