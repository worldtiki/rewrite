package com.netflix.java.refactor.ast

import com.netflix.java.refactor.parse.Parser
import com.netflix.java.refactor.test.AstTest
import org.junit.Test
import org.junit.Assert.assertEquals

abstract class FieldAccessTest(parser: Parser): AstTest(parser) {
    
    @Test
    fun fieldAccess() {
        val b = """
            public class B {
                public String field = "foo";
            }
        """
        
        val a = """
            public class A {
                B b = new B();
                String s = b.field;
            }
        """

        val cu = parse(a, whichDependsOn = b)
        val acc = cu.typeDecls[0].fields().first { it.name.name == "s" }.initializer as Tr.FieldAccess
        assertEquals("field", acc.name.name)
        assertEquals("b", acc.target.print())
    }
}