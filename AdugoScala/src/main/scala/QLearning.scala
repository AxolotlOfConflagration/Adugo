import botkop.numsca.Tensor
import botkop.{numsca => ns}
import com.sun.xml.internal.ws.api.model.ExceptionType

import scala.util.Random

case class QLearning() {
  private val MOVE = 0
  private val JUMP = 1

  val ACTIONS = Array(MOVE, JUMP)

  var q_values: Tensor = ns.zeros(ACTIONS.size, Board.connections.size, 1)

  def policy(board: Board, epsilon: Float): Int = {
    val state = board.jaguar
    val rnd = new Random()
    if(rnd.nextFloat() < epsilon){
      Random.shuffle(ACTIONS.toList).head
    }
    else {
      var allActions = Array[Double]()
      for(action <- ACTIONS){
        allActions :+= ns.argmax(q_values(action, state)).squeeze()
      }
      ns.argmax(Tensor(allActions)).squeeze().toInt
    }
  }

  def step(board: Board, action: Int): (Int, Int, Boolean) = {

    var next_state = board.jaguar
    var reward = 0
    var done = false

//    POSSIBLE ACTIONS
    if(action == MOVE){
      next_state = Random.shuffle(board.moves(board.jaguar)).head
    }else if(action == JUMP){
      if(board.jumps.nonEmpty)next_state = Random.shuffle(board.jumps).head.to
    }

//    RULES
    if(action == MOVE){
      reward = -1
    } else if((action == MOVE || action == JUMP) && board.isJaguarDefeated){
      reward = -100
    } else if(action == JUMP){
      reward = 10
    }

    if(board.isGameOver){ done = true}
    else{ done = false}

    print(action)
    print(next_state)

    (next_state, reward, done)
  }
}
