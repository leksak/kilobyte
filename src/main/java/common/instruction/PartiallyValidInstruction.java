package common.instruction;

public class PartiallyValidInstruction {
  public final Instruction instruction;
  public final String errors;


  public PartiallyValidInstruction(Instruction instruction, String errors) {
    this.instruction = instruction;
    this.errors = errors;
  }
}
