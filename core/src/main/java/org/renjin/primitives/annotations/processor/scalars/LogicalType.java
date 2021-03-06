package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;

public class LogicalType extends ScalarType {


  @Override
  public Class getScalarType() {
    return Logical.class;
  }

  @Override
  public String getConversionMethod() {
    return "convertToLogical";
  }

  @Override
  public String getAccessorMethod() {
    return "isElementAsLogical";
  }

  @Override
  public Class getStorageType() {
    return Integer.TYPE;
  }
  
  @Override
  public String getNALiteral() {
    return "IntVector.NA";
  }

  @Override
  public Class getVectorType() {
    return LogicalVector.class;
  }

  @Override
  public String convertToStorageTypeExpression(String valueExpression) {
    return valueExpression + ".getInternalValue()";
  }

  @Override
  public Class<LogicalArrayVector.Builder> getBuilderClass() {
    return LogicalArrayVector.Builder.class;
  }


}
