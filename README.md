# SudokuSandbox
Instructions:
- When running SudokuSandox, make sure all project files are stored into the same Java Project Folder
- Any puzzle file (.txt) must be stored directly into the Java Project Folder as well in order to be imported properly (not the src folder or package)
- When creating a new puzzle file, you must follow the following format:
    - The file consists of 9 rows, each of which has 9 integers
        - Each integer must be within the range 1-9, inclusive, or 0
        - 0's represent tiles that are empty
    - Each integer is separated by one space " "
    - There is no extra white space before and after each row
    - There are no spaces/empty lines between 2 rows
    
    e.g.

  0 0 0 0 9 8 0 0 2 <-- Top of the file
  
  0 1 0 0 3 2 0 0 5
  
  0 0 0 4 1 0 9 0 6
  
  0 3 9 0 8 4 0 0 0
  
  0 2 0 0 0 9 1 0 0
  
  5 0 4 0 6 1 0 9 0
  
  7 0 2 0 4 0 0 6 0
  
  0 9 0 8 2 0 0 0 0
  
  3 0 0 0 7 0 5 2 0



Patch Notes:

v1.1.1
- Added AI minigrid; provides an AI point-of-view for tracking each step of the algorithm
- Improved AI
- Additional files included; required for updated AI

v1.1
- Added option for importing user-made puzzles
- Added option to save GUI-made puzzles

v1.0
- Added interactive GUI for creating and solving your own puzzles
- GUI allows for quick reset if needed for custom puzzles
- Added randomized Puzzle Generator
- Added AI that solves puzzles step by step




