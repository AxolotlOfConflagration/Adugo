

object Program {
  def main(args: Array[String]): Unit = {
    val steps = if (args.length >= 2) args(1).toInt else 6
    Game(steps).start

//    val ql = new QLearning()
//    print(ql.step(new Board(12, (0 to 14).filterNot(_ == 12).toList, 0), 0))
  }
}
