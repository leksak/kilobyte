package simulator;


import lombok.Getter;

public class Control {

  @Getter
  private Boolean regDst, aluSrc, memtoReg, regWrite, memRead, memWrite,
          branch, aluOp1, aluOp0;

  public Control() {
    regDst = false;
    aluSrc = false;
    memtoReg = false;
    regWrite = false;
    memRead = false;
    memWrite = false;
    branch = false;
    aluOp1 = false;
    aluOp0 = false;
  }

  public void updateOperationType(int opCode) {
    boolean[] op = new boolean[6];
    for (int i = 5; i >= 0; i--) {
      op[i] = (opCode & (1 << i)) != 0;
    }

    /* R-Format */
    if (opCode == 0b0) {
      regDst = true;
      aluSrc = false;
      memtoReg = false;
      regWrite = true;
      memRead = false;
      memWrite = false;
      branch = false;
      aluOp1 = true;
      aluOp0 = false;
    }
    /* lw */
    else if (opCode == 0b100011) {
      regDst = false;
      aluSrc = true;
      memtoReg = true;
      regWrite = true;
      memRead = true;
      memWrite = false;
      branch = false;
      aluOp1 = false;
      aluOp0 = false;
    }
    /* sw */
    else if (opCode == 0b101011) {
      //regDst = false;
      aluSrc = true;
      //memtoReg = false;
      regWrite = false;
      memRead = false;
      memWrite = true;
      branch = false;
      aluOp1 = false;
      aluOp0 = false;
    }
    /* beq */
    else if (opCode == 0b000100) {
      //regDst = false;
      aluSrc = false;
      //memtoReg = false;
      regWrite = false;
      memRead = false;
      memWrite = false;
      branch = true;
      aluOp1 = false;
      aluOp0 = true;
    }
    else if (opCode == 0b001000) {
      regDst = false;
      regWrite = true;
      aluSrc = true;
      memRead = false;
      memWrite = false;
      memtoReg = false;
      branch = false;
      aluOp1 = false;
      aluOp0 = false;
    }
  }
}
