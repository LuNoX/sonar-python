/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.python.checks;

import org.junit.Test;
import org.sonar.python.checks.quickfix.PythonQuickFixVerifier;
import org.sonar.python.checks.utils.PythonCheckVerifier;

import static org.sonar.python.checks.utils.CodeTestUtils.code;

public class MandatoryFunctionReturnTypeHintCheckTest {
  @Test
  public void test() {
    PythonCheckVerifier.verify("src/test/resources/checks/mandatoryFunctionReturnType.py", new MandatoryFunctionReturnTypeHintCheck());
  }

  @Test
  public void quick_fix_none_return_type_for_init() {
    String codeWithIssue = code(
      "class A():",
      "    def __init__(self):",
      "        pass");
    String codeFixed = code(
      "class A():",
      "    def __init__(self) -> None:",
      "        pass");
    MandatoryFunctionReturnTypeHintCheck check = new MandatoryFunctionReturnTypeHintCheck();
    PythonQuickFixVerifier.verify(check, codeWithIssue, codeFixed);
    PythonQuickFixVerifier.verifyQuickFixMessages(check, codeWithIssue, MandatoryFunctionReturnTypeHintCheck.CONSTRUCTOR_MESSAGE);
  }

  @Test
  public void quick_fix_none_return_type_for_init_weird_format() {
    String codeWithIssue = code(
      "class A():",
      "    def __init__(self)   : ",
      "        pass");
    String codeFixed = code(
      "class A():",
      "    def __init__(self) -> None   : ",
      "        pass");
    MandatoryFunctionReturnTypeHintCheck check = new MandatoryFunctionReturnTypeHintCheck();
    PythonQuickFixVerifier.verify(check, codeWithIssue, codeFixed);
    PythonQuickFixVerifier.verifyQuickFixMessages(check, codeWithIssue, MandatoryFunctionReturnTypeHintCheck.CONSTRUCTOR_MESSAGE);
  }

  @Test
  public void quick_fix_none_return_type_for_init_multiline() {
    String codeWithIssue = code(
      "class A():",
      "    def __init__(self,",
      "        alice, bob):",
      "        pass");
    String codeFixed = code(
      "class A():",
      "    def __init__(self,",
      "        alice, bob) -> None:",
      "        pass");
    MandatoryFunctionReturnTypeHintCheck check = new MandatoryFunctionReturnTypeHintCheck();
    PythonQuickFixVerifier.verify(check, codeWithIssue, codeFixed);
    PythonQuickFixVerifier.verifyQuickFixMessages(check, codeWithIssue, MandatoryFunctionReturnTypeHintCheck.CONSTRUCTOR_MESSAGE);
  }
}