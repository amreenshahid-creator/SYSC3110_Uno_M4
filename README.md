
Breakdown of Project Tasks and Responsibilities:

Amreen Shahid

For the previous milestone, Amreen worked on the Uno Flip features. She added the Flip card, Draw Five, Skip Everyone, and Wild Draw Colour, and updated the scoring to follow the Flip rules. For this milestone, Amreen implemented the replay system for new rounds and new games. She also built the undo and redo features, added the GUI buttons, and made sure the multi-level undo/redo works smoothly.

Marc Aoun

In the previous milestone, Marc worked on the AI features, added the option for AI players, set up basic AI strategy, and updated the class and sequence diagrams. For this milestone, Marc implemented the save and load system. He added the serialization and deserialization features so the user can save a game and reload it later. He also made sure there is proper error-handling for both saving and loading. Marc updated the UML class diagrams for this milestone and created the sequence diagrams for undo and for loading a saved game. 

Iman Elabd

For the previous milestone, Iman continued doing the JUnit testing. She added tests for the Uno Flip cards, tested the AI actions, and made sure everything was running smoothly. She also prepared the README for that milestone and added JavaDocs and comments. For this milestone, Iman wrote the JUnit tests for the undo/redo feature and for serialization and deserialization. She also prepared the user manual, updated the README, added the needed JavaDocs, and kept the GitHub commits organized.

Explanation of AI Player Strategy

The AI does not pick random cards. When it is the AI’s turn, the model looks at all the cards in its hand and checks which ones are legal to play. If none work, the AI must draw.When the AI has more than one card it can use, it usually plays a special card first because those affect the game more, like a skip or a draw card. If it doesn’t have anything like that, it just plays a normal card that matches the colour or the number on the top of the pile. It isn’t trying to be clever or random, it just follows the rules in the simplest way possible.

Breakdown of Other Deliverables

Our project is split into four milestones. Milestone 1 was the text-based game of Uno. Milestone 2 added the full GUI and model testing. Milestone 3 focused on Uno Flip and the AI players. Milestone 4 includes the undo/redo feature and saving and loading the game

for every milestone, we update the code, add new tests, fix the UML diagrams, update our documentation, and write a README that explains what changed and who worked on what. Each milestone is submitted as one zip file with everything included.

Known issues: All features work as expected, and we did not encounter any known issues for this milestone.
