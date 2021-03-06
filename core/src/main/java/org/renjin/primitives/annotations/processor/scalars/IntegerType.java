package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.primitives.annotations.CastStyle;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;

public class IntegerType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Integer.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToInt";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsInt";
  }

  @Override
  public Class getVectorType() {
    return IntVector.class;
  }

  @Override
  public String getNALiteral() {
    return "IntVector.NA";
  }

  @Override
  public Class<IntArrayVector.Builder> getBuilderClass() {
    return IntArrayVector.Builder.class;
  }

  @Override
  public String testExpr(String expr, CastStyle castStyle) {
    // R language generally seems to allow implicit conversion of doubles
    // to ints
    if(castStyle == CastStyle.IMPLICIT) {
      return "(" + expr + " instanceof IntVector || " + expr + " instanceof DoubleVector || " + 
        expr + " instanceof LogicalVector)";
    } else {
      return "(" + expr + " instanceof IntVector)";
    }
  } 
}
