/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.listeners;

import java.util.HashMap;

/**
 * Controls the status while either downloading or upgrading the files.
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface FormDownloaderListener {
    /**
     * Form downloading complete status.
     * @param result
     */
    void formDownloadingComplete(HashMap<String, String> result);
    /**
     * File updating progress status
     * @param currentFile The file is being updating.
     * @param progress The progress status.
     * @param total Total value for updating.
     */
    void progressUpdate(String currentFile, int progress, int total);
}
