package decompiler.frontends;

import common.instruction.Instruction;
import common.instruction.PartiallyValidInstruction;
import io.atlassian.fugue.Either;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.cli.*;

import java.util.Collection;

@Value
public class CommandLineDecompiler implements DecompilerFrontEnd {
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

  private CommandLineDecompiler() {
    options.addOption(help);
    options.addOption(singleNumber);
    options.addOption(headerless);
  }

  @NonFinal
  boolean headerlessFlag = false;

  String formatString = "%-12s %-3s %-16s %-22s %-18s";
  Object[] header = { "Instruction", "Fmt", "Decomposition", "Decomp hex", "Source"};
  CommandLineParser parser = new DefaultParser();

  private CommandLine parse(String... args) throws ParseException {
    return parser.parse(options, args);
  }

  HelpFormatter formatter = new HelpFormatter();

  private String printUsage() {
    formatter.printHelp("Decompiler [OPTION] [file|number]...", options);
  }

  @Override
  public void display(Collection<Either<Instruction, PartiallyValidInstruction>> instructions) {

  }

  @Override
  public String usage() {
    return null;
  }

  @Override
  public void run(String[] args) {

  }
}
