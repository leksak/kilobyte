package common.instruction

import common.instruction.decomposedrepresentation.bits

/* Convenience functions */
fun Long.opcode() = this.toInt().opcode()
fun Long.rs() = this.toInt().rs()
fun Long.rt() = this.toInt().rt()
fun Long.rd() = this.toInt().rd()
fun Long.shamt() = this.toInt().shamt()
fun Long.funct() = this.toInt().funct()

/* Convenience functions */
fun Int.opcode() = this shr 26
fun Int.rs() = this.bits(25, 21)
fun Int.rt() = this.bits(20, 16)
fun Int.rd() = this.bits(15, 11)
fun Int.shamt() = this.bits(10, 6)

// This is the same as: return 0b111111 & this;
fun Int.funct() = 63 and this

