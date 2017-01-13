package simulator;

import common.instruction.Example;
import common.instruction.Instruction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static common.instruction.Format.R;
import static common.instruction.Instruction.BEQ;
import static common.instruction.Instruction.LW;
import static common.instruction.Instruction.SW;
import static common.instruction.decomposedrepresentation.DecomposedRepresentation.asBitPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jwestin on 2017-01-02.
 */
class ALUControlTest {

  private Control aluC;

  @BeforeEach
  void init(){
    aluC = new Control();
  }

  @AfterEach
  void tearDown() {
    aluC = null;
  }

  private void assertRFormat(Control aluC) {
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

  @Test
  public void testMatch() {
    assertTrue(ALUOperation.match(34, "XX0010"));
    assertThat(ALUOperation.from(0b10, 0b100010), is(equalTo(ALUOperation.SUBTRACT)));
  }

  private void assertLWFormat(Control aluC) {
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


  private void assertSWFormat(Control aluC) {
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


  private void assertBEQFormat(Control aluC) {
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
    Instruction i = LW;
    aluC.updateOperationType(i.getOpcode());
    assertLWFormat(aluC);
  }


  @Test
  void testUpdateOperationTypeCheckValuesAfterSWSW() {
    Instruction i = SW;
    aluC.updateOperationType(i.getOpcode());
    assertSWFormat(aluC);
  }


  @Test
  void testUpdateOperationTypeCheckValuesAfterBEQBEQ() {
    Instruction i = BEQ;
    aluC.updateOperationType(i.getOpcode());
    assertBEQFormat(aluC);
  }






}