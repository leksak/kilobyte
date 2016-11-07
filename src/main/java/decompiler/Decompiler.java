package decompiler;

import common.instruction.Instruction;
import common.instruction.MachineCodeDecoder;
import common.instruction.PartiallyValidInstruction;
import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Decompiler {
  // Decode a numeric representation of an instruction
  public static int decode(String s) {
    return MachineCodeDecoder.decode(s);
  }

  public static Instruction decompile(String s) {
    return null;
  }

  public static Set<Instruction> decompile(File f) {
    return null;
  }

  private static Option singleNumber = Option.builder("n")
        .argName("number(s)")
        .hasArgs()
        .desc("disassemble 32-bit word(s) from stdin")
        .build();
  private static Option help = Option.builder("h")
        .argName("help")
        .longOpt("help")
        .desc("print this message")
        .build();
  private static Option headerless = Option.builder()
        .longOpt("header-less")
        .desc("suppress table header")
        .build();

  private static Options options = new Options();
  private static HelpFormatter formatter = new HelpFormatter();

  static {
    options.addOption(help);
    options.addOption(singleNumber);
    options.addOption(headerless);
  }

  public void outputTable(List<Integer> instructions)
        throws IOException {
    if (!headerlessFlag) {
      System.out.format(formatString + "\n", header);
    }

    instructions.forEach(instruction -> {
      Either<Instruction, PartiallyValidInstruction> i;

      try {
        i = Instruction.from(instruction);
        printInstruction(i);
      } catch (NoSuchInstructionException e) {
        System.err.println(e.getMessage());
      }
    });
  }

  private static void printInstruction(Either<Instruction, PartiallyValidInstruction> maybeInstruction) {
    if (maybeInstruction.isLeft()) {
      Instruction i = maybeInstruction.left().get();
      System.out.println(i);
    } else {
      // Handle partially valid instruction
    }
  }

  private static boolean isNotNull(Object o) {
    return !Objects.isNull(o);
  }

  private boolean headerlessFlag = false;

  private static final String formatString = "%-12s %-3s %-16s %-22s %-18s";
  private static final Object[] header = {
        "Instruction", "Fmt", "Decomposition", "Decomp hex", "Source"};
  private static final CommandLineParser parser = new DefaultParser();


  public static void main(String[] args) throws IOException {
    Decompiler d = new Decompiler();

    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {
        formatter.printHelp("Decompiler [OPTION] [file|number]...", options);

        // TODO: Do we need to check if there are other args?
        // what does POSIX say?
        return;
      }

      List<Integer> numbers = new ArrayList<>();

      if (line.hasOption("header-less")) {
        d.headerlessFlag = true;
      }

      if (line.hasOption("n")) {
        for (String arg : line.getOptionValues("n")) {
          numbers.add(decode(arg));
        }

        d.outputTable(numbers);
        return;
      }

      String[] argv = line.getArgs();

      if (argv.length > 0) {
        for (String arg : argv) {
          BufferedReader br = new BufferedReader(
                new FileReader(arg));

          String l;
          while (isNotNull(l = br.readLine())) {
            if (l.isEmpty()) {
              continue;
            }

            numbers.add(decode(l));
          }
          d.outputTable(numbers);
        }
      } else if (argv.length == 0) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String l;
        while (isNotNull(l = br.readLine())) {
          if (l.isEmpty()) {
            continue;
          }

          int instruction = decode(l);
          try {
            printInstruction(Instruction.from(instruction));
          } catch (NoSuchInstructionException e) {
            System.err.println(e.getMessage());
          }
        }
      }
    } catch (org.apache.commons.cli.ParseException e) {
      System.err.println(e.getMessage());
    }
  }
}
