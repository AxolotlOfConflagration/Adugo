import Board._
import Utils._
import play.api.libs.json._

case class Board
(
  jaguar: Int,
  dogs: Seq[Int],
  turn: Int
) {
  def moveJaguar(to: Int): Board = {
    if (hasToJump) {
      if (!canJump(to))
        throw new Exception(s"Jaguar can't jump to $to!")
      val jump = jumps.find(_.to == to).get
      copy(jaguar = jump.to, dogs = dogs.filterNot(_ == jump.over))
    } else {
      if (!canMove(jaguar, to))
        throw new Exception(s"Can't move jaguar to $to!")
      copy(jaguar = to, turn = turn + 1)
    }
  }

  def moveJaguarQ(to: Int, jump: Boolean): Board = {
    if (jump) {
      if (!canJump(to))
        throw new Exception(s"Jaguar can't jump to $to!")
      val jump = jumps.find(_.to == to).get
      copy(jaguar = jump.to, dogs = dogs.filterNot(_ == jump.over))
    } else {
      if (!canMove(jaguar, to))
        throw new Exception(s"Can't move jaguar to $to!")
      copy(jaguar = to, turn = turn + 1)
    }
  }

  /**
    * Jaguar tries to:
    *   - kill as many dogs as possible
    *   - has as much space as possible
    */
  def rateJaguarState: Int = {
    (maxDogsCount - dogs.length) + moves(jaguar).length + turn
  }

  def isJaguarDefeated: Boolean = moves(jaguar).isEmpty

  def areDogsDefeated: Boolean = dogs.length < 10

  def isGameOver: Boolean = isJaguarDefeated || areDogsDefeated

  def jumps: Seq[Jump] = for {
    dog <- dogsNeighbours(jaguar)
    jump <- emptyNeighbours(dog) if Math.abs(jaguar - dog) == Math.abs(dog - jump)
  } yield Jump(dog, jump)

  def moves(of: Int): Seq[Int] = emptyNeighbours(of)

  def moveDog(from: Int, to: Int): Board = {
    if (!canMove(from, to))
      throw new Exception(s"Can't move dog from $from to $to!")

    copy(dogs = dogs.filterNot(_ == from) :+ to, turn = turn + 1)
  }

  def hasToJump: Boolean = jumps.nonEmpty

  def canJump(to: Int): Boolean = jumps.map(_.to).contains(to)

  def canMove(from: Int, to: Int): Boolean = emptyNeighbours(from).contains(to)

  def movableDogs: Seq[Int] = dogs.filterNot(emptyNeighbours(_).isEmpty)

  def dogsNeighbours(of: Int): Seq[Int] = connections(of)
    .filter(dogs.contains(_))

  def emptyNeighbours(of: Int): Seq[Int] = connections(of)
    .filterNot(_ == jaguar)
    .filterNot(dogs.contains(_))

  def svg: String = {
    val sb = new StringBuilder()
    sb.append("""<svg width="560" height="760">""" + "\n")

    val points = for {
      x <- 0 to 6
      y <- 0 to 4
    } yield (y * 100 + 30, x * 100 + 30)

    for {
      index <- points.indices
      node <- connections(index)
    } {
      val start = points(index)
      val stop = points(node)

      sb.append(s"""  <line x1="${start._1}" y1="${start._2}" x2="${stop._1}" y2="${stop._2}" style="stroke:black;stroke-width:2"/>""" + "\n")
    }

    dogs.foreach(dog => {
      val dogPos = points(dog)
      sb.append(s"""  <circle cx="${dogPos._1}" cy="${dogPos._2}" r="20" fill="blue"/>""" + "\n")
    })

    val jaguarPos = points(jaguar)
    sb.append(s"""  <circle cx="${jaguarPos._1}" cy="${jaguarPos._2}" r="20" fill="red"/>""" + "\n")

    sb.append("""</svg>""")
    sb.mkString
  }

  def print(): Unit = {

    clear

    val fields = connections
      .indices
      .map(n => {
        val str = f"$n%2d"
        if (dogs.contains(n)) str.blue
        else if (n == jaguar) str.yellow
        else if (connections(n).isEmpty) ""
        else str.white
      })

    val lines = for (i <- 0 until 5) yield fields.slice(i * 5, i * 5 + 5).mkString("--")
    println(lines(0))
    println(""" | \ | / | \ | / |""")
    println(lines(1))
    println(""" | / | \ | / | \ |""")
    println(lines(2))
    println(""" | \ | / | \ | / |""")
    println(lines(3))
    println(""" | / | \ | / | \ |""")
    println(lines(4))
    println("""      _/ | \_     """)
    println(fields.slice(25, 30).filterNot(_ == "").mkString(" " * 4, "--", " " * 4))
    println("""  _/     |    \_  """)
    println(fields.slice(30, 35).filterNot(_ == "").mkString("-" * 6))

    println(s"Dogs killed ${maxDogsCount - dogs.length}")
  }

  def toJson():JsObject = {
    Json.obj("turn" -> turn, "jaguar" -> jaguar, "dogs" -> dogs)
  }
}

