import java.io.PrintWriter

object Program {
  def main(args: Array[String]): Unit = {
    var board = Board.start

    //    println(board.jaguar)
    //    println(board.dogs)
    //    val dog = 11
    //    println(board.emptyNeighbours(dog))
    //    board = board.moveDog(dog,16)
    //    println(board.jumps)
    //    println(board.moves(board.jaguar))
    //    board = board.moveJaguar(20)
    val svg = board.svg
    //
    //    val expr = 1

    board.print()

    val writer = new PrintWriter("out.html")
    writer.write(
      """<!DOCTYPE html>
        |<html>
        |<body>
      """.stripMargin)
    writer.write(svg)
    writer.write(
      """
        |</body>
        |</html>
      """.stripMargin)
    writer.close()
  }
}
