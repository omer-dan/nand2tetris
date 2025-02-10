// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

(CHOOSE)
    // First pixel address
    @SCREEN
    D=A
    @addr
    M=D

    // Define number of rows and cols of screen (num -1)
    @255
    D=A
    @numrows
    M=D
    @31
    D=A
    @numcols
    M=D
    
    // Check if key pressed
    @KBD
    D=M
    // If key not pressed - color=white
    @val
    M=0
    @COLOR
    D;JEQ

    // key is presed - color=black
    @val
    M=-1

(COLOR)
    // define iterators
    @i
    M=0
    @j
    M=0

(ROWLOOP)
    // check if i < numrows
    @i
    D=M
    @numrows
    D=D-M
    // If finished rows - goes to start
    @CHOOSE
    D;JGT

(COLLOOP)
    // check if i < numrows
    @j
    D=M
    @numcols
    D=D-M
    // If finished cols - start next row in iteration
    @NEXTITER
    D;JGT
    
    // Coloring the selected pixel set
    @val
    D=M
    @addr
    A=M
    M=D
    
    // Select the next pixel set
    @addr
    M=M+1

    // inc j
    @j
    M=M+1
    @COLLOOP
    0;JMP


(NEXTITER)
    // reset j and inc i
    @j
    M=0
    @i
    M=M+1
    @ROWLOOP
    0;JMP
    
    
    






