/*
 * Copyright (c) 1998, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */


package com.sun.tools.example.debug.tty;

import com.sun.jdi.*;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.*;
import java.io.*;


public class Env {

    private static SourceMapper sourceMapper = new SourceMapper("");
    private static List<String> excludes;

    private static final int SOURCE_CACHE_SIZE = 5;
    private static List<SourceCode> sourceCache = new LinkedList<SourceCode>();

    private static HashMap<String, Value> savedValues = new HashMap<String, Value>();
    private static Method atExitMethod;

    static void setSourcePath(String srcPath) {
        sourceMapper = new SourceMapper(srcPath);
        sourceCache.clear();
    }

    static void setSourcePath(List<String> srcList) {
        sourceMapper = new SourceMapper(srcList);
        sourceCache.clear();
    }

    static String getSourcePath() {
        return sourceMapper.getSourcePath();
    }

    private static List<String> excludes() {
        if (excludes == null) {
            setExcludes("java.*, javax.*, sun.*, com.sun.*, jdk.*");
        }
        return excludes;
    }

    static String excludesString() {
        StringBuilder sb = new StringBuilder();
        for (String pattern : excludes()) {
            sb.append(pattern);
            sb.append(",");
        }
        return sb.toString();
    }

    static void addExcludes(StepRequest request) {
        for (String pattern : excludes()) {
            request.addClassExclusionFilter(pattern);
        }
    }

    static void addExcludes(MethodEntryRequest request) {
        for (String pattern : excludes()) {
            request.addClassExclusionFilter(pattern);
        }
    }

    static void addExcludes(MethodExitRequest request) {
        for (String pattern : excludes()) {
            request.addClassExclusionFilter(pattern);
        }
    }

    static void setExcludes(String excludeString) {
        StringTokenizer t = new StringTokenizer(excludeString, " ,;");
        List<String> list = new ArrayList<String>();
        while (t.hasMoreTokens()) {
            list.add(t.nextToken());
        }
        excludes = list;
    }

    static Method atExitMethod() {
        return atExitMethod;
    }

    static void setAtExitMethod(Method mmm) {
        atExitMethod = mmm;
    }

    /**
     * Return a Reader corresponding to the source of this location.
     * Return null if not available.
     * Note: returned reader must be closed.
     */
    static BufferedReader sourceReader(Location location) {
        return sourceMapper.sourceReader(location);
    }

    static synchronized String sourceLine(Location location, int lineNumber)
                                          throws IOException {
        if (lineNumber == -1) {
            throw new IllegalArgumentException();
        }

        try {
            String fileName = location.sourceName();

            Iterator<SourceCode> iter = sourceCache.iterator();
            SourceCode code = null;
            while (iter.hasNext()) {
                SourceCode candidate = iter.next();
                if (candidate.fileName().equals(fileName)) {
                    code = candidate;
                    iter.remove();
                    break;
                }
            }
            if (code == null) {
                BufferedReader reader = sourceReader(location);
                if (reader == null) {
                    throw new FileNotFoundException(fileName);
                }
                code = new SourceCode(fileName, reader);
                if (sourceCache.size() == SOURCE_CACHE_SIZE) {
                    sourceCache.remove(sourceCache.size() - 1);
                }
            }
            sourceCache.add(0, code);
            return code.sourceLine(lineNumber);
        } catch (AbsentInformationException e) {
            throw new IllegalArgumentException();
        }
    }

    static class SourceCode {
        private String fileName;
        private List<String> sourceLines = new ArrayList<String>();

        SourceCode(String fileName, BufferedReader reader)  throws IOException {
            this.fileName = fileName;
            try {
                String line = reader.readLine();
                while (line != null) {
                    sourceLines.add(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        }

        String fileName() {
            return fileName;
        }

        String sourceLine(int number) {
            int index = number - 1; // list is 0-indexed
            if (index >= sourceLines.size()) {
                return null;
            } else {
                return sourceLines.get(index);
            }
        }
    }
}
