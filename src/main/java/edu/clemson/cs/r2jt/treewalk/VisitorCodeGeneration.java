package edu.clemson.cs.r2jt.treewalk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;

public class VisitorCodeGeneration {

    private static String myPackagePath = "edu.clemson.cs.r2jt.";

    /**
     * Generates a treewalker. Two optional argument sin the array:
     * 1: the desired name of the walker (default: TreeWalker)
     * 2: the output package directory (default: treewalk)
     * @param String array
     */
    public static void main(String[] args) {
        String walkerName = "TreeWalker";
        String outputPackage = "treewalk";
        if (args.length > 0) {
            walkerName = args[0];
        }
        if (args.length == 2) {
            outputPackage = args[1];
        }
        String packageName = myPackagePath + outputPackage;
        StringBuilder buffer = generateVisitorClass(walkerName);
        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(path);
            ArrayList<File> dirs = new ArrayList<File>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                URI resourceURI = resource.toURI();
                dirs.add(new File(resourceURI.getPath()));
            }
            for (File directory : dirs) {
                String targetDir =
                        directory.getAbsolutePath().replace("bin", "src");
                String outputFile =
                        targetDir + File.separator + walkerName + ".java";
                FileWriter fstream = new FileWriter(outputFile);
                BufferedWriter out = new BufferedWriter(fstream);
                out.append("package " + packageName + ";\n\n");
                out.append(buffer);
                System.out.println("Successfully created " + outputFile);
                out.close();
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void generateVisitorClass() {
        System.out.println("public abstract class TreeWalkerVisitor {");
        System.out
                .println("\tpublic void preAny(ResolveConceptualElement node) { }");
        System.out
                .println("\tpublic void postAny(ResolveConceptualElement node) { }");
        try {
            Class<?>[] absynClasses = getClasses("edu.clemson.cs.r2jt.absyn");
            for (int i = 0; i < absynClasses.length; ++i) {
                if (ResolveConceptualElement.class
                        .isAssignableFrom(absynClasses[i])) {
                    String className = absynClasses[i].getSimpleName();
                    System.out.flush();
                    System.out.println("\tpublic void pre" + className + "("
                            + className + " node) { }");
                    System.out
                            .println("\tpublic void mid"
                                    + className
                                    + "("
                                    + className
                                    + " node, ResolveConceptualElement prevChild, ResolveConceptualElement nextChild) { }");
                    System.out.println("\tpublic void post" + className + "("
                            + className + " node) { }");
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("}");
        System.out.flush();
    }

    public static StringBuilder generateVisitorClass(String walkerName) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("import " + myPackagePath + "absyn.*;\n\n");
        buffer.append("public class " + walkerName
                + " extends TreeWalkerStackVisitor {\n");
        buffer
                .append("\tpublic void preAnyStack(ResolveConceptualElement data) { }\n");
        buffer
                .append("\tpublic void postAnyStack(ResolveConceptualElement data) { }\n");
        try {
            Class<?>[] absynClasses = getClasses(myPackagePath + "absyn");
            for (int i = 0; i < absynClasses.length; ++i) {
                if (ResolveConceptualElement.class
                        .isAssignableFrom(absynClasses[i])) {
                    String className = absynClasses[i].getSimpleName();
                    buffer.append("\tpublic void pre" + className + "("
                            + className + " data) { }\n");
                    buffer
                            .append("\tpublic void mid"
                                    + className
                                    + "("
                                    + className
                                    + " node, ResolveConceptualElement prevChild, ResolveConceptualElement nextChild) { }\n");
                    buffer.append("\tpublic void post" + className + "("
                            + className + " data) { }\n");
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        buffer.append("}\n");
        return buffer;
        //System.out.println(myOutputBuffer.toString());
        //System.out.flush();
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws URISyntaxException 
     */
    private static Class<?>[] getClasses(String packageName)
            throws ClassNotFoundException,
                IOException,
                URISyntaxException {
        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        ArrayList<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI resourceURI = resource.toURI();
            dirs.add(new File(resourceURI.getPath()));
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static ArrayList<Class<?>> findClasses(File directory,
            String packageName) throws ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "."
                        + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName
                        + '.'
                        + file.getName().substring(0,
                                file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
