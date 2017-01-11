package simulator;

import common.instruction.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jwestin on 2017-01-11.
 */
class SimulatorTestJUnit {
  Simulator simulator;
  @BeforeEach
  public void setUp() {
    simulator = new Simulator();

  }

  @Test
  public void testALUMockAdd() {
    simulator.setRegisterValue("$t0", 3);
    simulator.setRegisterValue("$t1", 5);
    Instruction add = Instruction.from("add $v0, $t0, $t1");
    simulator.execute(add);
    assertEquals(simulator.getRegisterValue("$v0"), 8);
  }
  @Test
  public void testALUMockSub() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sub $v0, $t0, $t1");
    simulator.execute(instruction);
    assertEquals(simulator.getRegisterValue("$v0"), 2);
  }



  @Test
  public void testALUADD() {
    simulator.execute(Instruction.ADD);
  }

  @Test
  public void testALUSUB() {
    simulator.execute(Instruction.SUB);
  }

  @Test
  public void testALUAND() {
    simulator.execute(Instruction.AND);
  }
  @Test
  public void testALULW() {
    //Should add
    simulator.execute(Instruction.LW);
  }
  @Test
  public void testALUSW() {
    //Should add
    simulator.execute(Instruction.SW);
  }
  @Test
  public void testALUBRANSWHEQUAL() {
    //Should subtract
    simulator.execute(Instruction.BEQ);
  }
  @Test
  public void testALUOR() {
    //Should subtract
    simulator.execute(Instruction.OR);
  }
  @Test
  public void testALUNOR() {
    //Should NOR -- not implemented
    assert false;
    simulator.execute(Instruction.NOR);
  }
  @Test
  public void testALUSLT() {
    //Should Set less on
    simulator.execute(Instruction.SLT);
  }
}