# `add_sub_beq.asm`

The file contains the following instructions, in order,

```
add $t0, $t1, $t2
sub $s0, $t0, $v0
beq $3, $8, -2
nop
exit
```

Since registers are by default empty we add "0" to $t0, we subtract nothing ("0") from "$s0",
we then branch if $3 and $8 are equal back to the add-instruction.
Since both registers are empty we will do so always,
never reaching the nop and exit state.

This file allows us to test that we can reset our simulation
even though we are in an infinite loop.