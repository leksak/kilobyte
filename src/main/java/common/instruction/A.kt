package common.instruction


class A(val iname: String) {
  init {
    foo.add(this)
  }

  companion object B {
    @JvmField val STATIC = A("hi")
    @JvmField val foo = mutableListOf<A>()


  }

  fun printAllStatics() {
    for (a in foo) {
      print(a.iname)
    }
  }


}