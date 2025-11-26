package de.htwg.luegen

enum TurnState {
  
  case Played
  case NoTurn
  case NoChallenge
  case ChallengedLieWon
  case ChallengedLieLost
  case Invalid
}