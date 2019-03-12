from enum import Enum
from copy import deepcopy
import sys, subprocess

class PointStatus(Enum):
    Blank = 0
    Dog = 1
    Jaguar = 2

class GameBoard:
    def __init__(self):
        self.whoseMove = 'JAGUAR'
        self.gameBoard = [
            ([1, PointStatus.Dog], [2,6,7]),
            ([2, PointStatus.Dog], [1,3,7]),
            ([3,PointStatus.Dog], [2, 4, 7, 8, 9]),
            ([4,PointStatus.Dog], [3, 5, 9]),
            ([5,PointStatus.Dog], [4, 9, 10]),
            ([6,PointStatus.Dog], [1, 7, 11]),
            ([7,PointStatus.Dog], [1, 2, 3, 6, 8, 11, 12, 13]),
            ([8,PointStatus.Dog], [3, 7, 9, 13]),
            ([9,PointStatus.Dog], [3, 4, 5, 8, 10, 13, 14, 15]),
            ([10,PointStatus.Dog], [5, 9, 15]),
            ([11,PointStatus.Dog], [6, 7, 12, 16, 17]),
            ([12,PointStatus.Dog], [7, 11, 13, 17]),
            ([13,PointStatus.Jaguar], [7, 8, 9, 12, 14, 17, 18, 19]),
            ([14,PointStatus.Dog], [9, 13, 15, 19]),
            ([15,PointStatus.Dog], [9, 10, 14, 19, 20]),
            ([16,PointStatus.Blank], [11, 17, 21]),
            ([17,PointStatus.Blank], [11, 12, 13, 16, 18, 21, 22, 23]),
            ([18,PointStatus.Blank], [13, 17, 19, 23]),
            ([19,PointStatus.Blank], [13, 14, 15, 18, 20, 23, 24, 25]),
            ([20,PointStatus.Blank], [15, 19, 25]),
            ([21,PointStatus.Blank], [16, 17, 22]),
            ([22,PointStatus.Blank], [17, 21, 23]),
            ([23,PointStatus.Blank], [17, 18, 19, 22, 24, 27, 28, 22]),
            ([24,PointStatus.Blank], [19, 23, 25]),
            ([25,PointStatus.Blank], [19, 20, 24]),
            ([27,PointStatus.Blank], [23, 28, 31]),
            ([28,PointStatus.Blank], [23, 27, 29, 33]),
            ([29,PointStatus.Blank], [23, 28, 35]),
            ([31,PointStatus.Blank], [27, 33]),
            ([33,PointStatus.Blank], [28, 31, 35]),
            ([35,PointStatus.Blank], [29, 33]),
        ]
        self.gameBoardCopy = deepcopy(self.gameBoard)

    def startGame(self):
        while(True):
            if not self.jaguarMove():
                print("Dogs won!")
                break
            if not self.dogsMove():
                print("Jaguar won!")
                break
            print(self.gameBoard)
        print("Do you want to restart game? ( y/n )")
        decision = input()
        if decision ==  'y':
            self.restartGame()
            self.startGame()
        else: sys.exit()

    def jaguarMove(self):
        possibleJumpPositions, possibleMovePositions = self.canJaguarMove(self.gameBoard[self.jaguarPosition()])
        possibleJumpPositionsTab = [i[1] for i in possibleJumpPositions] 
        possiblePositions = possibleJumpPositionsTab + possibleMovePositions
        # jeśli jaguar nie ma ruchu zwróć False
        if not possiblePositions: return False
        move =  -1
        while(move not in possiblePositions):
            print("JAGUAR - Possible moves:")
            for i in possibleJumpPositions:
                print("     * jump to {1} through {0}".format(i[0],i[1]))
            for i in possibleMovePositions:
                print("     * move to {}".format(i))
            move = int(input())
        self.gameBoard[move-1][0][1], self.gameBoard[self.jaguarPosition()][0][1] = self.gameBoard[self.jaguarPosition()][0][1],  self.gameBoard[move-1][0][1]
        if move in possibleJumpPositionsTab:
            jumpedPointPosition = next(i[0] for i in possibleJumpPositions if i[1] == move)
            self.gameBoard[jumpedPointPosition-1][0][1] = PointStatus.Blank
        return True

    def dogsMove(self):
        if self.countDogs() < 6: return False
        dogsPositions = self.dogsPosition()
        print("DOGS - Possible moves:")
        for dog in dogsPositions:
            possiblePositions = self.canMove(self.gameBoard[dog])
            if possiblePositions: print('     * Dog {} can move to {}'.format(dog, possiblePositions))

    def restartGame(self):
        subprocess.run(['clear'])
        self.gameBoard = self.gameBoardCopy

    def giveGameBoard(self):
        return self.gameBoard
    
    def countDogs(self):
        return sum(1 for point in self.gameBoard if point[0][1] == PointStatus.Dog)
    
    def canMove(self, point):
        return [i for i in point[1] if self.gameBoard[i-1][0][1] == PointStatus.Blank]

    def canJaguarMove(self, point):
        # szukamy czy dookoła jaguara są psy
        dogs = [i for i in point[1] if self.gameBoard[i-1][0][1] == PointStatus.Dog]
        # dogsValues zawiera tuple z nr miejsca psa i różnicą pomiędzy numerem jaguara i psa
        dogsValues = [(dog,point[0][0]-dog) for dog in dogs]
        # dla każdego psa dookoła jagura wyznaczamy pola za nim w lini prostej
        validBehindDog = []
        for dog in dogsValues:
            # pola za psem
            behindDog = self.gameBoard[dog[0]-1][1]
            # pole za psem w lini prostej
            try:
                inLineBehindDog = int(list(filter(lambda x: dog[0]-x == dog[1], behindDog))[0])
                # sprawdzenie czy pole jest puste(blank)
                if self.gameBoard[inLineBehindDog-1][0][1] == PointStatus.Blank: validBehindDog.append((dog[0],inLineBehindDog))
            except: pass
        return validBehindDog, self.canMove(point)
    
    def jaguarPosition(self):
        for point in self.gameBoard:
            if point[0][1] == PointStatus.Jaguar: return point[0][0]-1
    
    def dogsPosition(self):
        dogs = []
        for point in self.gameBoard:
            if point[0][1] == PointStatus.Dog: dogs.append(point[0][0]-1)
        return dogs

p = GameBoard()
p.startGame()

