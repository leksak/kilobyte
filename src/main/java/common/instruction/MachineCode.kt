package common.instruction

import common.instruction.decomposedrepresentation.DecomposedRepresentation

/* Convenience functions */
fun Long.opcode() = this.toInt().opcode()
fun Long.rs() = this.toInt().rs()
fun Long.rt() = this.toInt().rt()
fun Long.rd() = this.toInt().rd()
fun Long.shamt() = this.toInt().shamt()
fun Long.funct() = this.toInt().funct()



/* Convenience functions */
fun Int.opcode() = this shr 26
fun Int.rs() = DecomposedRepresentation.bits(25, 21, this)
fun Int.rt() = DecomposedRepresentation.bits(20, 16, this)
fun Int.rd() = DecomposedRepresentation.bits(15, 11, this)
fun Int.shamt() = DecomposedRepresentation.bits(10, 6, this)

// This is the same as: return 0b111111 & this;
fun Int.funct() = 63 and this

