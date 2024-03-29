/**
 * 
 */

package org.odk.collect.android.logic;

import org.javarosa.core.reference.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ctsims
 */
public class FileReference implements Reference {
    String localPart;
    String referencePart;


    public FileReference(String localPart, String referencePart) {
        this.localPart = localPart;
        this.referencePart = referencePart;
    }


    private String getInternalURI() {
        return "/" + localPart + referencePart;
    }


    public boolean doesBinaryExist() throws IOException {
        return new File(getInternalURI()).exists();
    }


    public InputStream getStream() throws IOException {
        return new FileInputStream(getInternalURI());
    }


    public String getURI() {
        return "jr://file" + referencePart;
    }


    public boolean isReadOnly() {
        return false;
    }


    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(getInternalURI());
    }


    public void remove() throws IOException {
        // TODO bad practice to ignore return values
        new File(getInternalURI()).delete();
    }


    public String getLocalURI() {
        return getInternalURI();
    }

}
