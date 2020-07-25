package io.liftwizard.model.reladomo.operation.compiler;

import java.util.regex.Pattern;

import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationLexer;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser;
import io.liftwizard.model.reladomo.operation.ReladomoOperationVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class ReladomoOperationCompiler
{
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r?\\n");

    public Operation compile(RelatedFinder finder, String sourceCodeText)
    {
        String[]            lines      = NEWLINE_PATTERN.split(sourceCodeText);
        CodePointCharStream charStream = CharStreams.fromString(sourceCodeText);

        var lexer         = new ReladomoOperationLexer(charStream);
        var errorListener = new ThrowingErrorListener(lines);
        var tokenStream   = new CommonTokenStream(lexer);
        var parser        = new ReladomoOperationParser(tokenStream);

        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ReladomoOperationVisitor<Operation> visitor = new ReladomoOperationBuilderVisitor(finder, tokenStream);

        return parser.compilationUnit().accept(visitor);
    }
}
