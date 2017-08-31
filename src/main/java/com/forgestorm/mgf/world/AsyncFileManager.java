package com.forgestorm.mgf.world;

import com.forgestorm.spigotcore.util.logger.ColorLogger;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/3/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

class AsyncFileManager extends BukkitRunnable {

    private final WorldManager worldManager;
    @Getter
    private final Queue<String> copyWorldQueue = new ConcurrentLinkedQueue<>();
    private final Queue<String> deleteWorldQueue = new ConcurrentLinkedQueue<>();
    private boolean copyingWorld = false;
    private boolean deletingWorld = false;

    AsyncFileManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    /**
     * This will prepare a world directory to be copied
     * from the backup directory.
     *
     * @param worldName The name of the world to copy.
     */
    void addWorldToCopy(String worldName) {
        copyWorldQueue.add(worldName);
    }

    /**
     * This will prepare a world directory to be deleted
     * from the server. If this is called, the world is
     * no longer needed and can be fully removed.
     * <p>
     * NOTE: When the world is needed later, it will be
     * restored from a backup.
     *
     * @param worldName The name of the world directory
     *                  to delete.
     */
    void addWorldToDelete(String worldName) {
        deleteWorldQueue.add(worldName);
    }

    /**
     * This will copy the world from the backup directory
     * to the server directory.
     */
    private void copyWorld() {
        if (!copyWorldQueue.isEmpty() && !copyingWorld) {
            // Put lock on copying worlds for now.
            copyingWorld = true;

            // Copy the world directory
            String worldName = copyWorldQueue.remove();
            File destinationFolder = new File(worldName);
            File backupFolder = new File("worlds" + File.separator + worldName.concat("_backup"));

            copyFolder(backupFolder, destinationFolder);

            // Now lets load the world.
            worldManager.loadWorld(worldName);

            // Now unlock the copying of worlds
            copyingWorld = false;
        }
    }

    /**
     * This will delete a world from the server directory.
     * If the world is needed later, it will be restored
     * from a backup directory.
     */
    private void deleteWorld() {
        if (!deleteWorldQueue.isEmpty() && !deletingWorld) {
            deletingWorld = true;
            deleteFolder(deleteWorldQueue.remove());
            deletingWorld = false;
        }
    }

    /**
     * The repeating task that will copy and delete worlds.
     */
    @Override
    public void run() {
        copyWorld();
        deleteWorld();
    }

    /**
     * Copies a world directory to another directory.
     *
     * @param src  The source destination of the folder to copy.
     * @param dest The end destination to copy the folder to.
     */
    private void copyFolder(File src, File dest) {
        if (src.isDirectory()) {

            // if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            // list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                // construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            // if file, then copy it
            // Use bytes stream to support all file types
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                // copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete's a file directory.
     *
     * @param filePath The file or folder that will be deleted.
     */
    private void deleteFolder(String filePath) {
        ColorLogger.DARK_GREEN.printLog(filePath);
        try {
            Files.walk(Paths.get(filePath))
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(file -> {

                        if (!file.delete()) {
                            ColorLogger.DARK_GREEN.printLog(file.getName());
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
