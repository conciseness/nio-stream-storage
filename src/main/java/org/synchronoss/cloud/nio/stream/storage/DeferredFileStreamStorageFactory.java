/*
 * Copyright (C) 2015 Synchronoss Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.synchronoss.cloud.nio.stream.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * A factory for {@link FileStreamStorage} that creates a {@link FileStreamStorage} to store the bytes in memory until the threshold
 * is reached and writes the remaining bytes to disk. If the threshold is never reached the data will remain in memory.
 */

public class DeferredFileStreamStorageFactory implements StreamStorageFactory {

    private static final Logger log = LoggerFactory.getLogger(DeferredFileStreamStorageFactory.class);

    /**
     * Default max threshold. 10Kb
     */
    public static final int DEFAULT_MAX_THRESHOLD = 10240;
    public static final String DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/nio-stream-storage";

    private final File tempFolder;
    private final int maxSizeThreshold;
    private boolean purgeFileAfterReadComplete = false;
    private boolean deleteFilesOnDismiss = false;

    public void setPurgeFileAfterReadComplete(boolean purgeFileAfterReadComplete) {
        this.purgeFileAfterReadComplete = purgeFileAfterReadComplete;
    }

    public void setDeleteFilesOnDismiss(boolean deleteFilesOnDismiss) {
        this.deleteFilesOnDismiss = deleteFilesOnDismiss;
    }

    /**
     * <p> Constructor.
     *
     * @param tempFolderPath   The path to the folder where temporary data will be stored if the max threshold is reached.
     * @param maxSizeThreshold The threshold in bytes. When the data in memory exceeds this threshold it will be written to a temporary file.
     */
    public DeferredFileStreamStorageFactory(final String tempFolderPath, final int maxSizeThreshold) {
        tempFolder = new File(tempFolderPath);
        if (!tempFolder.exists()) {
            if (!tempFolder.mkdirs()) {
                throw new IllegalStateException("Unable to create the temporary folder: " + tempFolderPath);
            }
        }
        this.maxSizeThreshold = maxSizeThreshold > 0 ? maxSizeThreshold : 0;
        if (log.isDebugEnabled()) log.debug("Temporary folder: " + tempFolder.getAbsolutePath());
    }

    /**
     * <p> Constructor that uses the default threshold of 10kb.
     *
     * @param tempFolderPath The path to the folder were temporary data will be stored if the max threshold is reached.
     */
    public DeferredFileStreamStorageFactory(final String tempFolderPath) {
        this(tempFolderPath, DEFAULT_MAX_THRESHOLD);
    }

    /**
     * <p> Constructor that uses a default folder ${java.io.tmpdir}/nio-stream-storage.
     *
     * @param maxSizeThreshold The threshold in bytes. When the data in memory exceeds this threshold it will be written to a temporary file.
     */
    public DeferredFileStreamStorageFactory(int maxSizeThreshold) {
        this(DEFAULT_TEMP_FOLDER, maxSizeThreshold);
    }

    /**
     * <p> Constructor that uses a default threshold of 10kb and a default folder ${java.io.tmpdir}/nio-stream-storage.
     */
    public DeferredFileStreamStorageFactory() {
        this(DEFAULT_TEMP_FOLDER, DEFAULT_MAX_THRESHOLD);
    }

    /**
     * Creates a new {@link FileStreamStorage}.
     *
     * @return a {@link StreamStorage} to store bytes temporarily in-memory or on disk if over the configured threshold.
     */
    @Override
    public StreamStorage create() {
        final String tempFileName = String.format("stream-object-%s.tmp", UUID.randomUUID().toString());
        return new FileStreamStorage(new File(tempFolder, tempFileName), maxSizeThreshold, purgeFileAfterReadComplete, deleteFilesOnDismiss, false);
    }

}
