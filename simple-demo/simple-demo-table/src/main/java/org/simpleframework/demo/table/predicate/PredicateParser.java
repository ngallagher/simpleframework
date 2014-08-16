package org.simpleframework.demo.table.predicate;

import static org.simpleframework.demo.table.predicate.Type.ARGUMENT;
import static org.simpleframework.demo.table.predicate.Type.NUMBER;
import static org.simpleframework.demo.table.predicate.Type.STRING;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This parser is used to parse a statement containing various text expressions
 * so that it can be evaluated as a single predicate. In order to separate
 * expressions braces can be used. Also, braces can be used to specify the order
 * of evaluation of the expressions.
 * 
 * @author Niall Gallagher
 */
public class PredicateParser extends Parser implements Predicate {

   private final AtomicReference<Predicate> result;
   private final Stack<Predicate> predicates;
   private final Stack<Condition> conditions;
   private final QuoteStack quotes;
   private final BraceStack braces;
   private final Token original;
   private final Token condition;
   private final Token operator;
   private final Token name;
   private final Token value;

   public PredicateParser() {
      this.result = new AtomicReference<Predicate>();
      this.predicates = new Stack<Predicate>();
      this.conditions = new Stack<Condition>();
      this.quotes = new QuoteStack();
      this.braces = new BraceStack();
      this.condition = new Token();
      this.operator = new Token();
      this.original = new Token();
      this.name = new Token();
      this.value = new Token();
   }

   public PredicateParser(String text) {
      this();
      parse(text);
   }

   @Override
   public boolean accept(Argument argument) {
      Predicate predicate = create();

      if (predicate != null) {
         return predicate.accept(argument);
      }
      return false;
   }

   private Predicate create() {
      Predicate predicate = result.get();

      if (predicate == null) {
         build();
      }
      return result.get();
   }

   private void build() {
      if (predicates.isEmpty()) {
         throw new IllegalStateException("Expression '" + original + "' contained no known predicates");
      }
      while (!conditions.isEmpty()) {
         combine();
      }
      Predicate predicate = predicates.pop();

      if (!predicates.isEmpty()) {
         throw new IllegalStateException("Expression is not balanced correctly '" + predicates);
      }
      result.set(predicate);
   }

   @Override
   protected void init() {
      predicates.clear();
      conditions.clear();
      quotes.clear();
      braces.clear();
      result.set(null);
   }

   @Override
   protected void parse() {
      pack();
      original();
      expression();
   }

   private void original() {
      original.off = 0;
      original.len = count;
   }

   private void pack() {
      int pos = 0;

      while (off < count) {
         if (quote(source[off])) {
            char open = source[off];

            while (off < count) {
               source[pos++] = source[off++];

               if (source[off] == open) {
                  source[pos++] = source[off++];
                  break;
               }
            }
         } else if (!space(source[off])) {
            source[pos++] = source[off++];
         } else {
            off++;
         }
      }
      count = pos;
      off = 0;
   }

   private void expression() {
      int statements = 0;
      int conditions = 0;

      while (off < count) { // ((x == y) && (x != x))
         char next = source[off];

         if (condition(next)) {
            conditions++;
            condition();
            commit();
         } else if (braces.open(next)) {
            off++;
            expression(); // ([(]x == y)

            while (conditions > statements) { // !(x = y)
               combine();
               conditions--;
            }
         } else if (braces.close(next)) {
            off++;
            break; // ((x == y[)]
         } else {
            statements++;
            statement();

            if (statements > 1) { // x == y && [i < j]
               combine();
               statements = 0;
            }
         }
      }
   }

   private void commit() { // (x == y && z == t) || (y = j)
      String token = condition.toString();
      Condition result = Condition.resolveCondition(token);

      conditions.push(result);
   }

   private void combine() {
      Condition condition = conditions.pop();
      Predicate predicate = condition.combine(predicates);

      predicates.push(predicate);
   }

   private void statement() {
      if (!skip("*")) {
         name();
         operator();
         value();
         insert();
      } else {
         wild();
      }
   }

   private void wild() {
      Any wild = new Any();
      predicates.push(wild);
   }

