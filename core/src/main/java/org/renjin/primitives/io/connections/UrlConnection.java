package org.renjin.primitives.io.connections;

import org.renjin.eval.EvalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class UrlConnection extends AbstractConnection {

  private final URL url;
  private InputStream in;  

  public UrlConnection(URL url) {
    super();
    this.url = url;
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    if(spec.forWriting()) {
      throw new EvalException("Cannot open url connection for writing");
    } else {
      if(spec.isText()) {
        getReader();
      } else {
        getInputStream();
      }
    }
  }

  
  @Override
  public InputStream getInputStream() throws IOException {
    if(in == null) {
      this.in = url.openStream();
    }
    return this.in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new EvalException("Cannot open URL for output");
  }

  @Override
  public boolean isOpen() {
    return in != null;
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    in.close();
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {    
  }

  @Override
  public String getClassName() {
    return "url";
  }
}
