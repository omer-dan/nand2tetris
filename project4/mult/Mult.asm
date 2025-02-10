// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
// The algorithm is based on repetitive addition.
    
    // R2 = 0
    @R2
    M=0

    // if R0 == 0 or R1 == 0 goto end
    @R0
    D=M
    @END
    D;JEQ
    @R1
    D=M
    @END
    D;JEQ
    
//LOOP - R1 times
    // iterator = R1
    @i
    M=D
(LOOP)
    // i=i-1, if i <= 0 end
    @i
    D=M
    M=M-1
    @END
    D;JLE
    // R2=R2+R0
    @R0
    D=M
    @R2
    M=D+M
    @LOOP
    0;JMP

(END)
    @END
    0;JMP






