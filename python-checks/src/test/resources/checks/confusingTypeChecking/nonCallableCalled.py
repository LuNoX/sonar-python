from typing import Set, FrozenSet

def foo(param1: int, param2: Set[int], param3: FrozenSet[int]):
  param1() # Noncompliant {{Fix this call; Previous type checks suggest that "param1" has type int and it is not callable.}}
  x = 42
  x() # OK, raised by S5756

  param2() # Noncompliant
  s = set()
  s() # OK, raised by S5756

  param3() # Noncompliant
  fs = frozenset()
  fs() # OK, raised by S5756

def derived(x: int):
  x.conjugate()() # FN
  y = x or 'str'
  y() # Noncompliant
  z = x + 42
  z() # Noncompliant