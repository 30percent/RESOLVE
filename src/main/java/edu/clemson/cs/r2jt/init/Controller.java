/*
 * This software is released under the new BSD 2006 license.
 * 
 * Note the new BSD license is equivalent to the MIT License, except for the
 * no-endorsement final clause.
 * 
 * Copyright (c) 2007, Clemson University
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Clemson University nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This sofware has been developed by past and present members of the
 * Reusable Sofware Research Group (RSRG) in the School of Computing at
 * Clemson University. Contributors to the initial version are:
 * 
 * Steven Atkinson
 * Greg Kulczycki
 * Kunal Chopra
 * John Hunt
 * Heather Keown
 * Ben Markle
 * Kim Roche
 * Murali Sitaraman
 */
/*
 * Controller.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.init;

import antlr.collections.AST;
import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import edu.clemson.cs.r2jt.ResolveCompiler;
import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.analysis.Analyzer;
import edu.clemson.cs.r2jt.archiving.Archiver;
import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.compilereport.CompileReport;
import edu.clemson.cs.r2jt.data.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.errors.BugReport;
import edu.clemson.cs.r2jt.typeandpopulate.ScopeRepository;
import edu.clemson.cs.r2jt.typeandpopulate.MathSymbolTable;
import edu.clemson.cs.r2jt.typeandpopulate.MathSymbolTableBuilder;
import edu.clemson.cs.r2jt.typeandpopulate.ModuleIdentifier;
import edu.clemson.cs.r2jt.typeandpopulate.ModuleScope;
import edu.clemson.cs.r2jt.typeandpopulate.NoSuchSymbolException;
import edu.clemson.cs.r2jt.parsing.*;
import edu.clemson.cs.r2jt.population.*;
import edu.clemson.cs.r2jt.processing.*;
import edu.clemson.cs.r2jt.proving.AlternativeProver;
import edu.clemson.cs.r2jt.proving.Prover;
import edu.clemson.cs.r2jt.proving.ProverException;
import edu.clemson.cs.r2jt.proving.TheoremToVCsConverter;
import edu.clemson.cs.r2jt.proving.VCCollector;
import edu.clemson.cs.r2jt.proving.VerificationCondition;
import edu.clemson.cs.r2jt.sanitycheck.VisitorSanityCheck;
import edu.clemson.cs.r2jt.scope.OldSymbolTable;
import edu.clemson.cs.r2jt.parsing.RSimpleTrans;
import edu.clemson.cs.r2jt.proving2.AlgebraicProver;
import edu.clemson.cs.r2jt.proving2.VC;
import edu.clemson.cs.r2jt.translation.PrettyJavaTranslator;
import edu.clemson.cs.r2jt.translation.PrettyJavaTranslation;
import edu.clemson.cs.r2jt.translation.PrettyCTranslation;
import edu.clemson.cs.r2jt.translation.JavaTranslator;
import edu.clemson.cs.r2jt.type.TypeMatcher;
import edu.clemson.cs.r2jt.typereasoning.TypeGraph;
import edu.clemson.cs.r2jt.verification.AssertiveCode;
import edu.clemson.cs.r2jt.verification.Verifier;
import edu.clemson.cs.r2jt.treewalk.*;
import edu.clemson.cs.r2jt.vcgeneration.VCGenerator;
import edu.clemson.cs.r2jt.utilities.SourceErrorException;

/**
 * A manager for the target file of a compilation.
 */
public class Controller {

    // ===========================================================
    // Variables
    // ===========================================================

    //private Environment myInstanceEnvironment = Environment.getInstance();
    private final CompileEnvironment myInstanceEnvironment;
    CompileReport myCompileReport;
    private Archiver myArchive;
    //private final Archiver myArchive;

    private ErrorHandler err;

    //private Archiver arc = Archiver.getInstance();

    private FileLocator locator = new FileLocator();

    private File astDumpFile = null;

    private String[] noImportList = { "Std_Location_Linking_Realiz.rb", "Std_Array_Realiz.rb" };

    // ===========================================================
    // Constructors
    // ===========================================================

