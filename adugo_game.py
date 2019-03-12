from enum import Enum
from copy import deepcopy
import sys, subprocess, pprint

class PointStatus(Enum):
    Blank = 0
    Dog = 1
    Jaguar = 2

class GameBoard:
    def __init__(self):
        self.whoseMove = 'JAGUAR'
        self.MAX_DOGS = 14
        # Game board is list of tuple ([point_id, point_status], [other connected points])
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
    
    def min_max_jaguar(self, game_state, depth, is_jaguar_move):
        '''
            Min-max for Jaguar player. Jaguar tires to eliminate dogs 
        '''
        if depth == 0 or self.game_over():
            return self.countDogs()

        if is_jaguar_move:
            minEval = self.MAX_DOGS
            for jaguar_pos in self.possible_jaguar_moves(tupled=False):
                next_state = GameBoard()

    def min_max_dogs(self, game_state, depth, is_dog_move):
        '''
            Min-max for Dogs is to minimalize Jaguar moves
        '''


    def game_over(self):
        '''
            Returns True if game is over or False otherwise
        '''
        return self.jaguar_game_over() or self.dog_game_over()

    def jaguar_game_over(self):
        '''
            Returns True if jaguar lost
        '''
        return not self.possible_jaguar_moves(tupled=False)

    def dog_game_over(self):
        '''
            Returns True if dogs lost 
        '''
        return self.countDogs() < 10

    def startGame(self):
        while(True):
            self.printBoard()
            if not self.user_jaguar_move():
                print("Dogs won!")
                break
            subprocess.run(['clear'])
            self.printBoard()
            if not self.user_dog_move():
                print("Jaguar won!")
                break
            subprocess.run(['clear'])
        print("Do you want to restart game? ( y/n )")
        decision = input()
        if decision ==  'y':
            self.restartGame()
            self.startGame()
        else: sys.exit()

    def user_jaguar_move(self):
        possibleJumpPositions, possibleMovePositions = self.possible_jaguar_moves()
        possibleJumpPositionsTab = [i[1] for i in possibleJumpPositions] 
        possiblePositions = possibleJumpPositionsTab + possibleMovePositions
        if not possiblePositions: return False
        move =  -1
        while(move not in possiblePositions):
            print("JAGUAR - Possible moves:")
            for i in possibleJumpPositions:
                print("     * jump to {1} through {0}".format(i[0],i[1]))
            for i in possibleMovePositions:
                print("     * move to {}".format(i))
            move = int(input())
        jagPos = self.jaguar_position()
        self.gameBoard[move-1][0][1], self.gameBoard[jagPos][0][1] = self.gameBoard[jagPos][0][1],  self.gameBoard[move-1][0][1]
        if move in possibleJumpPositionsTab:
            jumpedPointPosition = next(i[0] for i in possibleJumpPositions if i[1] == move)
            self.gameBoard[jumpedPointPosition-1][0][1] = PointStatus.Blank
        return True


    def jaguar_move(self, point, game_state=None):
        '''
            Moves Jaguar to given point (int). If game_state is None then this function uses
            self game board. If game_state is provided then this function returns new game_state 
            as deep copy of previous. This scenario is used for AI.
        '''
        return_state = False
        if game_state != None:
            game_state = deepcopy(game_state)
            return_state = True
        else:
            game_state = self.gameBoard
        
        # Extract possible jump positions
        possibleJumpPositions, _ = self.possible_jaguar_moves()
        possibleJumpPositionsTab = [i[1] for i in possibleJumpPositions] 

        jaguar_pos = self.jaguar_position()
        game_state[point-1][0][1], game_state[jaguar_pos][0][1] = game_state[jaguar_pos][0][1], game_state[point-1][0][1]
        if point in possibleJumpPositionsTab:
            jumpedPointPosition = next(i[0] for i in possibleJumpPositions if i[1] == point)
            game_state[jumpedPointPosition-1][0][1] = PointStatus.Blank

        if return_state:
            return game_state


    def user_dog_move(self):
        if self.countDogs() < 10: return False
        dogsPositions = self.dogsPosition()
        print("DOGS - Possible moves:")
        dogsPositionsWithMoves = {}
        for dog in dogsPositions:
            possiblePositions = self.possible_dog_move(self.gameBoard[dog])
            if possiblePositions: print('     * dog {} can move to {}'.format(dog+1, possiblePositions))
            dogsPositionsWithMoves[dog] = possiblePositions
        print('Choose a dog: ')
        dogNum = int(input())
        print('Choose move: ')
        dogMove = int(input())
        self.gameBoard[dogMove-1][0][1], self.gameBoard[dogNum-1][0][1] = self.gameBoard[dogNum-1][0][1],  self.gameBoard[dogMove-1][0][1]
        return True

    def restartGame(self):
        subprocess.run(['clear'])
        self.gameBoard = self.gameBoardCopy

    def giveGameBoard(self):
        return self.gameBoard
    
    def countDogs(self):
        return sum(1 for point in self.gameBoard if point[0][1] == PointStatus.Dog)
    
    def possible_dog_move(self, point):
        '''
            Returns list of all possible moves of a dog in given point
        '''
        return [i for i in point[1] if self.gameBoard[i-1][0][1] == PointStatus.Blank]

    def possible_jaguar_moves(self, tupled=True):
        '''
            As default returns tuple (list of possible jumps, list of possible moves),
            If tupled is False then returns list of all possible moves
        '''
        point = self.gameBoard[self.jaguar_position()]

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
        if tupled:
            return validBehindDog, self.possible_dog_move(point)
        else:
            return [i[1] for i in validBehindDog] + self.possible_dog_move(point)
    
    def jaguar_position(self):
        for point in self.gameBoard:
            if point[0][1] == PointStatus.Jaguar: return point[0][0]-1
    
    def dogsPosition(self):
        dogs = []
        for point in self.gameBoard:
            if point[0][1] == PointStatus.Dog: dogs.append(point[0][0]-1)
        return dogs

    def printBoard(self):
        class bcolors:
            HEADER = '\033[95m'
            OKBLUE = '\033[94m'
            OKGREEN = '\033[92m'
            WARNING = '\033[93m'
            FAIL = '\033[91m'
            ENDC = '\033[0m'
            BOLD = '\033[1m'
            UNDERLINE = '\033[4m'
        def chooseColor(num):
            if self.gameBoard[num-1][0][1] == PointStatus.Blank:
                return bcolors.OKGREEN+str(self.gameBoard[num-1][0][0])+bcolors.ENDC
            elif self.gameBoard[num-1][0][1] == PointStatus.Dog:
                    return bcolors.OKBLUE+str(self.gameBoard[num-1][0][0])+bcolors.ENDC               
            else: return bcolors.WARNING+str(self.gameBoard[num-1][0][0])+bcolors.ENDC 
        for i in range(1,26):
            if i % 5 == 0:
                print(chooseColor(i))
                if i % 10 == 0:
                    print(' | ／  | ＼  | ／  | ＼ |')
                else:
                    if i == 25:
                        print('        {}  | {}'.format(u'_／', u'＼_'))
                    else:
                        print(' | ＼  | ／  | ＼  | ／ |')
            else:
                if i<10: print(' '+chooseColor(i)+' -- ', end='')
                else: print(chooseColor(i)+' -- ', end='')
        print('      {} -- {} -- {}'.format(chooseColor(26),chooseColor(27),chooseColor(28)))
        print('  {}        |       {}'.format(u'_／', u'＼_'))
        print('{} -------- {} -------- {}'.format(chooseColor(29),chooseColor(30),chooseColor(31)))

p = GameBoard()
p.startGame()
print(p.game_over())

