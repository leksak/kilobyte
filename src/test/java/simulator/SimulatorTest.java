package simulator;

import static common.hardware.Register.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SimulatorTest {
  void testAddExecutionPath() {
    Simulator s = new Simulator();
    $t2.value = 1;
    $t3.value = 2;

    s.execute("add $t1, $t2, $t3");
    assertThat($t1, is(equalTo(3)));
  }
}
