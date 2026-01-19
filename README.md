[![Coverage Status](https://coveralls.io/repos/github/spaghett1/Luegen/badge.svg?branch=main)](https://coveralls.io/github/spaghett1/Luegen?branch=main)

<p align="center">
  <img src="src/main/resources/images/Gemini_Generated_Image_julotcjulotcjulo.png" alt="Logo Lügen" width="500"/>
</p>

This is a scala project about the famous card game "Luegen".
To try it out, clone the repo and type 'sbt run' to start a gui version and a tui version in parallel.

For the tui version:
For any input request, the user can type 'save' and 'load' to save and load the game state into an xml file. So, to load a savefile, the user has to start the game and then type 'load' to load it.

For any input request, the user can type 'undo' and 'redo', do undo and redo the most recent command. For undo, there must be at least one completed command. For redo, there must be at least one undid command.

For the gui version:
The 'save', 'load', 'undo', and 'redo' options can be found in the top left drop-down menu, as well as a quit button.

Luegen Rules:
A whole 52 card deck is dealt evenly to the players.
The first person decides a rank for the round that all players must play.
The player can play one or up to three cards of that rank. Then, the next player has their turn.
If a player doesnt have the specified rank, they can lie (claiming they still played the specified rank) and play up to three cards he desires.

Any player (except the starting player) during their turn can call the lie of the previous player, revealing the last played cards. 

If the previous player lied, all discarded Cards this round are added to their hand. Then, the player calling the lie (the current player) can set a new rank for the round and play their cards.

If the previous player told the truth, the player calling the lie draws all discarded cards. Then, the next player can set a rank for the round and play their cards.

If a player has all four cards of any rank, the cards will be removed from the game (the cards not in the game anymore will be shown on the left side of the screen). This is because if a player has all four, all other players would have to lie, giving the player with all four an unfair advantage.

There is a special rule: The queens are considered "death cards". A player cant put them aside when they have four in his hand, and they cant be set as a rank for the round. Instead, if a player has four queens in their hand, they immediatly lose and the game is over. So, the only way of getting rid of queens is to secretely play them during a lie (e.g a player claims he plays three 2's but plays three queens instead).
