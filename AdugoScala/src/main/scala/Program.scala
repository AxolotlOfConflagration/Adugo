

object Program {
  def main(args: Array[String]): Unit = {

    val steps = if (args.length >= 2) args(1).toInt else 6

    Game(steps).start
  }
}
