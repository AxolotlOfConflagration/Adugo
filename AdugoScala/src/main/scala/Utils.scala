object Utils {
  implicit class ConsoleColors(val str: String) {
    import Console._

    def black = s"$BLACK$str$RESET"
    def red = s"$RED$str$RESET"
    def green = s"$GREEN$str$RESET"
    def yellow = s"$YELLOW$str$RESET"
    def blue = s"$BLUE$str$RESET"
    def magenta = s"$MAGENTA$str$RESET"
    def cyan = s"$CYAN$str$RESET"
    def white = s"$WHITE$str$RESET"
  }
}
