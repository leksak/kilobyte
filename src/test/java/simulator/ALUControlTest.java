package simulator;

import common.instruction.Example;
import common.instruction.Instruction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jwestin on 2017-01-02.
 */
class ALUControlTest {

  private ALUControl aluC;

  @BeforeEach
  void init(){
    aluC = new ALUControl();
  }

  @AfterEach
  void tearDown() {
    aluC = null;
  }

  private void assertRFormat(ALUControl aluC) {
    assertTrue(aluC.getRegDst());
    assertFalse(aluC.getAluSrc());
    assertFalse(aluC.getMemtoReg());
    assertTrue(aluC.getRegWrite());
    assertFalse(aluC.getMemRead());
    assertFalse(aluC.getMemWrite());
    assertFalse(aluC.getBranch());
    assertTrue(aluC.getAluOp1());
    assertFalse(aluC.getAluOp0());
  }

  private void assertLWFormat(ALUControl aluC) {
    assertFalse(aluC.getRegDst());
    assertTrue(aluC.getAluSrc());
    assertTrue(aluC.getMemtoReg());
    assertTrue(aluC.getRegWrite());
    assertTrue(aluC.getMemRead());
    assertFalse(aluC.getMemWrite());
    assertFalse(aluC.getBranch());
    assertFalse(aluC.getAluOp1());
    assertFalse(aluC.getAluOp0());
  }


  private void assertSWFormat(ALUControl aluC) {
    //assertFalse(aluC.getRegDst());
    assertTrue(aluC.getAluSrc());
    //assertFalse(aluC.getMemtoReg());
    assertFalse(aluC.getRegWrite());
    assertFalse(aluC.getMemRead());
    assertTrue(aluC.getMemWrite());
    assertFalse(aluC.getBranch());
    assertFalse(aluC.getAluOp1());
    assertFalse(aluC.getAluOp0());
  }


  private void assertBEQFormat(ALUControl aluC) {
    //assertFalse(aluC.getRegDst());
    assertFalse(aluC.getAluSrc());
    //assertFalse(aluC.getMemtoReg());
    assertFalse(aluC.getRegWrite());
    assertFalse(aluC.getMemRead());
    assertFalse(aluC.getMemWrite());
    assertTrue(aluC.getBranch());
    assertFalse(aluC.getAluOp1());
    assertTrue(aluC.getAluOp0());
  }

  @Test
  void testUpdateOperationTypeCheckValuesAfterRFormatADD() {
    Instruction i = Instruction.ADD;
    aluC.updateOperationType(i.getOpcode());
    assertRFormat(aluC);
  }

  @Test
  void testUpdateOperationTypeCheckValuesAfterLWLW() {
    Instruction i = Instruction.LW;
    aluC.updateOperationType(i.getOpcode());
    assertLWFormat(aluC);
  }


  @Test
  void testUpdateOperationTypeCheckValuesAfterSWSW() {
    Instruction i = Instruction.SW;
    aluC.updateOperationType(i.getOpcode());
    assertSWFormat(aluC);
  }


  @Test
  void testUpdateOperationTypeCheckValuesAfterBEQBEQ() {
    Instruction i = Instruction.BEQ;
    aluC.updateOperationType(i.getOpcode());
    assertBEQFormat(aluC);
  }

  @Test
  void testUpdateOperationTypeCheckValuesAfterAllExamples() {
    Iterable<Example> iIterable = Instruction.allExamples();
    for (Example e : iIterable) {
      String mnemonic = e.getMnemonicExample();
      Instruction fromMnemonic = Instruction.from(mnemonic);
      aluC.updateOperationType(fromMnemonic.getOpcode());

      switch (aluC.getFormat()) {
        case R:
          assertRFormat(aluC);
          break;
        case LW:
          assertLWFormat(aluC);
          break;
        case SW:
          assertSWFormat(aluC);
          break;
        case BEQ:
          assertBEQFormat(aluC);
          break;
        default:
          fail("Didn't find matching format");


      }
    }
  }





}