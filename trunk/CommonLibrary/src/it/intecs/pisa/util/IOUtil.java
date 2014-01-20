/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IOUtil {

    public static String[] listDir(File schemaDir) {
        return listDir(schemaDir, "");
    }

    public static String[] listDir(File schemaDir, String removeStartPath) {
        Vector<String> files;
        String[] array;
        int arraylength;
        int i = 0;

        files = listSubDir(schemaDir);

        arraylength = files.size();
        array = new String[arraylength];
        for (String path : files) {
            if (removeStartPath != null && path.startsWith(removeStartPath)) {
                array[i] = path.substring(removeStartPath.length());
            } else {
                array[i] = path;
            }
            i++;
        }

        return array;
    }

    public static String loadString(File fileOnDisk) throws IOException {
        String fileName;

        fileName = fileOnDisk.getAbsolutePath();
        return loadString(fileName);
    }

    private static Vector<String> listSubDir(File schemaDir) {
        File subDir;
        File[] listingFiles;
        Vector<String> files;

        files = new Vector<String>();

        listingFiles = schemaDir.listFiles();

        if(listingFiles!=null)
        {
            for (File f : listingFiles) {
                if (f.isDirectory()) {
                    files.addAll(listSubDir(f));
                } else {
                    files.add(f.getAbsolutePath());
                }

            }
        }
       
        return files;
    }

    public static String readLine() {
        String line = null;
        try {
            line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            System.err.println(e);
        }
        return line;
    }

    public static byte[] loadBytes(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] buffer = new byte[(int) file.length()];
        DataInputStream input = new DataInputStream(new FileInputStream(file));
        input.readFully(buffer);
        input.close();
        return buffer;
    }

    public static String loadString(String fileName) throws IOException {
        return new String(loadBytes(fileName));
    }

    private static LinkedList loadLineList(String fileName) throws IOException {
        LinkedList lineList = new LinkedList();
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        for (String line = input.readLine(); line != null; line = input.readLine()) {
            lineList.addLast(line);
        }
        input.close();
        return lineList;
    }

    public static StringIterator loadLineIterator(String fileName) throws IOException {
        final LinkedList lineList = loadLineList(fileName);
        return new StringIterator() {

            Iterator iterator;
            

            {
                iterator = lineList.iterator();
            }

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public String next() {
                return (String) iterator.next();
            }
        };
    }

    public static String[] loadLineArray(String fileName) throws IOException {
        LinkedList lineList = loadLineList(fileName);
        String[] lineArray = new String[lineList.size()];
        Iterator lineIterator = lineList.iterator();
        for (int lineIndex = 0; lineIndex < lineArray.length; lineArray[lineIndex++] = (String) lineIterator.next()) {
            ;
        }
        return lineArray;
    }

    public static void dumpBytes(String fileName, byte[] buffer, boolean append) throws IOException {
        FileOutputStream output = new FileOutputStream(fileName, append);
        output.write(buffer);
        output.close();
    }

    public static void dumpString(String fileName, String buffer, boolean append) throws IOException {
        DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName, append));
        output.writeBytes(buffer);
        output.close();
    }

    public static void dumpReader(String fileName, Reader reader, boolean append) throws IOException {
        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(fileName, append));
        char[] buffer = new char[1024];
        int count;
        while ((count = reader.read(buffer)) >= 0) {
            output.write(buffer, 0, count);
        }
        output.close();
    }

    public static void dumpLineArray(String fileName, String[] lines, boolean append) throws IOException {
        DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName, append));
        for (int lineIndex = 0; lineIndex < lines.length; output.writeBytes(lines[lineIndex++] + '\n')) {
            ;
        }
        output.close();
    }

    public static void dumpBytes(String fileName, byte[] buffer) throws IOException {
        dumpBytes(fileName, buffer, false);
    }

    public static void dumpString(String fileName, String buffer) throws IOException {
        dumpString(fileName, buffer, false);
    }

    public static void dumpReader(String fileName, BufferedReader reader) throws IOException {
        dumpReader(fileName, reader, false);
    }

    public static void dumpLineArray(String fileName, String[] lines) throws IOException {
        dumpLineArray(fileName, lines, false);
    }

    public static void copy(File file, File directory) throws Exception {
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(new File(directory, file.getName()));
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) >= 0) {
            out.write(buffer, 0, count);
        }
        in.close();
        out.close();
    }

    public static void copyFile(File sourceFile, File targetFile) throws Exception {
        targetFile.getParentFile().mkdirs();
        
        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) >= 0) {
            out.write(buffer, 0, count);
        }
        in.close();
        out.close();
    }

    public static void copyDirectory(File sourceDir, File destDir) throws Exception {
        File[] elements;
        File destElement;

        elements = sourceDir.listFiles();

        if (elements != null) {
            for (File f : elements) {
                if (f.isDirectory()) {
                    destElement = new File(destDir, f.getName());
                    destElement.mkdir();

                    IOUtil.copyDirectory(f, destElement);
                } else {
                    destElement = new File(destDir, f.getName());
                    IOUtil.copyFile(f, destElement);
                }
           
        }
    }
    }

    public static void rmdir(File directory) throws Exception {
        File[] list = directory.listFiles();
        for (int index = 0; list!=null &&index < list.length; index++) {
            if (list[index].isDirectory()) {
                rmdir(list[index]);
            } else {
                list[index].delete();
            }
        }
        directory.delete();
    }

    public static String inputToString(InputStream input) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            out.write(b);
        }
        return out.toString();
    }
    


    /**
     * This method copy the data from an InputStream to an OtputStream
     * @param in InputStream
     * @param out OutputStream
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        synchronized (in) {
            synchronized (out) {

                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
        in.close();
        out.close();
    }

    /**
     * This method create a XML wich describes a directory structure
     * @param pathDirectory Directory Path
     */
    public static Document getDocumentFromDirectory(String pathDirectory) throws ParserConfigurationException {
        return getDocumentFromDirectory(pathDirectory, null);
    }

    /**
     * This method create a XML wich describes a directory structure
     * @param pathDirectory Directory Path
     */
    public static Document getDocumentFromDirectory(String pathDirectory, String filter) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        Document directoryDocuemnt = factory.newDocumentBuilder().newDocument();
        Element root = directoryDocuemnt.createElement("tree");
        root.setAttribute("id", "0");
        File directory = new File(pathDirectory);
        if (directory.isDirectory()) {
            Element directoryElement = directoryDocuemnt.createElement("item");
            directoryElement.setAttribute("text", directory.getName());
            directoryElement.setAttribute("type", "directory");
            String directoryArray[] = directory.list();
            File temp = null;
            Element fileElement;
            Element newDirectoryElement;
            for (int i = 0; i < directoryArray.length; i++) {
                temp = new File(pathDirectory, directoryArray[i]);
                if (temp.isDirectory()) {
                    newDirectoryElement = getElementFromDirectory(directoryDocuemnt, temp.getAbsolutePath(),filter);
                    directoryElement.appendChild(newDirectoryElement);
                } else {
                    fileElement = directoryDocuemnt.createElement("item");
                    fileElement.setAttribute("type", "file");
                    fileElement.setAttribute("text", directoryArray[i]);
                    directoryElement.appendChild(fileElement);
                }
            }
            root.appendChild(directoryElement);
        } else {
            if (filter != null) {
                if (directory.getName().endsWith(filter)) {
                    Element fileElement = directoryDocuemnt.createElement("item");
                    fileElement.setAttribute("type", "file");
                    fileElement.setAttribute("text", directory.getName());
                    root.appendChild(fileElement);
                }
            } else {
                Element fileElement = directoryDocuemnt.createElement("item");
                fileElement.setAttribute("type", "file");
                fileElement.setAttribute("text", directory.getName());
                root.appendChild(fileElement);
            }
        }

        directoryDocuemnt.appendChild(root);
        return (directoryDocuemnt);
    }

    private static Element getElementFromDirectory(Document ownDoc, String pathDirectory) {
        return getElementFromDirectory(ownDoc,pathDirectory,null);
    }

    private static Element getElementFromDirectory(Document ownDoc, String pathDirectory, String filter) {
        File directory = new File(pathDirectory);
        Element directoryElement = ownDoc.createElement("item");
        directoryElement.setAttribute("text", directory.getName());
        directoryElement.setAttribute("type", "directory");
        String directoryArray[] = directory.list();
        File temp = null;
        Element fileElement;
        Element newDirectoryElement;
        for (int i = 0; i < directoryArray.length; i++) {
            temp = new File(pathDirectory, directoryArray[i]);
            if (temp.isDirectory()) {
                newDirectoryElement = getElementFromDirectory(ownDoc, temp.getAbsolutePath());
                directoryElement.appendChild(newDirectoryElement);
            } else {
                if (filter != null) {
                    if (directory.getName().endsWith(filter)) {
                        fileElement = ownDoc.createElement("item");
                        fileElement.setAttribute("type", "file");
                        fileElement.setAttribute("text", directoryArray[i]);
                        directoryElement.appendChild(fileElement);
                    }
                } else {
                    fileElement = ownDoc.createElement("item");
                    fileElement.setAttribute("type", "file");
                    fileElement.setAttribute("text", directoryArray[i]);
                    directoryElement.appendChild(fileElement);
                }

            }
        }
        return directoryElement;
    }

    public static File getTemporaryFile()
    {
        try {
            File tempFile;
            tempFile = File.createTempFile(DateUtil.getCurrentDateAsUniqueId(), ".tmp");
            return tempFile;
        } catch (IOException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static File getTemporaryDirectory()
    {
        File tempDir=new File(System.getProperty("java.io.tmpdir"));
        File temp = new File(tempDir, DateUtil.getCurrentDateAsUniqueId());
        temp.mkdirs();

        return temp;
    }


    /**
     * This method compares two URI in order to check if one of the directory is a parent of the other
     * @param parentDirToCheck
     * @param file
     * @return
     */
    public static boolean isParentDirectory(URI parentDirToCheck, URI fileuri)
    {
        File parentDir;
        File file;

        parentDir=new File(parentDirToCheck);
        file=new File(fileuri);

        return isParentDirectory(parentDir,file);
    }

    public static boolean isParentDirectory(File parentDirToCheck,File fileuri)
    {
        if(parentDirToCheck == null || fileuri == null)
            return false;

        if(parentDirToCheck.equals(fileuri))
            return true;
        else return isParentDirectory(parentDirToCheck,fileuri.getParentFile());
    }

    public static void deleteOlderThan(File folder,Date treshold)
    {
        if(folder!=null && folder.exists())
        {
            for(File f:folder.listFiles())
            {
                if(f.isDirectory())
                    removeOldFilesFromSubdir(f,treshold);
            }
        }
    }

    private static void removeOldFilesFromSubdir(File f, Date treshold) {
        for(File subFile:f.listFiles())
        {
            try
            {
                if(subFile.lastModified()<treshold.getTime())
                    subFile.delete();
            }
            catch(Exception e)
            {
                System.out.println("Cannot delete file "+subFile.getAbsolutePath());
            }
        }
    }

    public static void moveFile(File localFile, File destFile) throws FileNotFoundException, IOException {
        //localFile.renameTo(destFile);
        IOUtil.copy(new FileInputStream(localFile), 
                               new FileOutputStream(destFile));
        localFile.delete();
    }


}
