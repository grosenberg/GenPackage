/*******************************************************************************
 * Copyright (c) 2008-2014 G Rosenberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *		G Rosenberg - initial API and implementation
 *
 * Versions:
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 *******************************************************************************/
package net.certiv.antlr.project.util;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class Utils {

	private Utils() {}

	public static boolean checkValues(String reason, String... values) {
		for (String value : values) {
			if (value == null || value.length() == 0) {
				Log.warn(Utils.class, reason);
				return false;
			}
		}
		return true;
	}

	public static boolean createDirs(String dir) {
		try {
			FileUtils.forceMkdir(new File(dir));
		} catch (IOException e) {
			Log.error(Utils.class, "Failed to make directory for " + dir, e);
			return false;
		}
		return true;
	}

	/**
	 * Creates a new, unique directory, with the given prefix, in the system temp directory.
	 * 
	 * @return a File representing the new directory
	 * @throws IOException
	 */
	public static File createTmpDir() throws IOException {
		Path p = null;
		if (Strings.pathSepStr == Strings.STD_SEPARATOR) {
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
			p = Files.createTempDirectory("ReGen", attr);
		} else {
			p = Files.createTempDirectory("ReGen");
		}
		return p.toFile();
	}

	/**
	 * Clears (deletes) all files from the given directory.
	 * 
	 * @param dir
	 *            the directory to clear
	 * @return true if all files were successfully deleted
	 * @throws IOException
	 */
	public static boolean deleteAllFiles(File dir) throws IOException {
		if (dir == null) throw new IllegalArgumentException("Directory cannot be null");
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Directory must exist");

		DirectoryStream<Path> ds = Files.newDirectoryStream(dir.toPath());
		int del = 0;
		int tot = 0;
		for (Path p : ds) {
			File f = p.toFile();
			String name = f.getName();
			if (f.isFile()) {
				tot++;
				boolean ok = f.delete();
				if (ok) {
					del++;
				} else {
					Log.warn(Utils.class, "Failed to delete: " + name);
				}
			}
		}
		Log.info(Utils.class, "Deleted " + del + " of " + tot + " files");
		return del == tot;
	}

	/**
	 * Move all files from the source directory to the destination directory.
	 * 
	 * @param source
	 *            the source directory
	 * @param dest
	 *            the destination directory
	 * @return
	 * @throws IOException
	 */
	public static boolean moveAllFiles(File source, File dest) throws IOException {
		if (source == null || dest == null) throw new IllegalArgumentException("Directory cannot be null");
		if (!source.exists() || !source.isDirectory())
			throw new IOException("Source directory must exist: " + source.getCanonicalPath());

		dest.mkdirs();
		if (!dest.exists() || !dest.isDirectory())
			throw new IOException("Destination directory must exist: " + dest.getCanonicalPath());

		Path srcDir = source.toPath();
		Path dstDir = dest.toPath();
		DirectoryStream<Path> ds = Files.newDirectoryStream(srcDir);
		int tot = 0;
		for (Path src : ds) {
			Files.copy(src, dstDir.resolve(src.getFileName()), REPLACE_EXISTING);
			tot++;
		}
		Log.info(Utils.class, "Moved " + tot + " files");
		return false;
	}

	public static void mkTmplGrpBackup(String pathname) throws IOException {
		File file = new File(pathname);
		if (file.exists()) {
			Log.debug(Utils.class, "Creating backup copy of: " + pathname);
			Path source = Paths.get(pathname);
			Path target = Paths.get(pathname + ".bak");
			Files.move(source, target, REPLACE_EXISTING, COPY_ATTRIBUTES);
			Log.debug(Utils.class, "Backup made.");
		}
	}

	public static String findJarPathname(Class<?> ref) {
		try {
			File path = new File(ref.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			String name = path.getCanonicalPath();
			if (name.endsWith("jar")) {
				Log.info(Utils.class, "Found: " + name);
				return name;
			}
		} catch (NullPointerException | URISyntaxException | IOException e) {
			Log.warn(Utils.class, "Unable to access executing jar");
		}
		return null;
	}

	public static String scanClassPath(String regex) {
		String[] pathElements = System.getProperty("java.class.path").split(";");
		for (String pe : pathElements) {
			String lc = pe.toLowerCase();
			if (lc.matches(regex)) {
				Log.info(Utils.class, "Found: " + pe);
				return pe;
			}
		}
		return null;
	}
}
