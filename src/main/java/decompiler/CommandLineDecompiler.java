package decompiler;

import common.instruction.DecompiledInstruction;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value
public class CommandLineDecompiler implements Decompiler {
  final Options options = new Options()
        .addOption("h", "help", false, "print this message")
        .addOption("n", "number(s)", true, "disassemble 32-bit word(s) from stdin")
        .addOption("headerless", "suppress table header");

  @NonFinal
  boolean headerlessFlag = false;

  String formatString = "%-12s %-3s %-16s %-22s %-18s";
  Object[] header = {
        "Instruction", "Fmt", "Decomposition", "Decomp hex", "Source"};

  CommandLineParser parser = new DefaultParser();

  private CommandLineDecompiler() {
    // Intentionally left empty
  }

  CommandLine parse(String... args) throws ParseException {
    return parser.parse(options, args);
  }

  HelpFormatter formatter = new HelpFormatter();

  void printUsage() {
    formatter.printHelp("MachineCodeDecoder [OPTION] [file|number]...", options);
  }

  public static void main(String[] args) throws IOException {
    CommandLineDecompiler decompiler = new CommandLineDecompiler();
    CommandLine line;
    try {
      line = decompiler.parse(args);

      if (line.hasOption("help")) {
        decompiler.printUsage();
        return;
      }
    } catch (ParseException e) {
      decompiler.printUsage(); // TODO: Test this live.
      System.err.println("Parsing the command-line failed. Error: " + e.getMessage());
      return;
    }

    if (line.hasOption("headerless")) {
      decompiler.headerlessFlag = true;
    }

    List<DecompiledInstruction> decompiledInstructions = new ArrayList<>();
    if (line.hasOption("n")) {
      // The user has passed integers on the command line
      List<String> inputs = Arrays.asList(line.getOptionValues("n"));
      inputs.forEach(i -> decompiledInstructions.add(decompiler.decompile(i)));
      outputTable(decompiledInstructions);
      return;
    }

    String[] argv = line.getArgs();
    List<Long> numbers = new ArrayList<>();

    if (argv.length > 0) {
      // Passed a list of files.
      for (String arg : argv) {
        // Decode the contents of each file
        numbers.addAll(MachineCodeDecoder.decode(new File(arg)));
      }
    } else if (argv.length == 0) {
      // Decompile from standard in
      numbers = MachineCodeDecoder.decode(new InputStreamReader(System.in));
    }
    // Decompile all of the numbers
    numbers.forEach(i -> decompiledInstructions.add(decompiler.decompile(i)));
    outputTable(decompiledInstructions);
  }

  private static void outputTable(List<DecompiledInstruction> decompiledInstructions) {
    for (DecompiledInstruction d : decompiledInstructions) {
      System.out.println(d.toString());
    }
  }
}