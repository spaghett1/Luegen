package de.htwg.luegen

enum TurnState {
  
  case Played
  case NoTurn
  case NoChallenge
  case ChallengedLieWon
  case ChallengedLieLost
  case Invalid
  case NeedsPlayerCount
  case NeedsPlayerNames
  case NeedsRankInput
  case NeedsCardInput
  case NeedsChallengeDecision
}