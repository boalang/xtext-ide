/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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

package edu.iastate.cs.boa.ui.errorValidation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.listeners.BoaErrorListener;
import boa.compiler.listeners.ParserErrorListener;
import boa.compiler.ast.Start;
import boa.compiler.visitors.TypeCheckingVisitor;

import boa.parser.BoaLexer;
import boa.parser.BoaParser;
import boa.parser.BoaParser.StartContext;
/**
 * @author rdyer
 */
public class BoaErrorValidation {
	protected static boolean DEBUG = true;
	static int i = 1;
	public boolean hasError = false;
	BoaErrorListener parseErrorListener = null;
	BoaLexer lexer = null;
	String error[] = new String[3];

	public String[] error(final String kind, final TokenSource tokens, final Object offendingSymbol, final int line, final int charPositionInLine, final int length, final String msg, final Exception e) {
		try {
			//underlineError(tokens, (Token)offendingSymbol, line, charPositionInLine, length);
			
			error[0] = Integer.toString(line); 
			error[1] = Integer.toString(charPositionInLine);
			error[2] = msg;
		}
		catch(Exception exception){
			
		}
		return error;		
	}
	
	private void underlineError(final TokenSource tokens, final Token offendingToken, final int line, final int charPositionInLine, final int length) {
		final String input = tokens.getInputStream().toString() + "\n ";
		final String[] lines = input.split("\n");
		final String errorLine = lines[line - 1];
		System.err.println(errorLine.replaceAll("\t", "    "));

		int stop = Math.min(charPositionInLine, errorLine.length());
		for (int i = 0; i < stop; i++)
			if (errorLine.charAt(i) == '\t')
				System.err.print("    ");
			else
				System.err.print(" ");

		int stop2 = Math.min(stop + length, errorLine.length());
		for (int i = stop; i < stop2; i++)
			if (errorLine.charAt(i) == '\t')
				System.err.print("^^^^");
			else
				System.err.print("^");

		System.err.println();
	}

	//
	// lexing
	//

	protected CommonTokenStream lex(final String input) throws IOException {
		return lex(input, new int[0], new String[0]);
	}

	protected CommonTokenStream lex(final String input, final int[] ids, final String[] strings) throws IOException {
		return lex(input, ids, strings, new String[0]);
	}

	protected CommonTokenStream lex(final String input, final int[] ids, final String[] strings, final String[] errors) throws IOException {
		lexer = new BoaLexer(new ANTLRInputStream(new StringReader(input)));
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener () {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				error("lexer", (BoaLexer)recognizer, offendingSymbol, line, charPositionInLine, 1, msg, e);
			}
		});

		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		return tokens;
	}


	//
	// parsing
	//

	protected StartContext parse(final String input) throws IOException {
		if(i==1){
			SymbolTable.initialize(new ArrayList<URL>());
			i = i + 1;
		}
		return parse(input, new String[0]);
	}

	protected StartContext parse(final String input, final String[] errors) throws IOException {
		
		final CommonTokenStream tokens = lex(input);
		final BoaParser parser = new BoaParser(tokens);
		final List<String> foundErr = new ArrayList<String>();
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
				throw new ParseCancellationException(e);
			}
		});
		parseErrorListener = new ParserErrorListener();
		parser.setBuildParseTree(false);
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		StartContext p;
		try {
			p = parser.start();
		} catch (final Exception e) {
			// fall-back to LL mode parsing if SLL fails
			tokens.reset();
			parser.reset();

			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener () {
				@Override
				public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
					foundErr.add(line + "," + charPositionInLine + ": " + msg);
				}
			});
			parser.getInterpreter().setPredictionMode(PredictionMode.LL);

			p = parser.start();
		}

		return p;
	}


	//
	// type checking
	//

	public String[] typecheck(final String input) throws IOException {
		return typecheck(input, null);
	}

	protected String[] typecheck(final String input, final String error) throws IOException {
		final Start p = parse(input).ast;
		String[] errorOutput = new String[3];
		try {
			new TypeCheckingVisitor().start(p, new SymbolTable());
		} catch (final TypeCheckException e) {
			if (error == null){
				errorOutput = error("typecheck", lexer, null, e.n.beginLine, 
					e.n.beginColumn, e.n2.endColumn - e.n.beginColumn + 1, e.getMessage(), e);
			}
		}
		catch (Exception e1) {
		}
		return errorOutput;
	}
	

	public String load(final String fileName) throws IOException {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
			final byte[] bytes = new byte[(int) new File(fileName).length()];
			in.read(bytes);
			return new String(bytes);
		} finally {
			if (in != null)
				in.close();
		}
	}
	
}
