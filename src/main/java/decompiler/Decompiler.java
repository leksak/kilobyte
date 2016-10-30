package decompiler;

import common.instruction.Format;
import common.instruction.Instruction;
import common.instruction.InstructionPrototype;

import java.io.File;
import java.util.Set;

public class Decompiler {
  public static int decode(String s) {
    return 0;
  }

  public static Instruction decompile(String s) {
    return null;
  }

  public static Set<Instruction> decompile(File f) {
    return null;
  }

  public static void main(String[] args) {
    new InstructionPrototype("Hej", 0, "hej", 0, "hej", Format.R, null, null, null, null);

    new InstructionPrototype("Hej", 0, "hej", 0, "hej", Format.R, null, null, null, null);
  }
}
