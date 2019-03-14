from enum import Enum
from copy import deepcopy
import sys, subprocess, pprint
from typing import List, Optional, Union


class PointStatus(Enum):
    Blank = 0
    Dog = 1
    Jaguar = 2
    Nothing = 3


class Point:
    def __init__(self, id: int, status: PointStatus, connected: List[int]):
        self.id = id
        self.status = status
        self.connected = connected


class Jump:
    def __init__(self, to: int, over: int):
        self.to = to
        self.over = over


class GameBoard:
    def __init__(self, game=None):
        self.whoseMove = 'JAGUAR'
        self.MAX_DOGS = 14
        self.DEPTH = 3

        if game is None:
            self.game_board: List[Point] = [
                Point(0, PointStatus.Dog, [2, 6, 7]),
                Point(1, PointStatus.Dog, [1, 3, 7]),
                Point(2, PointStatus.Dog, [2, 4, 7, 8, 9]),
                Point(3, PointStatus.Dog, [3, 5, 9]),
                Point(4, PointStatus.Dog, [4, 9, 10]),
                Point(5, PointStatus.Dog, [1, 7, 11]),
                Point(6, PointStatus.Dog, [1, 2, 3, 6, 8, 11, 12, 13]),
                Point(7, PointStatus.Dog, [3, 7, 9, 13]),
                Point(8, PointStatus.Dog, [3, 4, 5, 8, 10, 13, 14, 15]),
                Point(9, PointStatus.Dog, [5, 9, 15]),
                Point(10, PointStatus.Dog, [6, 7, 12, 16, 17]),
                Point(11, PointStatus.Dog, [7, 11, 13, 17]),
                Point(12, PointStatus.Jaguar, [7, 8, 9, 12, 14, 17, 18, 19]),
                Point(13, PointStatus.Dog, [9, 13, 15, 19]),
                Point(14, PointStatus.Dog, [9, 10, 14, 19, 20]),
                Point(15, PointStatus.Blank, [11, 17, 21]),
                Point(16, PointStatus.Blank, [11, 12, 13, 16, 18, 21, 22, 23]),
                Point(17, PointStatus.Blank, [13, 17, 19, 23]),
                Point(18, PointStatus.Blank, [13, 14, 15, 18, 20, 23, 24, 25]),
                Point(19, PointStatus.Blank, [15, 19, 25]),
                Point(20, PointStatus.Blank, [16, 17, 22]),
                Point(21, PointStatus.Blank, [17, 21, 23]),
                Point(22, PointStatus.Blank, [17, 18, 19, 22, 24, 27, 28, 29]),
                Point(23, PointStatus.Blank, [19, 23, 25]),
                Point(24, PointStatus.Blank, [19, 20, 24]),
                Point(25, PointStatus.Nothing, []),
                Point(26, PointStatus.Blank, [23, 28, 31]),
                Point(27, PointStatus.Blank, [23, 27, 29, 33]),
                Point(28, PointStatus.Blank, [23, 28, 35]),
                Point(29, PointStatus.Nothing, []),
                Point(30, PointStatus.Blank, [27, 33]),
                Point(31, PointStatus.Nothing, []),
                Point(32, PointStatus.Blank, [28, 31, 35]),
                Point(33, PointStatus.Nothing, []),
                Point(34, PointStatus.Blank, [29, 33]),
            ]
            for point in self.game_board:
                point.connected = [x - 1 for x in point.connected]
        else:
            self.game_board: List[Point] = deepcopy(game.game_board)
        self.game_board_copy = deepcopy(self.game_board)

    def simulate_jaguar(self, move):
        future_board = GameBoard(self)
        future_board.move_jaguar(move)
        return future_board

    def simulate_dog(self, dog, move):
        future_board = GameBoard(self)
        future_board.dog_move(dog, move)
        return future_board

    def min_max(self, depth: int, is_jaguar_move: bool, start=False):
        """
            Min-max. When start=True, then for jaguar function returns best move to take;
            For dogs it returns tuple of best dog and move.
            Dogs tries to maximize amount of alive dogs
            Jaguar tries to minimize number of alive dogs
        """
        if start:
            if is_jaguar_move:
                # When it is Jaguar turn we tries to find minimal number od dogs
                move_rewards = {}
                for move in self.possible_jaguar_moves(tupled=False):
                    move_rewards[move] = self.simulate_jaguar(move).min_max(depth, False)
                return min(move_rewards, key=move_rewards.get)
            else:
                dog_move_rewards = {}
                for dog in self.dogs_positions():
                    for move in self.possible_moves(dog):
                        dog_move_rewards[(dog, move)] = self.simulate_dog(dog, move).min_max(depth, True)
                return max(dog_move_rewards, key=dog_move_rewards.get)

        elif depth == 0 or self.game_over():
            return self.count_dogs()

        elif is_jaguar_move:
            min_value = self.MAX_DOGS
            for jaguar_move in self.possible_jaguar_moves(tupled=False):
                future_value = self.simulate_jaguar(jaguar_move).min_max(depth - 1, False)
                min_value = min(min_value, future_value)
            return min_value
        else:
            max_value = 0
            for dog in self.dogs_positions():
                for dog_move in self.possible_moves(self.game_board[dog]):
                    future_value = self.simulate_dog(dog, dog_move).min_max(depth - 1, True)
                    max_value = max(max_value, future_value)
            return max_value

    def min_max_dogs(self, game_state, depth, is_dog_move):
        """
            Min-max for Dogs is to minimalize Jaguar moves
        """
        pass

    def game_over(self):
        """
            Returns True if game is over or False otherwise
        """
        return self.jaguar_game_over() or self.dog_game_over()

    def jaguar_game_over(self):
        """
            Returns True if jaguar lost
        """
        return not self.possible_jaguar_moves(tupled=False)

    def dog_game_over(self):
        """
            Returns True if dogs lost
        """
        return self.count_dogs() < 10

    def ai_dog_move(self):
        if self.dog_game_over():
            return False
        best_dog, best_move = self.min_max(self.DEPTH, False, True)
        self.dog_move(best_dog, best_move)
        return True

    def start_game(self):
        while True:
            self.print_board()
            if not self.user_jaguar_move():
                print("Dogs won!")
                break
            subprocess.run(['clear'])
            self.print_board()
            if not self.ai_dog_move(): # self.user_dog_move():
                print("Jaguar won!")
                break
            subprocess.run(['clear'])
        print("Do you want to restart game? ( y/n )")
        decision = input()
        if decision == 'y':
            self.restart_game()
            self.start_game()
        else:
            sys.exit()

    def user_jaguar_move(self):
        possible_jump_points, possible_move_indices = self.possible_jaguar_moves()
        possible_jump_indices = [jump.to for jump in possible_jump_points]
        possible_indices = possible_jump_indices + possible_move_indices
        if not possible_indices:
            return False

        move = -1
        while move not in possible_indices:
            print("JAGUAR - Possible moves:")
            for i in possible_jump_points:
                print("     * jump to {0} through {1}".format(i.to + 1, i.over + 1))
            for i in possible_move_indices:
                print("     * move to {}".format(i+1))
            move = int(input()) - 1
        self.move_jaguar(move)
        return True

    def swap(self, x, y):
        self.game_board[x].status, self.game_board[y].status = self.game_board[y].status, self.game_board[x].status

    def move_jaguar(self, destination_id):
        """
            Moves Jaguar to given point where point is position
        """
        # Extract possible jump positions
        possible_jump_points, _ = self.possible_jaguar_moves()
        possible_jump_indices = [jump.to for jump in possible_jump_points]

        jaguar_index = self.get_jaguar_index()
        self.swap(destination_id, jaguar_index)

        if destination_id in possible_jump_indices:
            jumped_over = next(jump.over for jump in possible_jump_points if jump.to == destination_id)
            self.game_board[jumped_over].status = PointStatus.Blank

    def dog_move(self, dog, point):
        self.swap(dog, point)

    def can_dog_move(self, dog):
        if type(dog) == int:
            return len(self.possible_moves(self.game_board[dog])) != 0
        else:
            return len(self.possible_moves(dog)) != 0

    def user_dog_move(self):
        if self.dog_game_over():
            return False

        dogs_positions = list(filter(self.can_dog_move, self.dogs_positions()))
        print("DOGS - Possible moves:")
        dogs_positions_with_moves = {}
        for dog in dogs_positions:
            possible_positions = self.possible_moves(dog)
            if possible_positions:
                print('     * dog {} can move to {}'.format(dog + 1, [x+1 for x in possible_positions]))
            dogs_positions_with_moves[dog] = possible_positions

        dog_id = -1
        dog_move = -1
        while dog_id not in dogs_positions:
            print('Choose a dog: ')
            dog_id = int(input()) - 1

        while dog_move not in self.possible_moves(dog_id):
            print('Choose move: ')
            dog_move = int(input()) - 1
        self.dog_move(dog_id, dog_move)
        return True

    def restart_game(self):
        subprocess.run(['clear'])
        self.game_board = self.game_board_copy

    def count_dogs(self):
        return sum(1 for point in self.game_board if point.status == PointStatus.Dog)

    def possible_moves(self, point: Union[int, Point]):
        """
        Returns indices of all blank neighbouring points
        :param point: Can be 'int' or 'Point'
        :return: List of empty neighbours
        """
        if type(point) == int:
            point = self.game_board[point]
        return [x for x in point.connected if self.game_board[x].status == PointStatus.Blank]

    def possible_jaguar_jumps(self) -> List[Jump]:
        jaguar_point = self.game_board[self.get_jaguar_index()]

        # Check for dogs around jaguar
        dogs = [i for i in jaguar_point.connected if self.game_board[i].status == PointStatus.Dog]
        # dogs_values is list of a tuple (dog_id, difference between jaguar and dog ids)
        dogs_values = [(dog, jaguar_point.id - dog) for dog in dogs]
        # For each dog around jaguar we trying to find if jaguar can jump over it in straight line
        valid_jumps = []
        for dog in dogs_values:
            # Empty filed connected to dog we try to jump over
            possible_jump_destinations = \
                [c for c in self.game_board[dog[0]].connected if self.game_board[c].status == PointStatus.Blank]
            # Finding empty field that we can jump to. To check if jump is in straight line
            # we can check for equality between two distances:
            # abs(dog_pos - jaguar_pos) and abs(dog_pos - jump_pos)
            try:
                jump_destination = list(filter(lambda x: abs(dog[0] - x) == abs(dog[1]), possible_jump_destinations))[0]
                # Check if jump destination is empty
                #if self.game_board[jump_destination].status == PointStatus.Blank:
                valid_jumps.append(Jump(jump_destination, dog[0]))
            except:
                pass
        return valid_jumps

    def possible_jaguar_moves(self, tupled=True):
        """
            As default returns tuple (list of possible jumps, list of possible moves),
            If tupled is False then returns list of all possible moves
        """
        jaguar_point = self.game_board[self.get_jaguar_index()]
        possible_jump_point = self.possible_jaguar_jumps()
        if tupled:
            return possible_jump_point, self.possible_moves(jaguar_point)
        else:
            return [jump.to for jump in possible_jump_point] + self.possible_moves(jaguar_point)

    def get_jaguar_index(self) -> int:
        for point in self.game_board:
            if point.status == PointStatus.Jaguar:
                return point.id

    def dogs_positions(self) -> List[int]:
        dogs = []
        for point in self.game_board:
            if point.status == PointStatus.Dog: dogs.append(point.id)
        return dogs

    def print_board(self):
        class bcolors:
            HEADER = '\033[95m'
            OKBLUE = '\033[94m'
            OKGREEN = '\033[92m'
            WARNING = '\033[93m'
            FAIL = '\033[91m'
            ENDC = '\033[0m'
            BOLD = '\033[1m'
            UNDERLINE = '\033[4m'

        def choose_color(num):
            if self.game_board[num].status == PointStatus.Blank:
                return bcolors.OKGREEN + str(self.game_board[num].id + 1) + bcolors.ENDC
            elif self.game_board[num].status == PointStatus.Dog:
                return bcolors.OKBLUE + str(self.game_board[num].id + 1) + bcolors.ENDC
            else:
                return bcolors.WARNING + str(self.game_board[num].id + 1) + bcolors.ENDC

        for i in range(0, 25):
            if i % 5 == 4:
                print(choose_color(i))
                if i % 10 == 9:
                    print(' | ／  | ＼  | ／  | ＼ |')
                else:
                    if i == 24:
                        print('        {}  | {}'.format(u'_／', u'＼_'))
                    else:
                        print(' | ＼  | ／  | ＼  | ／ |')
            else:
                if i < 9:
                    print(' ' + choose_color(i) + ' -- ', end='')
                else:
                    print(choose_color(i) + ' -- ', end='')
        print('      {} -- {} -- {}'.format(choose_color(26), choose_color(27), choose_color(28)))
        print('  {}        |       {}'.format(u'_／', u'＼_'))
        print('{} -------- {} -------- {}'.format(choose_color(30), choose_color(32), choose_color(34)))


p = GameBoard()
p.start_game()
print(p.game_over())
p.min_max(p, 1, False)
