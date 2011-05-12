/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base.file;

import com.google.common.collect.Lists;
import r.base.regex.ExtendedRE;
import r.base.regex.RE;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;

import java.io.IOException;
import java.util.List;

public class Files {


  @Primitive("path.expand")
  public static String pathExpand(String path) {
    if(path.startsWith("~/")) {
      return java.lang.System.getProperty("user.home") + path.substring(2);
    } else {
      return path;
    }
  }

  @Primitive("file.info")
  public static ListVector fileInfo(@Current Context context, StringVector paths)  {

    DoubleVector.Builder size = new DoubleVector.Builder();
    LogicalVector.Builder isdir = new LogicalVector.Builder();
    IntVector.Builder mode = (IntVector.Builder) new IntVector.Builder()
        .setAttribute(Attributes.CLASS, new StringVector("octmode"));
    DoubleVector.Builder mtime = new DoubleVector.Builder();
    StringVector.Builder exe = new StringVector.Builder();

    for(String path : paths) {
      FileInfo file = FileSystem.getFileInfo(path);
      if(file.exists()) {
        if(file.isFile()) {
          size.add((int) file.length());
        } else {
          size.add(0);
        }
        isdir.add(file.isDirectory());
        mode.add(file.getMode());
        mtime.add(file.lastModified());
        exe.add(file.getName().endsWith(".exe") ? "yes" : "no");
      } else {
        size.add(IntVector.NA);
        isdir.add(IntVector.NA);
        mode.add(IntVector.NA);
        mtime.add(DoubleVector.NA);
        exe.add(StringVector.NA);

      }
    }

    ListVector list = ListVector.newBuilder()
        .add("size", size)
        .add("isdir", isdir)
        .add("mode", mode)
        .add("mtime", mtime)
        .add("ctime", mtime)
        .add("atime", mtime)
        .add("exe", exe)
        .build();
    return list;
  }


  @Primitive("file.exists")
  public static boolean fileExists(@Current Context context, String path) {
    return FileSystem.getFileInfo(path).exists();
  }

  /**
   *
   * @param path
   * @return  removes all of the path up to and including the last path separator (if any).
   */
  public static String basename(String path) {
    for(int i=path.length()-1;i>=0;--i) {
      if(path.charAt(i) == '\\' || path.charAt(i) == '/') {
        return path.substring(i+1);
      }
    }
    return path;
  }

  /**
   * Function to do wildcard expansion (also known as ‘globbing’) on file paths.
   *
   * @param paths character vector of patterns for relative or absolute filepaths.
   *  Missing values will be ignored.
   * @param markDirectories  should matches to directories from patterns that do not
   * already end in / or \ have a slash appended?
   * May not be supported on all platforms.
   * @return
   */
  @Primitive("Sys.glob")
  public static StringVector glob(@Current Context context, StringVector paths, boolean markDirectories) {

    List<String> matching = Lists.newArrayList();
    for(String path : paths) {
      if(path != null) {
        if(path.indexOf('*')==-1) {
          matching.add(path);
        } else {
          matching.addAll(FileScanner.scan(context, path, markDirectories));
        }
      }
    }
    return new StringVector(matching);
  }

  /**
   *
   * @param path
   * @return  the part of the path up to but excluding the last path separator, or "." if there is no path separator.
   */
  public static String dirname(String path) {
    for(int i=path.length()-1;i>=0;--i) {
      if(path.charAt(i) == '\\' || path.charAt(i) == '/') {
        return path.substring(0, i);
      }
    }
    return ".";
  }

  /**
   *
   * @param paths  a character vector of full path names; the default corresponds to the working directory getwd(). Missing values will be ignored.
   * @param pattern an optional regular expression. Only file names which match the regular expression will be returned.
   * @param allFiles  If FALSE, only the names of visible files are returned. If TRUE, all file names will be returned.
   * @param fullNames If TRUE, the directory path is prepended to the file names. If FALSE, only the file names are returned.
   * @param recursive Should the listing recurse into directories?
   * @param ignoreCase Should pattern-matching be case-insensitive?
   *
   * If a path does not exist or is not a directory or is unreadable it is skipped, with a warning.
   * The files are sorted in alphabetical order, on the full path if full.names = TRUE. Directories are included only if recursive = FALSE.
   *
   * @return
   */
  @Primitive("list.files")
  public static StringVector listFiles(@Current final Context context,
                                       final StringVector paths,
                                       final String pattern,
                                       final boolean allFiles,
                                       final boolean fullNames,
                                       boolean recursive,
                                       final boolean ignoreCase) throws IOException {

    return new Object() {

      private final StringVector.Builder result = new StringVector.Builder();
      private final RE filter = pattern == null ? null : new ExtendedRE(pattern).ignoreCase(ignoreCase);

      public StringVector list() throws IOException {
        for(String path : paths) {
          FileInfo folder = FileSystem.getFileInfo(path);
          if(folder.isDirectory()) {
            if(allFiles) {
              add(folder, ".");
              add(folder, "..");
            }
            for(FileInfo child : folder.listFiles()) {
              if(filter(child)) {
                add(child);
              }
            }
          }
        }
        return result.build();
      }

      void add(FileInfo file) {
        if(fullNames) {
          result.add(file.getPath());
        } else {
          result.add(file.getName());
        }
      }

      void add(FileInfo folder, String name)  {
        if(fullNames) {
          result.add(folder.getChild(name).getPath());
        } else {
          result.add(name);
        }
      }

      boolean filter(FileInfo child)  {
        if(!allFiles && isHidden(child)) {
          return false;
        }
        if(filter!=null && !filter.match(child.getName())) {
          return false;
        }
        return true;
      }

      private boolean isHidden(FileInfo file)  {
        return file.isHidden() || file.getName().startsWith(".");
      }
    }.list();
  }
}
