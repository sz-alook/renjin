package r.base;

import org.apache.commons.math.complex.Complex;

import r.jvmi.wrapper.generator.scalars.ComplexType;
import r.lang.AtomicVector;
import r.lang.ComplexVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.Null;
import r.lang.SEXP;

public class ComplexGroup {

  public static DoubleVector Mod(DoubleVector x) {
    return x;
  }
 
  public static IntVector Mod(IntVector x) {
    return x;
  }

  public static Null Mod(Null x) {
    return x;
  }
  
  public static double Mod(Complex z){
    return z.abs();
  }
  
  public static double Arg(Complex z){
    return z.getArgument();
  }
  
  public static ComplexVector complex(int lengthOut, AtomicVector realVector, AtomicVector imaginaryVector){
    if(realVector.length() > lengthOut) {
      lengthOut = realVector.length();
    }
    if(imaginaryVector.length() > lengthOut) {
      lengthOut = imaginaryVector.length();
    }
    
    ComplexVector.Builder result = new ComplexVector.Builder(0, lengthOut); 
    for(int i=0; i!=lengthOut;++i) {
      double real = 0;
      double imaginary = 0;
      if(realVector.length() > 0) {
        real = realVector.getElementAsDouble(i % realVector.length());
      }
      if(imaginaryVector.length() > 0) {
        imaginary = imaginaryVector.getElementAsDouble(i % imaginaryVector.length());
      }
      result.add(new Complex(real, imaginary));
    }
    return result.build();
  }
  
  public static double Re(Complex z){
    return z.getReal();
  }
  
  public static double Im(Complex z){
    return z.getImaginary();
  }
  
  
  public static Complex Conj(Complex z){
    return new Complex(z.getReal(),-1*z.getImaginary());
  }
  
}
