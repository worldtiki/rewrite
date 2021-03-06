/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.refactor

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.java.assertRefactored

open class ChangeMethodNameTest : JavaParser() {

    private val b: String = """
                class B {
                   public void singleArg(String s) {}
                   public void arrArg(String[] s) {}
                   public void varargArg(String... s) {}
                }
            """.trimIndent()

    @Test
    fun refactorMethodNameForMethodWithSingleArg() {
        val a = """
            class A {
               public void test() {
                   new B().singleArg("boo");
               }
            }
        """.trimIndent()

        val cu = parse(a, b)

        val fixed = cu.refactor()
                .fold(cu.findMethodCalls("B singleArg(String)")) { ChangeMethodName(it, "bar") }
                .fix().fixed

        assertRefactored(fixed, """
            class A {
               public void test() {
                   new B().bar("boo");
               }
            }
        """)
    }

    @Test
    fun refactorMethodNameForMethodWithArrayArg() {
        val a = """
            class A {
               public void test() {
                   new B().arrArg(new String[] {"boo"});
               }
            }
        """.trimIndent()

        val cu = parse(a, b)

        val fixed = cu.refactor()
                .fold(cu.findMethodCalls("B arrArg(String[])")) { ChangeMethodName(it, "bar") }
                .fix().fixed

        assertRefactored(fixed, """
            class A {
               public void test() {
                   new B().bar(new String[] {"boo"});
               }
            }
        """)
    }

    @Test
    fun refactorMethodNameForMethodWithVarargArg() {
        val a = """
            class A {
               public void test() {
                   new B().varargArg("boo", "again");
               }
            }
        """.trimIndent()

        val cu = parse(a, b)

        val fixed = cu.refactor()
                .fold(cu.findMethodCalls("B varargArg(String...)")) { ChangeMethodName(it, "bar") }
                .fix().fixed

        assertRefactored(fixed, """
            class A {
               public void test() {
                   new B().bar("boo", "again");
               }
            }
        """)
    }

    @Test
    fun refactorMethodNameWhenMatchingAgainstMethodWithNameThatIsAnAspectjToken() {
        val b = """
            class B {
               public void error() {}
               public void foo() {}
            }
        """.trimIndent()

        val a = """
            class A {
               public void test() {
                   new B().error();
               }
            }
        """.trimIndent()

        val cu = parse(a, b)
        val fixed = cu.refactor()
                .fold(cu.findMethodCalls("B error()")) { ChangeMethodName(it, "foo") }
                .fix().fixed

        assertRefactored(fixed, """
            class A {
               public void test() {
                   new B().foo();
               }
            }
        """)
    }
}
