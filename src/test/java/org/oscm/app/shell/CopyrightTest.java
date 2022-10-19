/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2015年4月30日
 *
 *******************************************************************************/

package org.oscm.app.shell;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author goebel
 */
public class CopyrightTest {

    static ArrayList<String> failed = new ArrayList<String>();
    static final List<String> EXCLUDES = Arrays.asList("target", ".idea", ".settings", "pom.xml",
            "settings.xml", "apache-maven", "apache-maven.tar.gz");
    static boolean success = true;

    @Test
    public void hasCopyrightHeader() {

        ArrayList<String> dirs = new ArrayList<>();

        dirs.add(new File(System.getProperty("user.dir")).getAbsolutePath());
        for (String dir : dirs) {
            checkFiles(dir);
        }
        assertEquals("", Boolean.TRUE, success);
    }

    @After
    public void after() {
        if (!success) {
            System.out.println(
                    "Test failed due to the following files don't contain the specified copyright headers:");
            for (int i = 0; i < failed.size(); i++) {
                System.out.println(failed.get(i));
            }
        }
    }

    private static boolean isExcluded(String filePath) {
        for (String excludePath : EXCLUDES) {
            if (filePath.endsWith(excludePath)) {
                return true;
            }
        }
        return false;
    }

    public static void checkFiles(String root) {
        File dir = new File(root);
        File[] files = dir.listFiles();

        for (File file : files) {
            if (isExcluded(file.getAbsolutePath()))
                continue;

            if (file.isDirectory()) {
                checkFiles(file.getAbsolutePath());
            } else {
                checkHeader(file);
            }
        }
    }

    private static void checkHeader(File file) {
        final String fileName = file.getAbsolutePath();
        String ext = extension(fileName);
        System.out.println("* " + ext);
        switch (ext) {
            case "java":
            case "css":
            case "js":
                checkFile(fileName, "*  Copyright FUJITSU LIMITED 20");
                break;
            case "xml":
            case "xhtml":
                checkFile(fileName, "<!-- Copyright FUJITSU LIMITED 20");
                break;
            case "properties":
                checkFile(fileName, "# Copyright FUJITSU LIMITED 20");
                break;
            default:
        }
    }

    private static String extension(String fileName) {
        int idxS = fileName.lastIndexOf(File.separator);
        if (idxS > 0) {
            fileName = fileName.substring(idxS);
            System.out.println(fileName);
        }
        int idx = fileName.lastIndexOf(".");
        if (idx > 0) {
            return fileName.substring(idx + 1).toLowerCase();
        }
        return fileName.toLowerCase();
    }

    public static void checkFile(String filePath, String header) {
        try {

            final RandomAccessFile randomFile = new RandomAccessFile(filePath,
                    "rw");
            byte[] fileContent = new byte[(int) randomFile.length()];
            randomFile.readFully(fileContent);
            randomFile.close();
            String text = new String(fileContent);
            if (text.contains("Copyright IBM Corp")) {
                return;
            }
            if (!text.contains(header)) {
                success = false;
                failed.add(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
