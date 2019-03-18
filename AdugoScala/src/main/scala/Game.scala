import io.StdIn._

case class Game(depth: Int) {
  private var board = Board.start

  def start = {
    println("Want to play as jaguar(j) or dogs(d)?")
    val choice = readChar()

    if (choice == 'j') {
      do {
        humanJaguar()
        aiDogs()
      } while (!board.isGameOver)
    } else {
      do {
        aiJaguar()
        humanDogs()
      } while (!board.isGameOver)
    }

    if (board.isJaguarDefeated) {
      println("Dogs wins!")
    } else {
      println("Jaguar wins!")
    }
  }

  def humanDogs() = if (!board.isGameOver) {
    var dog = -1
    var destination = -1

    board.print()

    do {
      println(s"Select dog to move: ${board.movableDogs}")
      dog = readInt()
    } while (!board.movableDogs.contains(dog))

    do {
      println(s"Select dog $dog destination: ${board.moves(dog)}")
      destination = readInt()
    } while (!board.moves(dog).contains(destination))

    board = board.moveDog(dog, destination)
  }

  def humanJaguar() = if (!board.isGameOver) {
    var destination = -1

    board.print()

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

    board = board.moveJaguar(destination)
  }

  def aiDogs() = if (!board.isGameOver) {
    val (dog, destination, _) = (for {
      dog <- board.movableDogs
      move <- board.moves(dog)
    } yield (dog, move, Board.minMax(board.moveDog(dog, move), depth, false)))
      .minBy(_._3)

    board = board.moveDog(dog, destination)
  }

  def aiJaguar() = if (!board.isGameOver) {
    var destination = -1


    board = board.moveJaguar(destination)
  }
}
