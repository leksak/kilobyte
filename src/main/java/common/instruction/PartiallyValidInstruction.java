package common.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public class PartiallyValidInstruction {
  public Instruction instruction;
  public List<String> errors;

  public PartiallyValidInstruction(Instruction instruction, Collection<String> errors) {
    this.instruction = instruction;
    this.errors = new ArrayList<>(errors);
  }

  @Override
  public String toString() {
    StringJoiner sj = new StringJoiner("\", \"", "[\"", "\"]");
    errors.forEach(sj::add);

    return instruction.toString() + " error(s)=" + sj.toString();
  }
}
