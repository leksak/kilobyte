add $s1, $t1, $t9
sub $s1, $t1, $t9

and $s1, $t1, $t9
or $s1, $t1, $t9
nor $s1, $t1, $t9
    
slt $s1, $t1, $t9
nop
    
lw $s1, 1234($t1)
lw $s1, -1234($t1)
sw $s1, 1234($t1)
sw $s1, -1234($t1)

beq $s1, $t1, 5
beq $s1, $t1, 13
        
addi $s1, $t1, 1234
addi $s1, $t1, -1234

ori $s1, $t1, 1234

srl $s1, $t1, 19
sra $s1, $t1, 19

j 2
jr $s1
            
exit    

