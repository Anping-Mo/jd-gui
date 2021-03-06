/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.spi.SourceSaver;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;

public class ZipFileSourceSaverProvider extends DirectorySourceSaverProvider {

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.zip", "*:file:*.jar", "*:file:*.war", "*:file:*.ear"); }

    @Override
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        try {
            String sourcePath = getSourcePath(entry);
            Path path = rootPath.resolve(sourcePath);
            Path parentPath = path.getParent();

            if ((parentPath != null) && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            File tmpFile = File.createTempFile("jd-gui.", ".tmp.zip");
            tmpFile.delete();

            URI tmpFileUri = tmpFile.toURI();
            URI tmpArchiveUri = new URI("jar:" + tmpFileUri.getScheme(), tmpFileUri.getHost(), tmpFileUri.getPath() + "!/", null);

            HashMap<String,String> env = new HashMap<>();
            env.put("create", "true");

            FileSystem tmpArchiveFs = FileSystems.newFileSystem(tmpArchiveUri, env);
            Path tmpArchiveRootPath = tmpArchiveFs.getPath("/");

            saveContent(api, controller, listener, tmpArchiveRootPath, tmpArchiveRootPath, entry);

            tmpArchiveFs.close();

            Path tmpPath = Paths.get(tmpFile.getAbsolutePath());

            Files.move(tmpPath, path);
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }
}
