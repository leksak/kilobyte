package simulator

enum class ControlLine {
    RegDst,
    Branch,
    MemRead,
    MemToReg,
    ALUOp,
    MemWrite,
    ALUSrc,
    RegWrite;

    internal var value: Boolean = false
}