object Board {
  val maxDogsCount: Int = 14

  def minMax(state: Board, depth: Int, alpha: Int, beta: Int, isJaguarMove: Boolean): Int = {
    var a = alpha
    var b = beta

    if (depth == 0 || state.isGameOver) state.rateJaguarState
    else if (isJaguarMove) {
      var max = Int.MinValue
      if (state.hasToJump) {
        state.jumps.foreach { jump =>
          val futureMax = minMax(state.moveJaguar(jump.to), depth - 1, a, b, isJaguarMove = false)
          max = Math.max(max, futureMax)
          a = Math.max(a, futureMax)
          if (b <= a)
            return max
        }
      }
      else {
        state.emptyNeighbours(state.jaguar)
          .foreach { to =>
            val futureMax = minMax(state.moveJaguar(to), depth - 1, a, b, isJaguarMove = false)
            max = Math.max(max, futureMax)
            a = Math.max(a, futureMax)
            if (b <= a)
              return max
          }
      }
      max
    } else {
      var min = Int.MaxValue
      for {
        dog <- state.movableDogs
        move <- state.moves(dog)
      } {
        val futureMin = minMax(state.moveDog(dog, move), depth - 1, a, b, isJaguarMove = true)
        min = Math.min(min, futureMin)
        b = Math.min(b, futureMin)
        if (b <= a)
          return min
      }
      min
    }
  }

  val connections: Array[List[Int]] = Array(
    //0
    List(1, 6, 5),
    List(0, 6, 2),
    List(1, 6, 7, 8, 3),
    List(2, 8, 4),
    List(3, 8, 9),
    // 5
    List(0, 6, 10),
    List(0, 1, 2, 7, 12, 11, 10, 5),
    List(2, 8, 12, 6),
    List(2, 3, 4, 9, 14, 13, 12, 7),
    List(4, 8, 14),
    // 10
    List(5, 6, 11, 16, 15),
    List(6, 12, 16, 10),
    List(6, 7, 8, 13, 18, 17, 16, 11),
    List(8, 14, 18, 12),
    List(9, 8, 13, 18, 19),
    // 15
    List(10, 16, 20),
    List(10, 11, 12, 17, 22, 21, 20, 15),
    List(16, 12, 18, 22),
    List(12, 13, 14, 17, 19, 22, 23, 24),
    List(14, 18, 24),
    // 20
    List(15, 16, 21),
    List(20, 16, 22),
    List(16, 17, 18, 23, 28, 27, 26, 21),
    List(18, 24, 22),
    List(18, 19, 23),
    // 25
    Nil,
    List(22, 27, 30),
    List(22, 28, 32, 26),
    List(22, 27, 34),
    Nil,
    // 30
    List(26, 32),
    Nil,
    List(27, 34, 30),
    Nil,
    List(28, 34, 32)
  ).map(_.sorted)

  val start = Board(12, (0 to 14).filterNot(_ == 12).toList, 0)
}