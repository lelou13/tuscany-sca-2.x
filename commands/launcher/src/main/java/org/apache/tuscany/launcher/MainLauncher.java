/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors as applicable
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.launcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.jar.JarFile;

/**
 * Launcher for launcher runtime environment that invokes a jar's Main class.
 *
 * @version $Rev$ $Date$
 */
public class MainLauncher extends LauncherSupport {
    /**
     * Set the application classpath from a string delimited by path separators.
     * If the application classname is not defined, any jars on the path are examined
     * and if the Main-Class manifest entry is set then it will be used.
     *
     * @param path the path to parse
     */
    public void setClassPath(String path) {
        String[] files = path.split(File.pathSeparator);
        setApplicationLoader(createClassLoader(ClassLoader.getSystemClassLoader(), files));

        // if we don't have a main class yet, see if we can extract one from the jars
        for (int i = 0; getClassName() == null && i < files.length; i++) {
            String file = files[i];
            setClassName(getMainClassFromJar(file));
        }
    }

    protected String getMainClassFromJar(String f) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(f);
        } catch (IOException e) {
            // ignore and return
            return null;
        }
        try {
            return jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            // ignore and return
            return null;
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Calls the application's main class as defined by the MainClassName property.
     * The Thread's context ClassLoader is set to the application classloader before
     * the application class is loaded or the main method invoked.
     *
     * @throws ClassNotFoundException    if the specified class could not be loaded using the application classloader
     * @throws InvalidMainException      if the main method does not exist or is invalid
     * @throws InvocationTargetException if the main method throws an exception
     */
    public void callApplication() throws ClassNotFoundException, InvalidMainException, InvocationTargetException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getApplicationLoader());
        try {
            if (getClassName() == null) {
                throw new InvalidMainException("Main-Class not specified");
            }
            Class<?> mainClass = Class.forName(getClassName(), true, getApplicationLoader());
            Method main;
            try {
                main = mainClass.getMethod("main", String[].class);
            } catch (NoSuchMethodException e) {
                throw new InvalidMainException(getClassName());
            }
            if (!Modifier.isStatic(main.getModifiers())) {
                throw new InvalidMainException(main.toString());
            }
            try {
                main.invoke(null, (Object) getArgs());
            } catch (IllegalAccessException e) {
                // assertion as getMethod() should not have returned a method that is not accessible
                throw new AssertionError();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Main method.
     *
     * @param args the command line args
     */
    public static void main(String[] args) throws Throwable {
        MainLauncher launcher = new MainLauncher();
        launcher.parseArguments(args);
        try {
            launcher.callApplication();
        } catch (ClassNotFoundException e) {
            System.err.println("Main-Class not found: " + e.getMessage());
            System.exit(1);
        } catch (InvalidMainException e) {
            System.err.println("Invalid main method: " + e.getMessage());
            System.exit(1);
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace(System.err);
            System.exit(2);
        }
    }

    protected void parseArguments(String... args) {
    	String specifiedMain = null;
        int i = 0;
        while (i < args.length) {
            int left = args.length - i;
            String arg = args[i];
            if ("--classpath".equals(arg) && left > 1) {
                setClassPath(args[i + 1]);
                i += 2;
            } else if ("--main".equals(arg) && left > 1) {
                specifiedMain = args[i + 1];
                i += 2;
            } else if (arg.startsWith("--")) {
                usage();
            } else {
                break;
            }
        }
        
        // Specified main-class overrides anything found on classpath
        if (specifiedMain != null) {
        	setClassName(specifiedMain);
        }
        
        String[] mainArgs = new String[args.length - i];
        System.arraycopy(args, i, mainArgs, 0, mainArgs.length);
        setArgs(mainArgs);
    }

    protected void usage() {
        ResourceBundle bundle = ResourceBundle.getBundle(MainLauncher.class.getName());
        System.err.print(bundle.getString("org.apache.tuscany.launcher.Usage"));
        System.exit(1);
    }
}
