package simulator;

import common.hardware.Register;

/**
 * Created by jwestin on 2017-01-11.
 */
public class ALUArithmetic {


  private final DataMemory dataMemory;

  public ALUArithmetic(DataMemory dataMemory) {
    this.dataMemory = dataMemory;
  }

  public int Arithmetic(ALUOperation.Operation aluCTL,
                         Register a,
                         Register b) {
    int output;
    output = 0;
    assert(aluCTL != null);
    assert(a != null);
    assert(b != null);
    int aValue = a.getValue();
    int bValue = b.getValue();
    switch(aluCTL) {
      case AND:
        output = aValue & bValue;
        break;
      case OR:
        output = aValue | bValue;
        break;
      case ADD:
        output = aValue + bValue;
        break;
      case SUBTRACT:
        output = aValue - bValue;
        break;
      case SETONLESSTHAN:
        output = aValue < bValue ? 1 : 0;
        break;
      case NOR:
        output = ~(aValue | bValue);
        //Not implemented in ALU-OP
        break;
      default:
          output = 0;
          break;
    }
    return output;

  }
}
