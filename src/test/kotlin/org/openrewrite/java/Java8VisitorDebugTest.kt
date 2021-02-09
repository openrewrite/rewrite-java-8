/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java

import org.junit.jupiter.api.extension.ExtendWith
import org.openrewrite.DebugOnly
import org.openrewrite.java.cleanup.*
import org.openrewrite.java.example.GenerateGetterTest
import org.openrewrite.java.format.*
import org.openrewrite.java.search.*
import org.openrewrite.java.tree.TypeTreeTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8AddImportTest : Java8Test, AddImportTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8BlankLinesTest : Java8Test, BlankLinesTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeFieldNameTest : Java8Test, ChangeFieldNameTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeFieldTypeTest : Java8Test, ChangeFieldTypeTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeLiteralTest : Java8Test, ChangeLiteralTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeMethodNameTest : Java8Test, ChangeMethodNameTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeMethodTargetToStaticTest : Java8Test, ChangeMethodTargetToStaticTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeMethodTargetToVariableTest : Java8Test, ChangeMethodTargetToVariableTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ChangeTypeTest : Java8Test, ChangeTypeTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8DeleteMethodArgumentTest : Java8Test, DeleteMethodArgumentTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8DeleteStatementTest : Java8Test, DeleteStatementTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8FindAnnotationsTest : Java8Test, FindAnnotationsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8FindFieldsTest : Java8Test, FindFieldsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8FindInheritedFieldsTest : Java8Test, FindInheritedFieldsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8FindMethodsTest : Java8Test, FindMethodsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8FindTypesTest : Java8Test, FindTypesTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8GenerateGetterTest : Java8Test, GenerateGetterTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8SemanticallyEqualTest : Java8Test, SemanticallyEqualTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ImplementInterfaceTest : Java8Test, ImplementInterfaceTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8JavaTemplateTest : Java8Test, JavaTemplateTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8MinimumViableSpacingTest : Java8Test, MinimumViableSpacingTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8NormalizeFormatTest : Java8Test, NormalizeFormatTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8OrderImportsTest : Java8Test, OrderImportsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8RemoveImportTest : Java8Test, RemoveImportTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8RenameVariableTest : Java8Test, RenameVariableTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8RemoveTrailingWhitespaceTest : Java8Test, RemoveTrailingWhitespaceTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8ReorderMethodArgumentsTest : Java8Test, ReorderMethodArgumentsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8SimplifyBooleanExpressionTest : Java8Test, SimplifyBooleanExpressionTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8SimplifyBooleanReturnTest : Java8Test, SimplifyBooleanReturnTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8SpacesTest : Java8Test, SpacesTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8TabsAndIndentsTest : Java8Test, TabsAndIndentsTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8TypeTreeTest : Java8Test, TypeTreeTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8UnnecessaryParenthesesTest : Java8Test, UnnecessaryParenthesesTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8UnwrapParenthesesTest : Java8Test, UnwrapParenthesesTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8UseStaticImportTest : Java8Test, UseStaticImportTest

@DebugOnly
@ExtendWith(JavaParserResolver::class)
class Java8WrappingAndBracesTest : Java8Test, WrappingAndBracesTest
