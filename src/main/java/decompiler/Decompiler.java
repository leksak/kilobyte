package decompiler;

import common.instruction.Instruction;
import common.instruction.MachineCodeDecoder;
import common.instruction.PartiallyValidInstruction;
import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;
import lombok.Getter;
import lombok.Value;
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

@Value public class Decompiler {
  // Decode a numeric representation of an instruction
  public static int decode(String s) {
    return MachineCodeDecoder.decode(s);
  }

  Option singleNumber = Option.builder("n")
        .argName("number(s)")
        .hasArgs()
        .desc("disassemble 32-bit word(s) from stdin")
        .build();
  Option help = Option.builder("h")
        .argName("help")
        .longOpt("help")
        .desc("print this message")
        .build();
  Option headerless = Option.builder()
        .longOpt("header-less")
        .desc("suppress table header")
        .build();

  Options options = new Options();

  private Decompiler() {
    options.addOption(help);
    options.addOption(singleNumber);
    options.addOption(headerless);
  }

  private void outputTable(List<Integer> instructions)
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

  private CommandLine parse(String... args) throws ParseException {
    return parser.parse(options, args);
  }

  private static final HelpFormatter formatter = new HelpFormatter();

  private void printUsage() {
    formatter.printHelp("Decompiler [OPTION] [file|number]...", options);
  }

  public static void main(String[] args) throws IOException {
    Decompiler d = new Decompiler();

    try {
      // parse the command line arguments
      CommandLine line = d.parse(args);

      if (line.hasOption("help")) {
        d.printUsage();
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