   private void insert() {
      String left = name.toString();
      String right = value.toString();
      String op = operator.toString();

      insert(left, right, op, value.type);
   }

   public void insert(String left, String right, String operator, Type type) {
      Evaluation evaluation = new Evaluation(left, right, operator, type);
      predicates.push(evaluation);
   }

   private void name() {
      name.off = off;
      name.len = 0;

      if (off < count) {
         token(name);
      }
   }

   private void value() {
      value.off = off;
      value.len = 0;

      if (off < count) {
         token(value);
      }
   }

   private void token(Token token) {
      System.err.println(new String(source, off, count-off));
      if (quotes.open(source[off])) {
         token.type = STRING;         
         token.off++;
         off++;

         while (off < count) {
            if (quotes.close(source[off])) {               
               off++;
               break;
            }
            token.len++;
            off++;
         }
      } else {
         char next = source[off];

         if (digit(next) || minus(next)) {
            token.type = NUMBER;
         } else {
            token.type = ARGUMENT;
         }
         while (off < count) {
            if (terminal(source[off])) {
               break;
            }
            token.len++;
            off++;
         }
      }
   }

   private void operator() {
      operator.off = off;
      operator.len = 0;

      while (off < count) {
         char next = source[off];

         if (!operator(next)) {
            break;
         }
         operator.len++;
         off++;
      }
   }

   private void condition() {
      condition.off = off;
      condition.len = 0;

      while (off < count) {
         char next = source[off];

         if (!condition(next)) {
            break;
         }
         condition.len++;
         off++;
      }
   }

   private boolean minus(char ch) {
      return ch == '-';
   }

   private boolean condition(char ch) {
      switch (ch) {
      case '!': case '&':
      case '|':
         return true;
      }
      return false;
   }

   private boolean operator(char ch) {
      switch (ch) {
      case '!': case '<':
      case '>': case '=':
      case '~':
         return true;
      }
      return false;
   }

   private boolean terminal(char ch) {
      switch (ch) {
      case '&': case '|':
      case '!': case '<':
      case '>': case '=':
      case '(': case ')':
      case '{': case '}':
      case '[': case ']':
         return true;
      }
      return false;
   }

   @Override
   public String toString() {
      return create().toString();
   }

   private class Token {

      public Type type;
      public int off;
      public int len;

      public Token() {
         this(0, 0, ARGUMENT);
      }

      public Token(int off, int len, Type type) {
         this.type = type;
         this.off = off;
         this.len = len;
      }

      public void clear() {
         type = null;
         len = 0;
      }

      public String toString() {
         return new String(source, off, len);
      }
   }

   private class QuoteStack {

      private char[] stack;
      private int count;

      public QuoteStack() {
         this.stack = new char[16];
      }

      public boolean open(char ch) {
         if (ch == '"') {
            stack[count++] = '"';
            return true;
         }
         if (ch == '\'') {
            stack[count++] = '\'';
            return true;
         }
         return false;
      }

      public boolean close(char ch) {
         if (count > 0) {
            if (stack[count - 1] == ch) {
               count--;
               return true;
            }
         }
         return false;
      }

      public boolean isOpen() {
         return count > 0;
      }

      public boolean isEmpty() {
         return count == 0;
      }

      public void clear() {
         stack[0] = 0;
         count = 0;
      }
   }

   private class BraceStack {

      private char[] stack;
      private int count;

      public BraceStack() {
         this.stack = new char[16];
      }

      public boolean open(char ch) {
         if (ch == '[') {
            stack[count++] = ']';
            return true;
         }
         if (ch == '(') {
            stack[count++] = ')';
            return true;
         }
         if (ch == '{') {
            stack[count++] = '}';
            return true;
         }
         return false;
      }

      public boolean close(char ch) {
         if (count > 0) {
            if (stack[count - 1] == ch) {
               count--;
               return true;
            }
         }
         return false;
      }

      public boolean isCurrent() {
         return count == 1;
      }

      public boolean isEmpty() {
         return count == 0;
      }

      public void clear() {
         stack[0] = 0;
         count = 0;
      }
   }

}
