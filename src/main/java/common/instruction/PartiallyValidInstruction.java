package common.instruction;

import lombok.val;

import java.util.Collection;
import java.util.StringJoiner;

public class PartiallyValidInstruction {
  public final Instruction instruction;
  public final String errors;

  public PartiallyValidInstruction(Instruction instruction, String errors) {
    this.instruction = instruction;
    this.errors = errors;
  }

  public PartiallyValidInstruction(Instruction instruction, Collection<String> errors) {
    this.instruction = instruction;
    val sj = new StringJoiner("\", \"", "[\"", "\"]");
    errors.forEach(sj::add);
    this.errors = sj.toString();
  }

  @Override
  public String toString() {
    return instruction.toString() + " error(s)=" + errors;
  }
}
