package org.renjin.packaging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.renjin.RVersion;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Bootstraps the packaging of the default R packages
 *
 */
public class Bootstrapper {

  private File srcRoot = new File("src/library");
  private File destRoot = new File("target/classes/org/renjin/library");
  
  public Bootstrapper() throws IOException, ScriptException {

    try {
      
      // this is the minimum we need to run a proper context
      System.out.println("Building base package database...");
      installPackageSources("base");
      copyProfile();
      
      // now we have enough to create the base package database
      createBasePackageDatabase();
      
      // next up we need the tools sources in place
      bootstrapTools();
    
      // now we can compile the rest of the packages
      for(String packageName : new String[] 
          { "datasets", "utils", "grDevices", "graphics", "stats", "splines" }) {
        buildPackage(packageName);
      }
    } catch(Exception e) {
      System.out.println("R language package build failed, expect subsequent test failures...");
      e.printStackTrace();
    }
    
  }
  
  private void copyProfile() throws IOException {
    File profileScript = file(destRoot, "base", "R", "Rprofile");
    Files.copy(file(srcRoot, "profile", "Common.R"), profileScript);
    Files.append(
        Files.toString(
             file(srcRoot, "profile", "Renjin.R"), Charsets.UTF_8),
         profileScript, Charsets.UTF_8);
  } 
  
  private void installPackageSources(String packageName) throws IOException {
    List<File> sources = PackagingUtils.findSourceFiles(
        file(srcRoot, packageName));
    
    file(destRoot, packageName).mkdirs();
    
    copyDescription(packageName);
    
    File namespaceFile = file(srcRoot, packageName, "NAMESPACE");
    if(namespaceFile.exists()) {
      Files.copy(namespaceFile, file(destRoot, packageName, "NAMESPACE"));
    }
    
    PackagingUtils.concatSources(sources, destRoot, packageName);
  }

  protected void copyDescription(String packageName) throws IOException {
    File src = file(srcRoot, packageName, "DESCRIPTION");
    File dest = file(destRoot, packageName, "DESCRIPTION");
    Files.copy(src, dest);
    Files.append(String.format("Build: R %s; ; %s; unix", RVersion.STRING, new Date().toString()),
        dest, Charsets.UTF_8);
  }
  
  private void createBasePackageDatabase() throws IOException, ScriptException {
    evalWithoutDefaultPackages(file(srcRoot, "base", "makebasedb.R"));
    
    Files.copy(file(srcRoot, "base", "baseloader.R"), 
          file(destRoot, "base", "R", "base"));
  }

  private void evalWithoutDefaultPackages(File file) throws IOException,
      ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.withOptions().withNoDefaultPackages().get();
    engine.eval(file);
  }

  private void eval(String source) throws ScriptException, IOException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.withOptions().withNoDefaultPackages().get();
    engine.eval(source); 
    engine.printWarnings();
  }
  
  private void bootstrapTools() throws IOException, ScriptException {
    System.out.println("Building tools package...");

    installPackageSources("tools");
    eval(String.format("tools:::.install_package_description('%s', '%s')",
        file(srcRoot, "tools").getAbsolutePath(),
        file(destRoot, "tools").getAbsolutePath()));
    
    String lazyLoadScript = Files.toString(file(srcRoot, "tools", "R", "makeLazyLoad.R"), Charsets.UTF_8);
    lazyLoadScript += "\n" + "makeLazyLoading('tools')\n";
    
    eval(lazyLoadScript);
  }


  private void buildPackage(String packageName) throws IOException, ScriptException {
    System.out.println(String.format("Building package '%s'...", packageName));

    installPackageSources(packageName);
    eval(
      String.format("tools:::.install_package_description('%s', '%s')\n" +
                    "tools:::makeLazyLoading('%s')\n",
        file(srcRoot, packageName).getAbsolutePath(),
        file(destRoot, packageName).getAbsolutePath(),
        packageName));
  }
  
  private static File file(File parent, String... children) {
    File file = parent;
    for(String child : children) {
      file = new File(file, child);
    }
    return file;
  }

  public static void main(String[] args) throws IOException, ScriptException {
      new Bootstrapper();
  }
  
}
