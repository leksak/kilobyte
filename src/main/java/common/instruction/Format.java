package common.instruction;

public enum Format {
  R(6, 5, 5, 5, 5, 6), // Use to compose decomposed representation
  I(6, 5, 5, 16),
  J(6, 26);

  public final int[] lengths;
  public final int noOfFields;

  Format(int... lengths) {
    this.lengths = lengths;
    noOfFields = lengths.length;
  }
}