    public Controller(CompileEnvironment e) {
        myInstanceEnvironment = e;
        err = e.getErrorHandler();
        myCompileReport = e.getCompileReport();
        /*if(myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)){
        	myArchive = new Archiver(myInstanceEnvironment);
        }
        else{
        	myArchive = null;
        }*/
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /*
     * Glossary:
     * 
     * Target File - A file that appears on the command line of the compiler.
     * Import File - A file that is imported by a module being compiled. New
     * Target File - A target file that has not been seen by the compilation
     * environment. New Import File - An import file that has not been seem by
     * the compilation environment.
     */

    /**
     * Compiles a target file. A target file is one that is specified on the
     * command line of the compiler as opposed to one that is being compiled
     * because it was imported by another file.
     */
    public void compileTargetFile(File file, MathSymbolTableBuilder symbolTable) {
        try {
            err.resetCounts();
            err.setIgnore(false);
            if (myInstanceEnvironment.contains(file)) {
                if (myInstanceEnvironment.compileCompleted(file)) {
                    String msg = completeMessage(file.getName());
                    err.message(msg);
                }
                else if (myInstanceEnvironment.compileAborted(file)) {
                    String msg = abortMessage(file.getName());
                    err.error(msg);
                }
                else { // unresolved compilation
                    assert false : "unresolved compilation";
                }
            }
            else {
                if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                    myArchive = new Archiver(myInstanceEnvironment, file, null);
                }
                else {
                    myArchive = null;
                }
                if (myInstanceEnvironment.flags.isFlagSet(PrettyJavaTranslator.FLAG_TRANSLATE)
                        || myInstanceEnvironment.flags
                                .isFlagSet(PrettyJavaTranslation.FLAG_PRETTY_JAVA_TRANSLATE)
                        || myInstanceEnvironment.flags.isFlagSet(PrettyCTranslation.FLAG_PRETTY_C_TRANSLATE)) {
                    simpleTranslateNewTargetFile(file, symbolTable);
                }
                else {
                    compileNewTargetFile(file, symbolTable);
                }
                //compileNewTargetFile(file);
                if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                    //arc.printArchiveList();
                    //arc.prepArchiver(file);

                    myArchive.cleanupFiles();
                }
            }
        }
        catch (Throwable e) {
            Throwable cause = e;
            while (cause != null && !(cause instanceof SourceErrorException)) {
                cause = cause.getCause();
            }

            if (cause == null) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                throw new RuntimeException(e);
            }
            else {
                SourceErrorException see = (SourceErrorException) cause;
                err.error(see.getErrorLocation(), see.getMessage());
            }
        }
    }

    /**
     * Compiles target source code directly.
     */
    public void compileTargetSource(MetaFile inputFile, MathSymbolTableBuilder symbolTable) {
        err.resetCounts();
        err.setIgnore(false);
        if (myInstanceEnvironment.flags.isFlagSet(PrettyJavaTranslator.FLAG_TRANSLATE)
                || myInstanceEnvironment.flags.isFlagSet(PrettyJavaTranslation.FLAG_PRETTY_JAVA_TRANSLATE)
                || myInstanceEnvironment.flags.isFlagSet(PrettyCTranslation.FLAG_PRETTY_C_TRANSLATE)) {
            simpleTranslateNewTargetSource(inputFile, symbolTable);
        }
        else {
            //myInstanceEnvironment.setTargetFileName("Std_Unbounded_List_Realiz.rb");
            //myInstanceEnvironment.setTargetSource(myInstanceEnvironment.getUserFileFromMap("Unbounded_List_Template.Std_Unbounded_List_Realiz"));
            if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                //System.out.println(inputFile.getMyFile(myInstanceEnvironment.getMainDir()));
                if (inputFile.getMyKind().equals(ModuleKind.FACILITY)) {
                    String jarTempLoc = inputFile.getJarTempDir() + inputFile.getMyFileName();
                    myArchive =
                            new Archiver(myInstanceEnvironment, inputFile.getMyFile(myInstanceEnvironment
                                    .getMainDir()), inputFile);
                    myArchive.setOutputJar(jarTempLoc + inputFile.getMyKind().getExtension());
                }
                else {
                    myArchive =
                            new Archiver(myInstanceEnvironment, inputFile.getMyFile(myInstanceEnvironment
                                    .getMainDir()), inputFile);
                }

            }
            else {
                myArchive = null;
            }

            try {
                compileNewTargetSource(inputFile, symbolTable);
            }
            catch (Exception ex) {
                Throwable cause = ex;
                while (cause != null && !(cause instanceof SourceErrorException)) {
                    cause = cause.getCause();
                }

                if (cause != null) {
                    SourceErrorException see = (SourceErrorException) cause;
                    err.error(see.getErrorLocation(), see.getMessage());
                }
                else {
                    BugReport.abortProgram(ex, myInstanceEnvironment);
                    myCompileReport.setError();
                }
            }
            //compileNewTargetFile(file);
            if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                //arc.printArchiveList();
                //arc.prepArchiver(file);

                myArchive.cleanupFiles();
            }
        }
        /*if (env.contains(file)) {
        	if (env.compileCompleted(file)) {
        		String msg = completeMessage(file.getName());
        		err.message(msg);
        	} else if (env.compileAborted(file)) {
        		String msg = abortMessage(file.getName());
        		err.error(msg);
        	} else { // unresolved compilation
        		assert false : "unresolved compilation";
        	}
        } else { */

        //}
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    // -----------------------------------------------------------
    // New Target File Compilation Methods
    // -----------------------------------------------------------

    /**
     * The constant parameters in the head of this Concept/Enh/etc. cannot
     * appear in their initial (#) state in the ensures clause of this
     * operation.
     */
    private void checkOpDecs(List<Dec> decs, Dec dec, Iterator<ModuleParameterDec> params) {
        String checkStr = "";
        Iterator<Dec> h = decs.iterator();
        Iterator<Dec> i = decs.iterator();
        if (params != null) {
            List<ConstantParamDec> constants = new List<ConstantParamDec>("Constant Parameters");
            while (params.hasNext()) {
                Dec mp = params.next().getWrappedDec();
                if (mp instanceof ConstantParamDec) {
                    constants.add((ConstantParamDec) mp);
                }
            }
            Iterator<ConstantParamDec> cpdIt = constants.iterator();
            while (cpdIt.hasNext()) {
                ConstantParamDec cpdTemp = cpdIt.next();
                PosSymbol cpdSymbol = cpdTemp.getName();
                String cpdName = cpdSymbol.getName();
                while (h.hasNext()) {
                    Dec next = h.next();
                    if (next instanceof OperationDec) {
                        Exp ensures = ((OperationDec) next).getEnsures();
                        if (ensures != null) {
                            if (ensures.containsVar(cpdName, true)) {
                                err.error("Because of parameter mode 'evaluates' in Concept parameters,"
                                        + " \nensures clause of Operation " + (dec.getName()).getName()
                                        + " cannot contain #" + cpdName);
                            }
                        }
                    }
                }
            }
        }
        while (i.hasNext()) {
            Dec temp = i.next();
            if (temp instanceof OperationDec) {
                checkStr = ((OperationDec) temp).checkRequiresEnsures();
                if (checkStr != null) {
                    if (dec instanceof ConceptModuleDec) {
                        err.error(checkStr + " (Concept Module \"" + (dec.getName()).getName() + "\")");
                    }
                    else if (dec instanceof ConceptBodyModuleDec) {
                        err.error(checkStr + " (Concept Body Module \"" + (dec.getName()).getName() + "\")");
                    }
                    else if (dec instanceof FacilityModuleDec) {
                        err.error(checkStr + " (Facility Module \"" + (dec.getName()).getName() + "\")");
                    }
                    else if (dec instanceof EnhancementModuleDec) {
                        err.error(checkStr + " (Enhancement Module \"" + (dec.getName()).getName() + "\")");
                    }
                    else if (dec instanceof EnhancementBodyModuleDec) {
                        err.error(checkStr + " (Enhancement Body Module \"" + (dec.getName()).getName()
                                + "\")");
                    }
                }
            }
        }
    }

    /***************************************************************************
     * Checks this ModuleDec for any OperationDec's contained within. If found,
     * calls the checkRequiresEnsures() of that OperationDec.
     **************************************************************************/
    private void checkModeCompatibility(ModuleDec dec) {
        String checkStr = null;
        if (dec instanceof ConceptModuleDec) {
            List<Dec> decs = ((ConceptModuleDec) dec).getDecs();
            Iterator<ModuleParameterDec> params = (((ConceptModuleDec) dec).getParameters()).iterator();
            checkOpDecs(decs, dec, params);
        }
        else if (dec instanceof ConceptBodyModuleDec) {
            List<Dec> decs = ((ConceptBodyModuleDec) dec).getDecs();
            Iterator<ModuleParameterDec> params = (((ConceptBodyModuleDec) dec).getParameters()).iterator();
            checkOpDecs(decs, dec, params);
        }
        else if (dec instanceof FacilityModuleDec) {
            List<Dec> decs = ((FacilityModuleDec) dec).getDecs();
            checkOpDecs(decs, dec, null);
        }
        else if (dec instanceof EnhancementModuleDec) {
            List<Dec> decs = ((EnhancementModuleDec) dec).getDecs();
            Iterator<ModuleParameterDec> params = (((EnhancementModuleDec) dec).getParameters()).iterator();
            checkOpDecs(decs, dec, params);
        }
        else if (dec instanceof EnhancementBodyModuleDec) {
            List<Dec> decs = ((EnhancementBodyModuleDec) dec).getDecs();
            Iterator<ModuleParameterDec> params =
                    (((EnhancementBodyModuleDec) dec).getParameters()).iterator();
            checkOpDecs(decs, dec, params);
        }
    }

    private void compileNewTargetFile(File file, MathSymbolTableBuilder symbolTable) throws Exception {
        //private File compileNewTargetFile(File file) {
        //long start = System.currentTimeMillis();
        try {
            // AST debugging file output
            /*astDumpFile = new File(myInstanceEnvironment.getTargetFile()+".ast");
            try{
            	FileWriter fstream = new FileWriter(astDumpFile, true);
            	BufferedWriter out = new BufferedWriter(fstream);
            	out.write("\nAST for: "+myInstanceEnvironment.getTargetFile()+"\n");
            	out.close();
            }catch(Exception ex){
            	
            }*/
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            ModuleDec dec = buildModuleDec(file);
            ModuleID id = ModuleID.createID(dec);

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            checkDirectoryCompatibility(dec, id, file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            /* Invoke PreProcessor */
            PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            TreeWalker tw = new TreeWalker(preProc);
            tw.visit(dec);

            /* Have compile imported modules bring in extra stuff that 
             * the PreProcessor may have added manually.
             */
            compileImportedModules(dec, symbolTable);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            MathSymbolTable mathSymTab = getMathSymbolTable(dec, symbolTable);
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            // checkModeCompatibility(dec);
            myInstanceEnvironment.completeRecord(id, table);
            //env.setSuccess();
            if (myInstanceEnvironment.flags.isFlagSet(PrettyCTranslation.FLAG_PRETTY_C_TRANSLATE)) {
                PrettyCTranslation prettyT = new PrettyCTranslation(myInstanceEnvironment, table, dec, err);
                tw = new TreeWalker(prettyT);
                tw.visit(dec);
                System.out.println("");
            }
            if (myInstanceEnvironment.flags
                    .isFlagSet(JavaTranslator.JAVA_FLAG_TRANSLATE)) {
                translateModuleDec(file, mathSymTab, dec);
                //System.out.println("Translated: " + file.toString());
                if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                    myArchive.addFileToArchive(file);
                    if (!myCompileReport.hasError()) {
                        if (myArchive.createJar()) {
                            myCompileReport.setJarSuccess();
                        }
                    }
                    //arc.printArchiveList();
                }
                myInstanceEnvironment.printModules();
            }
            if (myInstanceEnvironment.flags.isFlagSet(Verifier.FLAG_VERIFY_VC)) {
                verifyModuleDec(symbolTable, table, dec);
            }

            if (myInstanceEnvironment.flags.isFlagSet(VCGenerator.FLAG_ALTVERIFY_VC)) {
                generateVCs(table, dec);
            }

            String currFileName = dec.getName().getFile().toString();
            if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_EXPORT_AST)) {
                genModuleDecDotFile(dec, currFileName + "_post");
            }
        }
        catch (CompilerException cex) {
            myInstanceEnvironment.abortCompile(file);
            myCompileReport.setError();
        }

        //long end = System.currentTimeMillis();
        //System.out.println("Execution time: " + (end - start) + " ms");
    }

    private void compileNewTargetSource(MetaFile inputFile, MathSymbolTableBuilder symbolTable)
            throws Exception {
        //private File compileNewTargetFile(File file) {
        //long start = System.currentTimeMillis();CharStream cs = null;
        File file = null;
        try {
            // AST debugging file output
            /*astDumpFile = new File(myInstanceEnvironment.getTargetFile()+".ast");
            try{
            	FileWriter fstream = new FileWriter(astDumpFile, true);
            	BufferedWriter out = new BufferedWriter(fstream);
            	out.write("\nAST for: "+myInstanceEnvironment.getTargetFile()+"\n");
            	out.close();
            }catch(Exception ex){
            	
            }*/
            String fileName = inputFile.getMyFileName();
            //String fileConcept = inputFile.getMyAssocConcept();
            //String filePkg = inputFile.getMyPkg();
            String fileSource = inputFile.getMyFileSource();
            //ModuleKind fileKind = inputFile.getMyKind();
            /*String filePath = myInstanceEnvironment.getMainDir().getAbsolutePath();
            if(fileKind.equals(ModuleKind.FACILITY)){
            	filePath += File.separator + "Facilities" + File.separator;
            }
            else{
            	filePath += File.separator + "Concepts" + File.separator;
            }
            filePath += filePkg + File.separator + fileName + fileKind.getExtension();
            file = new File(filePath);*/
            file = inputFile.getMyFile(myInstanceEnvironment.getMainDir());
            myInstanceEnvironment.getErrorHandler().setFile(file);
            myInstanceEnvironment.setTargetFile(file);
            CommonTokenStream tokens = getSourceTokenStream(fileName, fileSource);
            CommonTree ast = getParseTree(fileName, tokens);
            //myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            ModuleDec dec = getModuleDec(ast);
            ModuleID id = ModuleID.createID(dec);

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            //checkDirectoryCompatibility(dec, id, file);
            //file = createFileFromSource(id, fileName);
            //myInstanceEnvironment.getErrorHandler().setFile(file);
            //myInstanceEnvironment.setTargetFile(file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            /* Invoke PreProcessor */
            PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            TreeWalker tw = new TreeWalker(preProc);
            tw.visit(dec);

            /* Have compile imported modules bring in extra stuff that 
             * the PreProcessor may have added manually.
             */
            compileImportedModules(dec, symbolTable);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            MathSymbolTable mathSymTab = getMathSymbolTable(dec, symbolTable);
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            // checkModeCompatibility(dec);
            myInstanceEnvironment.completeRecord(id, table);
            //env.setSuccess();
            if (myInstanceEnvironment.flags
                    .isFlagSet(JavaTranslator.JAVA_FLAG_TRANSLATE)) {
                if (inputFile.getIsCustomLoc()) {
                    file = inputFile.getMyCustomFile();
                }
                translateModuleDec(file, mathSymTab, dec);
                //System.out.println("Translated: " + file.toString());
                if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                    myArchive.addFileToArchive(file);
                    if (!myCompileReport.hasError()) {
                        if (myArchive.createJar()) {
                            myCompileReport.setJarSuccess();
                        }
                    }
                    //arc.printArchiveList();
                }
                myInstanceEnvironment.printModules();
            }
            if (myInstanceEnvironment.flags.isFlagSet(Verifier.FLAG_VERIFY_VC)) {
                verifyModuleDec(symbolTable, table, dec);
            }
            String currFileName = dec.getName().getFile().toString();
            if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_EXPORT_AST)) {
                genModuleDecDotFile(dec, currFileName + "_post");
            }

        }
        catch (CompilerException cex) {
            myInstanceEnvironment.abortCompile(file);
            myCompileReport.setError();
        }

        //long end = System.currentTimeMillis();
        //System.out.println("Execution time: " + (end - start) + " ms");
    }

    private File createFileFromSource(ModuleID id, String fileName) {
        String ext = id.getModuleKind().getExtension();
        return new File(fileName + ext);
    }

    private void simpleTranslateNewTargetFile(File file, MathSymbolTableBuilder symbolTable) {
        //long start = System.currentTimeMillis();
        //File file = null;
        try {
            // AST debugging file output
            /*astDumpFile = new File(myInstanceEnvironment.getTargetFile()+".ast");
            try{
            	FileWriter fstream = new FileWriter(astDumpFile, true);
            	BufferedWriter out = new BufferedWriter(fstream);
            	out.write("\nAST for: "+myInstanceEnvironment.getTargetFile()+"\n");
            	out.close();
            }catch(Exception ex){
            	
            }*/
            myInstanceEnvironment.getErrorHandler().setFile(file);
            myInstanceEnvironment.setTargetFile(file);
            //myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            ModuleDec dec = buildModuleDec(file);
            ModuleID id = ModuleID.createID(dec);

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            //checkDirectoryCompatibility(dec, id, file);
            //file = createFileFromSource(id, fileName);
            //myInstanceEnvironment.getErrorHandler().setFile(file);
            //myInstanceEnvironment.setTargetFile(file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            // Invoke PreProcessor 
            //PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            //TreeWalker tw = new TreeWalker(preProc);
            //tw.visit(dec);

            // Have compile imported modules bring in extra stuff that 
            // the PreProcessor may have added manually.

            compileImportedModules(dec, symbolTable);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            // checkModeCompatibility(dec);
            myInstanceEnvironment.completeRecord(id, table);
            //env.setSuccess();
            if (myInstanceEnvironment.flags.isFlagSet(PrettyJavaTranslator.FLAG_TRANSLATE)
                    || myInstanceEnvironment.flags
                            .isFlagSet(PrettyJavaTranslation.FLAG_PRETTY_JAVA_TRANSLATE)
                    || myInstanceEnvironment.flags.isFlagSet(PrettyCTranslation.FLAG_PRETTY_C_TRANSLATE)) {
                translatePrettyModuleDec(file, table, dec);
                //System.out.println("Translated: " + file.toString());
                /*if(myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)){
                	myArchive.addFileToArchive(file);
                	if(!myCompileReport.hasError()){
                		if(myArchive.createJar()){
                			myCompileReport.setJarSuccess();
                		}
                	}
                }*/
                myInstanceEnvironment.printModules();
            }

        }
        catch (CompilerException cex) {
            myInstanceEnvironment.abortCompile(file);
            myCompileReport.setError();
        }
        catch (Exception ex) {
            BugReport.abortProgram(ex, myInstanceEnvironment);
            myCompileReport.setError();
        }
        //long end = System.currentTimeMillis();
        //System.out.println("Execution time: " + (end - start) + " ms");
    }

    private void simpleTranslateNewTargetSource(MetaFile inputFile, MathSymbolTableBuilder symbolTable) {
        //long start = System.currentTimeMillis();
        /*CharStream cs = null;
        try {
        	String fileName = myInstanceEnvironment.getTargetFileName();
        	String fileSource = myInstanceEnvironment.getTargetSource();
        	CommonTokenStream tokens = getSourceTokenStream(fileName, fileSource);
        	CommonTree ast = getParseTree(fileName, tokens);
        	simpleTranslateTree(ast, tokens);
        } catch (Exception ex) {
        	BugReport.abortProgram(ex, myInstanceEnvironment);
        	myCompileReport.setError();
        }*/
        File file = null;
        try {
            // AST debugging file output
            /*astDumpFile = new File(myInstanceEnvironment.getTargetFile()+".ast");
            try{
            	FileWriter fstream = new FileWriter(astDumpFile, true);
            	BufferedWriter out = new BufferedWriter(fstream);
            	out.write("\nAST for: "+myInstanceEnvironment.getTargetFile()+"\n");
            	out.close();
            }catch(Exception ex){
            	
            }*/
            String fileName = inputFile.getMyFileName();
            //String fileConcept = inputFile.getMyAssocConcept();
            //String filePkg = inputFile.getMyPkg();
            String fileSource = inputFile.getMyFileSource();
            //ModuleKind fileKind = inputFile.getMyKind();
            /*String filePath = myInstanceEnvironment.getMainDir().getAbsolutePath();
            if(fileKind.equals(ModuleKind.FACILITY)){
            	filePath += File.separator + "Facilities" + File.separator;
            }
            else{
            	filePath += File.separator + "Concepts" + File.separator;
            }
            filePath += filePkg + File.separator + fileName + fileKind.getExtension();
            file = new File(filePath);*/
            file = inputFile.getMyFile(myInstanceEnvironment.getMainDir());
            myInstanceEnvironment.getErrorHandler().setFile(file);
            myInstanceEnvironment.setTargetFile(file);
            CommonTokenStream tokens = getSourceTokenStream(fileName, fileSource);
            CommonTree ast = getParseTree(fileName, tokens);
            //myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            ModuleDec dec = getModuleDec(ast);
            ModuleID id = ModuleID.createID(dec);

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            //checkDirectoryCompatibility(dec, id, file);
            //file = createFileFromSource(id, fileName);
            //myInstanceEnvironment.getErrorHandler().setFile(file);
            //myInstanceEnvironment.setTargetFile(file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            // Invoke PreProcessor 
            //PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            //TreeWalker tw = new TreeWalker(preProc);
            //tw.visit(dec);

            // Have compile imported modules bring in extra stuff that 
            // the PreProcessor may have added manually.

            //compileImportedModules(dec);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            // checkModeCompatibility(dec);
            myInstanceEnvironment.completeRecord(id, table);
            //env.setSuccess();
            if (myInstanceEnvironment.flags.isFlagSet(PrettyJavaTranslator.FLAG_TRANSLATE)
                    || myInstanceEnvironment.flags
                            .isFlagSet(PrettyJavaTranslation.FLAG_PRETTY_JAVA_TRANSLATE)
                    || myInstanceEnvironment.flags.isFlagSet(PrettyCTranslation.FLAG_PRETTY_C_TRANSLATE)) {
                if (inputFile.getIsCustomLoc()) {
                    file = inputFile.getMyCustomFile();
                }
                translatePrettyModuleDec(file, table, dec);
                //System.out.println("Translated: " + file.toString());
                /*if(myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)){
                	myArchive.addFileToArchive(file);
                	if(!myCompileReport.hasError()){
                		if(myArchive.createJar()){
                			myCompileReport.setJarSuccess();
                		}
                	}
                }*/
                myInstanceEnvironment.printModules();
            }

        }
        catch (CompilerException cex) {
            myInstanceEnvironment.abortCompile(file);
            myCompileReport.setError();
        }
        catch (Exception ex) {
            BugReport.abortProgram(ex, myInstanceEnvironment);
            myCompileReport.setError();
        }
        //long end = System.currentTimeMillis();
        //System.out.println("Execution time: " + (end - start) + " ms");
    }

    private void simpleTranslateTree(CommonTree ast, CommonTokenStream tokens) {
        try {
            /*FileReader groupFileR = new FileReader("T.stg");
            StringTemplateGroup templates = new StringTemplateGroup(groupFileR);
            groupFileR.close();*/
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);
            nodes.setTokenStream(tokens);
            RSimpleTrans st = new RSimpleTrans(nodes);
            //st.setTemplateLib(templates);
            RSimpleTrans.module_return r = st.module(err);
            StringTemplate output = (StringTemplate) r.getTemplate();
            //System.out.println(output.toString());
            if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)) {
                CompileReport cp = myInstanceEnvironment.getCompileReport();
                cp.setTranslateSuccess();
                cp.setOutput(output.toString());
            }
            else {
                System.out.println(output.toString());
                //st.outputAsFile(fileName, output.toString());
            }
        }
        catch (RecognitionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------
    // Name Compatibility Methods
    // -----------------------------------------------------------

    private void checkNameCompatibility(Location loc, ModuleID id, File file) throws CompilerException {
        String idext = id.getModuleKind().getExtension();
        if (!file.getName().endsWith(idext)) {
            String msg = incompatibleModuleTypes(id.getModuleKind().toString(), extension(file.getName()));
            Location loc2 = beginOfLine(loc);
            err.error(loc2, msg);
            throw new CompilerException();
        }
        String base = basename(file.getName());
        if (id.getName() != Symbol.symbol(base)) {
            String msg = incompatibleNames(id.getName().toString(), base);
            err.error(loc, msg);
            throw new CompilerException();
        }
    }

    private Location beginOfLine(Location loc) {
        return new Location(loc.getFile(), new Pos(loc.getPos().getLine(), 1));
    }

    private String extension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    private String basename(String filename) {
        return filename.substring(0, filename.lastIndexOf("."));
    }

    // -----------------------------------------------------------
    // Directory Compatibility Methods
    // -----------------------------------------------------------

    private void checkDirectoryCompatibility(ModuleDec dec, ModuleID id, File file) throws CompilerException {
        if (!id.hasConcept()) {
            return;
        }
        String cName = id.getConceptFilename();
        File dir = file.getParentFile();
        try {
            locator.locateFileInDir(cName, dir);
        }
        catch (FileLocatorException flex) {
            Location loc = conceptLocation(dec);
            String msg = incompatibleDirectories(id.toString(), cName, dir.getName());
            err.error(loc, msg);
            throw new CompilerException();
        }
    }

    private Location conceptLocation(ModuleDec dec) {
        PosSymbol cName = null;
        if (dec instanceof EnhancementModuleDec) {
            cName = ((EnhancementModuleDec) dec).getConceptName();
        }
        else if (dec instanceof ConceptBodyModuleDec) {
            cName = ((ConceptBodyModuleDec) dec).getConceptName();
        }
        else if (dec instanceof EnhancementBodyModuleDec) {
            cName = ((EnhancementBodyModuleDec) dec).getConceptName();
        }
        else {
            assert false : "dec is an invalid type";
        }
        return cName.getLocation();
    }

    // -----------------------------------------------------------
    // Import Module Compilation Methods
    // -----------------------------------------------------------

    private void compileImportFile(File file, MathSymbolTableBuilder symbolTable) throws Exception {
        if (myInstanceEnvironment.compileCompleted(file)) {
            if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_NO_DEBUG)) {
                String msg = importCompleteMessage(file.getName());
                err.message(msg);
            }
            return;
        }
        if (myInstanceEnvironment.compileAborted(file)) {
            if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_NO_DEBUG)) {
                String msg = importAbortMessage(file.getName());
                err.error(msg);
            }
            return;
        }
        compileNewImportFile(file, symbolTable);
    }

    private void compileNewImportFile(File file, MathSymbolTableBuilder symbolTable) throws Exception {
        try {
            myInstanceEnvironment.setCurrentTargetFileName(file.getName());
            ModuleDec dec = buildModuleDec(file);
            ModuleID id = ModuleID.createID(dec);

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            /* Invoke PreProcessor */
            PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            TreeWalker tw = new TreeWalker(preProc);
            tw.visit(dec);

            /* Have compile imported modules bring in extra stuff that 
             * the PreProcessor may have added manually.
             */
            compileImportedModules(dec, symbolTable);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            MathSymbolTable mathSymTab = getMathSymbolTable(dec, symbolTable);
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            myInstanceEnvironment.completeRecord(id, table);
            // System.out.println(dec.asString(0,2));
            //if (myInstanceEnvironment.flags.isFlagSet(Translator.FLAG_TRANSLATE)) {
            //if(arc.needToTranslate(file)) translateModuleDec(table, dec);
            //if(env.createJarOn() && arc.needToTranslate(file)){
            if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                translateModuleDec(file, mathSymTab, dec);
                //arc.addFiletoArchive(file);
                //arc.printArchiveList();
            }
            //}
            if (myInstanceEnvironment.flags.isFlagSet(Verifier.FLAG_VERIFY_VC)) {
                //verifyModuleDec(context, dec);
                // I don't think this is necessary
            }
        }
        catch (CompilerException cex) {
            myInstanceEnvironment.abortCompile(file);
        }
    }

    private void compileNewImportSource(String name, MetaFile importFile, MathSymbolTableBuilder symbolTable)
            throws Exception {
        try {
            myInstanceEnvironment.setCurrentTargetFileName(name);
            String fileSource = importFile.getMyFileSource();
            File file = importFile.getMyFile(myInstanceEnvironment.getMainDir());
            myInstanceEnvironment.getErrorHandler().setFile(file);
            myInstanceEnvironment.setTargetFile(file);
            CommonTokenStream tokens = getSourceTokenStream(name, fileSource);
            CommonTree ast = getParseTree(name, tokens);
            ModuleDec dec = getModuleDec(ast);
            ModuleID id = ModuleID.createID(dec);

            //System.out.println(file.toString() + " Controller(772)");
            //System.out.println(id + " Controller(773)");

            checkNameCompatibility(dec.getName().getLocation(), id, file);
            myInstanceEnvironment.constructRecord(id, file, dec);
            compileImportedModules(dec, symbolTable);

            /* Invoke PreProcessor */
            PreProcessor preProc = new PreProcessor(myInstanceEnvironment);
            TreeWalker tw = new TreeWalker(preProc);
            tw.visit(dec);

            /* Have compile imported modules bring in extra stuff that 
             * the PreProcessor may have added manually.
             */
            compileImportedModules(dec, symbolTable);

            if (myInstanceEnvironment.showEnv()) { // DEBUG
                OldSymbolTable table = new OldSymbolTable(id, myInstanceEnvironment);
                myInstanceEnvironment.completeRecord(id, table);
                return;
            }
            MathSymbolTable mathSymTab = getMathSymbolTable(dec, symbolTable);
            OldSymbolTable table = analyzeModuleDec(symbolTable, dec);

            myInstanceEnvironment.completeRecord(id, table);
            // System.out.println(dec.asString(0,2));
            //if (myInstanceEnvironment.flags.isFlagSet(Translator.FLAG_TRANSLATE)) {
            //if(arc.needToTranslate(file)) translateModuleDec(table, dec);
            //if(env.createJarOn() && arc.needToTranslate(file)){
            if (myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
                if (importFile.getIsCustomLoc()) {
                    file = importFile.getMyCustomFile();
                }
                translateModuleDec(file, mathSymTab, dec);
                //arc.addFiletoArchive(file);
                //arc.printArchiveList();
            }
            //}
            if (myInstanceEnvironment.flags.isFlagSet(Verifier.FLAG_VERIFY_VC)) {
                //verifyModuleDec(context, dec);
                // I don't think this is necessary
            }
        }
        catch (CompilerException cex) {
            //myInstanceEnvironment.abortCompile(file);
        }
    }

    // -----------------------------------------------------------
    // Parsing Methods
    // -----------------------------------------------------------

    private ModuleDec buildModuleDec(File file) throws Exception {
        // FIX: Is this the only place we mess with this?
        //err.setFile(file);
        CommonTokenStream tokens = getFileTokenStream(file);
        CommonTree ast = getParseTree(file.toString(), tokens);
        ModuleDec dec = getModuleDec(ast);
        return dec;
    }

    private CommonTree getParseTree(String fileName, CommonTokenStream tokens) throws Exception {
        CommonTree ast = null;
        int initErrorCount = err.getErrorCount();
        RParser parser = new RParser(tokens);

        ColsASTAdaptor adaptor = new ColsASTAdaptor();
        parser.setTreeAdaptor(adaptor);
        RuleReturnScope results = parser.module(err);
        if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_EXPORT_AST)) {
            if (fileName.equals(myInstanceEnvironment.getTargetFile().toString())) {
                dumpTokenFile(tokens, parser.getTokenNames());
                genAstDotFile(results);
            }
        }
        ast = (CommonTree) results.getTree();
        if (err.countExceeds(initErrorCount)) {
            throw new CompilerException();
        }
        return ast;
    }

    private CommonTokenStream getFileTokenStream(File file) {
        CharStream cs = null;
        CommonTokenStream tokens = null;
        try {
            err.setFile(file);
            int initErrorCount = err.getErrorCount();
            String fileName = file.getAbsolutePath();
            cs = new ANTLRFileStream(fileName);
            RLexer lexer = new RLexer(cs);
            tokens = new CommonTokenStream();
            tokens.setTokenSource(lexer);
            if (err.countExceeds(initErrorCount)) {
                throw new CompilerException();
            }
        }
        catch (Exception ex) {
            BugReport.abortProgram(ex, myInstanceEnvironment);
            myCompileReport.setError();
        }
        return tokens;
    }

    private CommonTokenStream getSourceTokenStream(String fileName, String fileSource) {
        CharStream cs = null;
        CommonTokenStream tokens = null;
        try {
            err.setFilename(fileName);
            int initErrorCount = err.getErrorCount();
            cs = new ANTLRStringStream(fileSource);
            RLexer lexer = new RLexer(cs);
            tokens = new CommonTokenStream();
            tokens.setTokenSource(lexer);
            if (err.countExceeds(initErrorCount)) {
                throw new CompilerException();
            }
        }
        catch (Exception ex) {
            BugReport.abortProgram(ex, myInstanceEnvironment);
            myCompileReport.setError();
        }
        return tokens;
    }

    private ModuleDec getModuleDec(CommonTree ast) throws Exception {
        // AST debugging file output
        /*try{
        	FileWriter fstream = new FileWriter(astDumpFile, true);
        	BufferedWriter out = new BufferedWriter(fstream);
        	out.write("\t"+ast.toStringTree()+"\n");
        	out.close();
        }catch(Exception ex){
        	
        }*/
        int initErrorCount = err.getErrorCount();
        RBuilder builder = new RBuilder(new CommonTreeNodeStream(ast));
        ColsASTAdaptor adaptor = new ColsASTAdaptor();
        builder.setTreeAdaptor(adaptor);
        ModuleDec dec = builder.module(err).dec;

        String currFileName = dec.getName().getFile().toString();
        if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_EXPORT_AST)) {
            if (currFileName.equals(myInstanceEnvironment.getTargetFile().toString())) {
                genModuleDecDotFile(dec, currFileName);
            }
        }
        //RBuilder builder = new RBuilder();
        //builder.setASTNodeType("edu.clemson.cs.r2jt.parsing.ColsAST");
        //ModuleDec dec = builder.module(ast);
        if (err.countExceeds(initErrorCount)) {
            throw new CompilerException();
        }
        return dec;
    }

    // -----------------------------------------------------------
    // Import Compilation Methods
    // -----------------------------------------------------------

    private void compileImportedModules(ModuleDec dec, MathSymbolTableBuilder symbolTable) throws Exception {
        int initErrorCount = err.getErrorCount();
        /*
         * A set of visible theories must be accessible to the module scope
         * before population begins. Since the import scanner finds all imports,
         * this method is an efficient place to obtain the information. The
         * information must be added to the environment since the symbol table
         * has not been created yet.
         */
        ModuleID id = myInstanceEnvironment.getModuleID(dec.getName().getLocation().getFile());
        List<ModuleID> theories = new List<ModuleID>();
        ImportScanner scanner = new ImportScanner(myInstanceEnvironment);
        List<Import> imports = scanner.getImportList(dec);
        Iterator<Import> i = imports.iterator();
        while (i.hasNext()) {
            Import pid = i.next();
            compilePosModule(pid, dec, symbolTable);
            ModuleID id2 = guessModuleID(pid);

            if (myInstanceEnvironment.contains(id2)
                    && myInstanceEnvironment.compileCompleted(myInstanceEnvironment.getFile(id2))) {
                if (id2.getModuleKind() == ModuleKind.THEORY) {
                    theories.addUnique(id2);
                }
                theories.addAllUnique(myInstanceEnvironment.getTheories(id2));
            }
        }
        if (id == null) {
            System.out.println("name: " + dec.getName().getName() + " Controller(970)");
            System.out.println("location: " + dec.getName().getLocation().toString() + " Controller(969)");
            System.out.println("file: " + dec.getName().getLocation().getFile() + " Controller(969)");
        }
        myInstanceEnvironment.setTheories(id, theories);
        if (err.countExceeds(initErrorCount)) {
            throw new CompilerException();
        }
    }

    private ModuleID guessModuleID(Import pid) {
        ModuleID id = pid.getModuleID();
        if (id.getModuleKind() == ModuleKind.USES_ITEM) {
            ModuleID tid = ModuleID.createTheoryID(id.getName());
            if (myInstanceEnvironment.contains(tid)) {
                return tid;
            }
            ModuleID fid = ModuleID.createFacilityID(id.getName());
            if (myInstanceEnvironment.contains(fid)) {
                return fid;
            }
        }
        return id;
    }

    private void compilePosModule(Import pid, ModuleDec targetFile, MathSymbolTableBuilder symbolTable)
            throws Exception {
        try {
            ModuleID mid = pid.getModuleID();
            ModuleKind kind = mid.getModuleKind();

            String key = "";

            if (kind == ModuleKind.CONCEPT_BODY
                    || kind == ModuleKind.ENHANCEMENT
                    || kind == ModuleKind.ENHANCEMENT_BODY) {
                key += mid.getConceptName().getName() + ".";
            }
            else {
                key += mid.getName().getName() + ".";
            }
            key += mid.getName().getName();

            if (myInstanceEnvironment.isUserFile(key)) {
                MetaFile importFile =
                        myInstanceEnvironment.getUserFileFromMap(key);
                compileNewImportSource(key, importFile, symbolTable);
            }
            else {
                File file = getPosModuleFile(pid, targetFile);
                if (file != null) {
                    checkModuleDependencies(file, pid.getLocation());
                    compileImportFile(file, symbolTable);
                }
            }
        }
        catch (CompilerException cex) {
            /*
             * This catch is here so that we do not continue if getPosModuleFile
             * or checkModuleDependencies throws an error. In both cases, the
             * error count in the error handler will be increased, and
             * compilation will ultimately be aborted on the file from which the
             * pid originated.
             */
        }
    }

    private File getPosModuleFile(Import importID, ModuleDec sourceFile) throws CompilerException {

        File file = null;
        Location importLocationInCode = importID.getLocation();
        ModuleID moduleToImport = importID.getModuleID();

        try {
            if (moduleToImport.getModuleKind() == ModuleKind.USES_ITEM) {
                file = getUsesItemFile(importLocationInCode, moduleToImport);
            }
            else if (moduleToImport.getModuleKind() == ModuleKind.CONCEPT) {
                if (myInstanceEnvironment.contains(moduleToImport)) {
                    file = myInstanceEnvironment.getFile(moduleToImport);
                }
                else {
                    file =
                            locator.locateFileInTree(moduleToImport.getFilename(), myInstanceEnvironment
                                    .getMainDir());
                }
            }
            else { // ModuleKind is body or enhancement
                /* Check to see if this is one of the files we specified as not to be imported. */
                if (!onNoImportList(moduleToImport.getFilename())) {
                    file = getBodyOrEnhFile(importLocationInCode, moduleToImport, sourceFile);
                }
                else {
                    /* Create a dummy ConceptBodyModuleDec with just what we need */
                    ConceptBodyModuleDec newDec =
                            new ConceptBodyModuleDec(new PosSymbol(null, moduleToImport.getName()), null,
                                    new edu.clemson.cs.r2jt.collections.List<ModuleParameterDec>(),
                                    new PosSymbol(null, moduleToImport.getConceptName()),
                                    new edu.clemson.cs.r2jt.collections.List<PosSymbol>(),
                                    new edu.clemson.cs.r2jt.collections.List<UsesItem>(), null,
                                    new edu.clemson.cs.r2jt.collections.List<Exp>(),
                                    new edu.clemson.cs.r2jt.collections.List<Exp>(), new InitItem(),
                                    new FinalItem(), new edu.clemson.cs.r2jt.collections.List<Dec>());
                    /* Add this ConceptBodyModuleDec to the environment */
                    myInstanceEnvironment.constructRecord(moduleToImport, new File(moduleToImport
                            .getFilename()), newDec);

                    /* Still returning null */
                    file = null;
                }
            }
            return file;
        }
        catch (FileLocatorException flex) {
            err.error(importLocationInCode, flex.getMessage());
            throw new CompilerException();
        }
    }

    /* Function to check if our file is in the no import list */
    private boolean onNoImportList(String filename) {
        /* Loop through the noImportList */
        for (String s : noImportList) {
            /* If found, return true */
            if (s.equals(filename)) {
                return true;
            }
        }

        /* Otherwise return false */
        return false;
    }

    private File getUsesItemFile(Location loc, ModuleID id) throws CompilerException {
        File file = null;
        try {
            PosSymbol ps = new PosSymbol(loc, id.getName());
            List<File> files = getUsesFilesFromEnv(ps);
            if (files.size() == 0) {
                file =
                        locator.locateFileInTree(ModuleID.createConceptID(ps).getFilename(), ModuleID
                                .createFacilityID(ps).getFilename(), ModuleID.createTheoryID(ps)
                                .getFilename(), myInstanceEnvironment.getMainDir());
            }
            else if (files.size() == 1) {
                file = files.get(0);
            }
            else {
                String msg =
                        multiFilesMessage(ps.toString(), myInstanceEnvironment.getMainDir().getName(), files
                                .toString());
                err.error(loc, msg);
                throw new CompilerException();
            }
            return file;
        }
        catch (FileLocatorException flex) {
            err.error(loc, flex.getMessage());
            throw new CompilerException();
        }
    }

    private List<File> getUsesFilesFromEnv(PosSymbol ps) {
        List<File> files = new List<File>();
        ModuleID cid = ModuleID.createConceptID(ps);
        ModuleID fid = ModuleID.createFacilityID(ps);
        ModuleID mid = ModuleID.createTheoryID(ps);
        if (myInstanceEnvironment.contains(cid)) {
            files.add(myInstanceEnvironment.getFile(cid));
        }
        if (myInstanceEnvironment.contains(fid)) {
            files.add(myInstanceEnvironment.getFile(fid));
        }
        if (myInstanceEnvironment.contains(mid)) {
            files.add(myInstanceEnvironment.getFile(mid));
        }
        return files;
    }

    private File getBodyOrEnhFile(Location loc, ModuleID id, ModuleDec targetFile) throws CompilerException {

        boolean assocSearch = false;
        try {
            File file = null;
            if (myInstanceEnvironment.contains(id)) {
                file = myInstanceEnvironment.getFile(id);
            }
            else {
                assert id.hasConcept() : "id has not concept";
                PosSymbol cps = new PosSymbol(loc, id.getConceptName());
                ModuleID cid = ModuleID.createConceptID(cps);
                if (myInstanceEnvironment.contains(cid)) {
                    file = myInstanceEnvironment.getFile(cid);
                }
                else {
                    assocSearch = true;
                    // System.out.println(id.getFilename()); //DEBUG
                    file =
                            locator.locateFileInTree(id.getFilename(), targetFile.getName().getLocation()
                                    .getFile().getParentFile());
                    assocSearch = false;
                }
                File dir = file.getParentFile();
                file = locator.locateFileInDir(id.getFilename(), dir);
            }
            return file;
        }
        catch (FileLocatorException flex) {
            // flex.printStackTrace(); //DEBUG
            String msg = flex.getMessage();
            if (assocSearch) {
                msg = "Looking for associated concept. " + flex.getMessage();
            }
            err.error(loc, msg);
            throw new CompilerException();
        }
    }

    private void checkModuleDependencies(File file, Location loc) throws CompilerException {
        if (myInstanceEnvironment.compileIncomplete(file)) {
            ModuleID id = myInstanceEnvironment.getModuleID(file);
            String msg =
                    circularDependencyMessage(id.getName().toString(), myInstanceEnvironment
                            .printStackPath(id));
            err.error(loc, msg);
            throw new CompilerException();
        }
    }

    // -----------------------------------------------------------
    // Analysis Methods
    // -----------------------------------------------------------

    private OldSymbolTable analyzeModuleDec(ScopeRepository aRealSymbolTable, ModuleDec dec)
            throws CompilerException {
        OldSymbolTable table = getSymbolTable(aRealSymbolTable, dec);
        return table;
    }

    private OldSymbolTable getSymbolTable(ScopeRepository aRealSymbolTable, ModuleDec dec)
            throws CompilerException {
        int initErrorCount = err.getErrorCount();
        OldSymbolTable table = new OldSymbolTable(ModuleID.createID(dec), myInstanceEnvironment);
        OldPopulator populator = new OldPopulator(table, myInstanceEnvironment);
        populator.visitModuleDec(dec);

        // *** This next section is for testing the new tree walker ***

        // change twv to whatever visitor logic you want to use
        //VisitorPrintStructure twv = new VisitorPrintStructure();
        //TreeWalker tw = new TreeWalker(twv);
        //tw.visit(dec);

        //SanityCheck Walker -JCK
        VisitorSanityCheck sctwv = new VisitorSanityCheck(myInstanceEnvironment);
        TreeWalker tw = new TreeWalker(sctwv);
        tw.visit(dec);

        // ************************************************************

        if (err.getErrorCount() != initErrorCount) {
            throw new CompilerException();
        }

        table.bindTypeNames();

        if (err.getErrorCount() != initErrorCount) {
            throw new CompilerException();
        }

        Analyzer analyzer = new Analyzer(aRealSymbolTable, table, myInstanceEnvironment);
        analyzer.visitModuleDec(dec);

        if (err.getErrorCount() != initErrorCount) {
            throw new CompilerException();
        }

        return table;
    }

    private MathSymbolTable getMathSymbolTable(ModuleDec dec, MathSymbolTableBuilder symbolTable) {

        System.err.flush();

        //VisitorCodeGeneration.generateVisitorClass();

        /*edu.clemson.cs.r2jt.treewalk.VisitorPrintStructure ps = new edu.clemson.cs.r2jt.treewalk.VisitorPrintStructure();
        TreeWalker twps = new TreeWalker(ps);
        twps.visit(dec);*/

        System.err.flush();
        System.out.flush();

        Populator populator = new Populator(symbolTable);
        TreeWalker tw = new TreeWalker(populator);
        populator.setTreeWalker(tw);
        tw.visit(dec);

        System.err.flush();
        System.out.flush();

        /*MathAnalyzer analyzer = new MathAnalyzer(g, populator.getSymbolTable());
        tw = new TreeWalker(analyzer);
        tw.visit(dec);*/

        Populator.emitDebug("Type Graph:\n\n" + symbolTable.getTypeGraph().toString());

        return null;
    }

    // ------------------------------------------------------------
    // Verification Related Methods
    // ------------------------------------------------------------

    // Invoke the new VC Generator 
    // -YS
    private void generateVCs(OldSymbolTable table, ModuleDec dec) {

        // Create a new instance of the VC Generator and invoke the
        // tree walker on it.
        VCGenerator vcgen = new VCGenerator(table, myInstanceEnvironment);
        TreeWalker tw = new TreeWalker(vcgen);
        tw.visit(dec);
    }

    private void verifyModuleDec(ScopeRepository realTable, OldSymbolTable table, ModuleDec dec) {
        Verifier verifier = new Verifier(realTable, table, myInstanceEnvironment);
        verifier.visitModuleDec(dec);
        verifier.outputAsrt();

        //Regardless of whether or not we intend to do any proving, we sanity
        //check resulting VCs so that we fail early if there's a problem.  The
        //most likely problem is that some Exp in one of the generated VCs got
        //through without typing information--which would cause the Prover to
        //(rightly) crash.
        Iterable<VerificationCondition> vcsToProve = getVCsToProve(verifier, dec);

        try {
            for (VerificationCondition vc : vcsToProve) {
                AlternativeProver.convertToImmutableVC(vc);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("INVARIANT FAILED: Generated VCs are "
                    + "not valid Prover input!  See 'caused by' Exception for " + "details.", e);
        }
        //End sanity check

        if (myInstanceEnvironment.flags.isFlagSet(Prover.FLAG_LEGACY_PROVE)
                || myInstanceEnvironment.flags.isFlagSet(Prover.FLAG_PROVE)) {

            try {
                //Make sure we've got at least one VC to prove so that we don't
                //waste time and clutter output "proving" sets of zero VCs
                if (vcsToProve.iterator().hasNext()) {
                    new Prover(vcsToProve, myInstanceEnvironment);
                }
            }
            catch (ProverException e) {
                err.error(e.toString());
            }
        }
        else if (myInstanceEnvironment.flags.isFlagSet(AlgebraicProver.FLAG_PROVE)) {
            try {
                ModuleScope scope = realTable.getModuleScope(new ModuleIdentifier(dec));

                java.util.List<VC> vcs = new LinkedList<VC>();
                for (VerificationCondition originalVC : vcsToProve) {
                    vcs.add(edu.clemson.cs.r2jt.proving2.Utilities.convertToImmutableVC(originalVC));
                }

                AlgebraicProver prover =
                        new AlgebraicProver(realTable.getTypeGraph(), vcs, scope, myInstanceEnvironment.flags
                                .isFlagSet(AlgebraicProver.FLAG_INTERACTIVE), myInstanceEnvironment,
                                myInstanceEnvironment.getProverListener());

                try {
                    prover.start();
                }
                catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
            catch (NoSuchSymbolException nsse) {
                //Can't find the module we're in.  Shouldn't be possible.
                throw new RuntimeException(nsse);
            }
        }
    }

    private Iterable<VerificationCondition> getVCsToProve(Verifier verifier, ModuleDec verificationTarget) {
        ModuleDec targetDec =
                myInstanceEnvironment.getModuleDec(myInstanceEnvironment.getModuleID(myInstanceEnvironment
                        .getTargetFile()));

        Iterable<VerificationCondition> vcsToProve = null;

        if (targetDec == verificationTarget && verificationTarget instanceof MathModuleDec) {
            vcsToProve = new TheoremToVCsConverter((MathModuleDec) verificationTarget);
        }
        else {
            Collection<AssertiveCode> VCs = verifier.getFinalVCs();

            if (VCs == null) {
                VCs = new LinkedList<AssertiveCode>();
            }

            vcsToProve = new VCCollector(VCs);
        }

        return vcsToProve;
    }

    // ------------------------------------------------------------
    // Translation Related Methods
    // ------------------------------------------------------------

    private void translateModuleDec(File file, MathSymbolTable table,
            ModuleDec dec) {

        JavaTranslator javaTranslate =
                new JavaTranslator(myInstanceEnvironment, table, dec, err);

        TreeWalker tw = new TreeWalker(javaTranslate);
        tw.visit(dec);

        javaTranslate.outputCode(file);
    }

    private void translatePrettyModuleDec(File file, OldSymbolTable table,
            ModuleDec dec) {

        if (myInstanceEnvironment.flags
                .isFlagSet(PrettyJavaTranslation.FLAG_PRETTY_JAVA_TRANSLATE)) {

            PrettyJavaTranslation prettyT =
                    new PrettyJavaTranslation(myInstanceEnvironment, table,
                            dec, err);
            TreeWalker tw = new TreeWalker(prettyT);
            tw.visit(dec);

            prettyT.outputCode(file);
        }
        else {
            PrettyCTranslation prettyT = new PrettyCTranslation(myInstanceEnvironment, table, dec, err);
            TreeWalker tw = new TreeWalker(prettyT);
            tw.visit(dec);

            prettyT.outputCode(file);
        }

    }

    /*
     * This dumps the stream of tokens to a file
     */
    private void dumpTokenFile(CommonTokenStream tokens, String[] tokenNames) {
        try {
            File tokenFile = new File(myInstanceEnvironment.getTargetFile() + "_TOKENS.txt");
            FileWriter fstream = new FileWriter(tokenFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            String line, match, tokenNum;
            Pattern p;
            Matcher m;
            for (int i = 0; i < tokens.size(); i++) {
                line = tokens.get(i).toString();
                p = Pattern.compile("<\\d+>");
                m = p.matcher(line);
                if (m.find()) {
                    match = m.group();
                    tokenNum = match.substring(1, match.length() - 1);
                    line = line.replaceAll(tokenNum, tokenNames[Integer.parseInt(tokenNum)]);
                }
                out.write(line);
                out.newLine();
            }

            System.out.println("Dumped tokens to file: " + tokenFile.toString());
            out.close();
        }
        catch (Exception ex) {

        }
    }

    /*
     * This generates the dot file for the AST
     */
    private void genAstDotFile(RuleReturnScope results) {
        //create dot file
        try {
            DOTTreeGenerator gen = new DOTTreeGenerator();
            StringTemplate st = gen.toDOT((Tree) results.getTree());
            File dotFile = new File(myInstanceEnvironment.getTargetFile() + "_AST.dot");
            FileWriter fstream = new FileWriter(dotFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(st.toString());
            System.out.println("Exported AST to dot file: " + dotFile.toString());
            out.close();
        }
        catch (Exception ex) {

        }
    }

    /*
     * This takes the ModuleDec and generates a .dot file representation
     */
    private void genModuleDecDotFile(ModuleDec dec, String currFileName) {
        StringBuffer sb = new StringBuffer();
        sb.append("digraph {\n\n");
        sb.append("\tordering=out;\n");
        sb.append("\tranksep=.4;\n");
        sb
                .append("\tbgcolor=\"lightgrey\"; node [shape=box, fixedsize=false, fontsize=12, fontname=\"Helvetica-bold\", fontcolor=\"blue\"\n");
        sb
                .append("\t\twidth=.25, height=.25, color=\"black\", fillcolor=\"white\", style=\"filled, solid, bold\"];\n");
        sb.append("\tedge [arrowsize=.5, color=\"black\", style=\"bold\"]\n");
        sb.append("\n");
        //walk the tree and generated the output file
        VisitorGenModuleDecDot twv = new VisitorGenModuleDecDot();
        TreeWalker tw = new TreeWalker(twv);
        tw.visit(dec);
        sb.append(twv.getNodeList().toString());
        sb.append(twv.getArrowList().toString());
        sb.append("\n");
        sb.append("}\n");
        try {
            //System.out.println(currFileName);
            File decDotFile = new File(currFileName + "_ModuleDec.dot");
            FileWriter fstream = new FileWriter(decDotFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(sb.toString());
            System.out.println("Exported ModuleDec to dot file: " + decDotFile.toString());
            out.close();
        }
        catch (Exception ex) {

        }

    }

    /*
     * This takes the ModuleDec and generates a .svg file representation
     */
    private void genModuleDecSVGFile(ModuleDec dec, String currFileName) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" standalone=\"no\"?>\n\n");
        sb
                .append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        sb.append("\tordering=out;\n");
        sb.append("\tranksep=.4;\n");
        sb
                .append("\tbgcolor=\"lightgrey\"; node [shape=box, fixedsize=false, fontsize=12, fontname=\"Helvetica-bold\", fontcolor=\"blue\"\n");
        sb
                .append("\t\twidth=.25, height=.25, color=\"black\", fillcolor=\"white\", style=\"filled, solid, bold\"];\n");
        sb.append("\tedge [arrowsize=.5, color=\"black\", style=\"bold\"]\n");
        sb.append("\n");
        //walk the tree and generated the output file
        VisitorGenModuleDecDot twv = new VisitorGenModuleDecDot();
        TreeWalker tw = new TreeWalker(twv);
        tw.visit(dec);
        sb.append(twv.getNodeList().toString());
        sb.append(twv.getArrowList().toString());
        sb.append("\n");
        sb.append("}\n");
        try {
            //System.out.println(currFileName);
            File decDotFile = new File(currFileName + "_ModuleDec.dot");
            FileWriter fstream = new FileWriter(decDotFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(sb.toString());
            System.out.println("Exported ModuleDec to dot file: " + decDotFile.toString());
            out.close();
        }
        catch (Exception ex) {

        }

    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String abortMessage(String filename) {
        String msg = "Compile of target file " + filename + " already attempted and aborted due to errors.";
        return msg;
    }

    private String completeMessage(String filename) {
        String msg = "Target file " + filename + " has already been successfully compiled.";
        return msg;
    }

    private String importAbortMessage(String filename) {
        String msg = "Compile of import file " + filename + " already attempted and aborted due to errors.";
        return msg;
    }

    private String importCompleteMessage(String filename) {
        String msg = "Import file " + filename + " has already been successfully compiled.";
        return msg;
    }

    private String incompatibleNames(String idname, String basename) {
        String msg =
                "The module name \"" + idname + "\" does not match the file's basename \"" + basename + "\"";
        return msg;
    }

    private String incompatibleModuleTypes(String idtype, String extension) {
        String msg =
                "The module type \"" + idtype + "\" is not compatible " + " with the file extension \""
                        + extension + "\"";
        return msg;
    }

    private String incompatibleDirectories(String modID, String cName, String dir) {
        String msg =
                "This module (" + modID + ") must reside in the same "
                        + "directory as its associated concept, but no file with the " + "name " + cName
                        + " was found in the direcorty " + dir + ".";
        return msg;
    }

    private String circularDependencyMessage(String id, String path) {
        String msg = "Circular module dependency between this module " + " and " + id + ": " + path;
        return msg;
    }

    private String multiFilesMessage(String basename, String dir, String files) {
        String msg =
                "Found multiple files with the basename " + basename + " in the directory " + dir
                        + " or its subdirectories: " + files;
        return msg;
    }
}
