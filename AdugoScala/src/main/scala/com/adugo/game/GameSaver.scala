package com.adugo.game

import java.io.FileWriter

case class GameSaver(fileName: String) {

  private val fw = new FileWriter(fileName, true)

  def appendToFile(data: String): Unit = {
    fw.write(data+"\n")
  }

  def closeFile(): Unit ={
    fw.close()
  }
}
