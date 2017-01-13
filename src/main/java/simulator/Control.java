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
    if (!op[0] && !op[1] && !op[2] && !op[3] && !op[4] && !op[5]) {
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
    else if (op[0] && op[1] && !op[2] && !op[3] && !op[4] && op[5]) {
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
    else if (op[0] && op[1] && !op[2] && op[3] && !op[4] && op[5]) {
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
    else if (!op[0] && !op[1] && op[2] && !op[3] && !op[4] && !op[5]) {
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
  }
}
