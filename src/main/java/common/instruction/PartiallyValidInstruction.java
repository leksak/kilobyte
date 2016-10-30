package common.instruction;

import lombok.Value;

@Value
public class PartiallyValidInstruction {
  Instruction instruction;
  String errors;
}
