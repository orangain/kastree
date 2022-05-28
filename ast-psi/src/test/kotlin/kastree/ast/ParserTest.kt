package kastree.ast

import kastree.ast.psi.ConverterWithExtras
import kastree.ast.psi.Parser
import org.junit.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun testDeclaration() {
        assertParsedAs("""
            val x = ""
        """.trimIndent(), """
            Node.File
              Node.Decl.Property
                Node.Decl.Property.Var
                Node.Expr.StringTmpl
        """.trimIndent())
    }

    @Test
    fun testInlineComment() {
        assertParsedAs("""
            val x = "" // x is empty
        """.trimIndent(), """
            Node.File
              Node.Decl.Property
                Node.Decl.Property.Var
                Node.Expr.StringTmpl
              AFTER: Node.Extra.Comment
        """.trimIndent())
    }

    @Test
    fun testLineComment() {
        assertParsedAs("""
            // x is empty
            val x = ""
        """.trimIndent(), """
            Node.File
              BEFORE: Node.Extra.Comment
              Node.Decl.Property
                Node.Decl.Property.Var
                Node.Expr.StringTmpl
        """.trimIndent())
    }

    @Test
    fun testFunctionBlock() {
        assertParsedAs("""
            fun setup() {
                // do something
                val x = ""
            }
        """.trimIndent(), """
            Node.File
              Node.Decl.Func
                Node.Decl.Func.Body.Block
                  Node.Expr.Block
                    BEFORE: Node.Extra.Comment
                    Node.Stmt.Decl
                      Node.Decl.Property
                        Node.Decl.Property.Var
                        Node.Expr.StringTmpl
        """.trimIndent())
    }

    @Test
    fun testFunctionBlockHavingOnlyComment() {
        assertParsedAs("""
            fun setup() {
                // do something
            }
        """.trimIndent(), """
            Node.File
              Node.Decl.Func
                Node.Decl.Func.Body.Block
                  WITHIN: Node.Extra.Comment
                  Node.Expr.Block
        """.trimIndent())
    }

    @Test
    fun testFunctionExpression() {
        assertParsedAs("""
            fun calc() = 1 + 2
        """.trimIndent(), """
            Node.File
              Node.Decl.Func
                Node.Decl.Func.Body.Expr
                  Node.Expr.BinaryOp
                    Node.Expr.Const
                    Node.Expr.BinaryOp.Oper.Token
                    Node.Expr.Const
        """.trimIndent()
        )
    }

    @Test
    fun testLambdaExpression() {
        assertParsedAs("""
            fun setup() {
                run {
                    // do something
                    val x = ""
                }
            }
        """.trimIndent(), """
            Node.File
              Node.Decl.Func
                Node.Decl.Func.Body.Block
                  Node.Expr.Block
                    Node.Stmt.Expr
                      Node.Expr.Call
                        Node.Expr.Name
                        Node.Expr.Call.TrailLambda
                          Node.Expr.Lambda
                            Node.Expr.Lambda.Body
                              BEFORE: Node.Extra.Comment
                              Node.Stmt.Decl
                                Node.Decl.Property
                                  Node.Decl.Property.Var
                                  Node.Expr.StringTmpl
        """.trimIndent())
    }

    @Test
    fun testLambdaExpressionHavingOnlyComment() {
        assertParsedAs("""
            fun setup() {
                run {
                    // do something
                }
            }
        """.trimIndent(), """
            Node.File
              Node.Decl.Func
                Node.Decl.Func.Body.Block
                  Node.Expr.Block
                    Node.Stmt.Expr
                      Node.Expr.Call
                        Node.Expr.Name
                        Node.Expr.Call.TrailLambda
                          Node.Expr.Lambda
                            BEFORE: Node.Extra.Comment
                            Node.Expr.Lambda.Body
        """.trimIndent())
    }

    private fun assertParsedAs(code: String, expectedDump: String) {
        val converter = ConverterWithExtras()
        val node = Parser(converter).parseFile(code)
        val actualDump = Dumper.dump(node, converter, verbose = false)
        assertEquals(expectedDump.trim(), actualDump.trim())
    }

}