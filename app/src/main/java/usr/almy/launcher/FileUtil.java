/*UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST
 UST  Copyright (c) 2020. Unknown Sources Team
 UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST UST */

package usr.almy.launcher;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("ALL")
class FileUtil {
	public static String recognizeType(String s) {
		if (s.charAt(0) == '/')
			return "path";
		else
			return "site";
	}

	static String deleteExtension(String path) {
		return path.split("\\.")[0];
	}

	static String getLastSegment(String path) {
		return path.split("/")[path.split("/").length - 1];
	}

	private static void createNewFile(String path) {
		int lastSep = path.lastIndexOf(File.separator);
		if (lastSep > 0) {
			String dirPath = path.substring(0, lastSep);
			makeDir(dirPath);
		}

		File file = new File(path);

		try {
			if (!file.exists())
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String readFile(String path) {
		createNewFile(path);

		StringBuilder sb = new StringBuilder();
		FileReader fr = null;
		try {
			fr = new FileReader(new File(path));

			char[] buff = new char[1024];
			int length;

			while ((length = fr.read(buff)) > 0) {
				sb.append(new String(buff, 0, length));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	static void writeFile(String path, String str) {
		createNewFile(path);
		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(new File(path), false);
			fileWriter.write(str);
			fileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileWriter != null)
					fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void extractFolder(String zipFile, String extractFolder) {
		try {
			int BUFFER = 2048;
			File file = new File(zipFile);

			ZipFile zip = new ZipFile(file);

			new File(extractFolder).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(extractFolder, currentEntry);
				//destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				assert destinationParent != null;
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip
							.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte[] data = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}


			}
		} catch (Exception ignored) {
		}

	}

	public static void copyFileFromRes(Context app, int resId, String destPath) {
		createNewFile(destPath);

		InputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = app.getResources().openRawResource(resId);
			fos = new FileOutputStream(destPath, false);

			byte[] buff = new byte[1024];
			int length;

			while ((length = fis.read(buff)) > 0) {
				fos.write(buff, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static void copyFile(String sourcePath, String destPath) {
		if (!isExistFile(sourcePath)) return;
		createNewFile(destPath);

		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(sourcePath);
			fos = new FileOutputStream(destPath, false);

			byte[] buff = new byte[1024];
			int length;

			while ((length = fis.read(buff)) > 0) {
				fos.write(buff, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void moveFile(String sourcePath, String destPath) {
		copyFile(sourcePath, destPath);
		deleteFile(sourcePath);
	}

	public static void deleteFile(String path) {
		File file = new File(path);

		if (!file.exists()) return;

		if (file.isFile()) {
			file.delete();
			return;
		}

		File[] fileArr = file.listFiles();

		if (fileArr != null) {
			for (File subFile : fileArr) {
				if (subFile.isDirectory()) {
					deleteFile(subFile.getAbsolutePath());
				}

				if (subFile.isFile()) {
					subFile.delete();
				}
			}
		}

		file.delete();
	}

	static boolean isExistFile(String path) {
		File file = new File(path);
		return file.exists();
	}

	static void makeDir(String path) {
		if (!isExistFile(path)) {
			File file = new File(path);
			file.mkdirs();
		}
	}

	static void listDir(String path, ArrayList<String> list) {
		File dir = new File(path);
		if (!dir.exists() || dir.isFile()) return;

		File[] listFiles = dir.listFiles();
		if (listFiles == null || listFiles.length <= 0) return;

		if (list == null) return;
		list.clear();
		for (File file : listFiles) {
			list.add(file.getAbsolutePath());
		}
	}

	public static boolean isDirectory(String path) {
		if (!isExistFile(path)) return false;
		return new File(path).isDirectory();
	}

	public static boolean isFile(String path) {
		if (!isExistFile(path)) return false;
		return new File(path).isFile();
	}

	public static long getFileLength(String path) {
		if (!isExistFile(path)) return 0;
		return new File(path).length();
	}

	static String getExternalStorageDir() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	static String getPackageDataDir(Context context) {
		return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath();
	}

	public static String convertUriToFilePath(final Context context, final Uri uri) {
		String path = null;
		if (DocumentsContract.isDocumentUri(context, uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					path = Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);

				if (!TextUtils.isEmpty(id)) {
					if (id.startsWith("raw:")) {
						return id.replaceFirst("raw:", "");
					}
				}

				final Uri contentUri = ContentUris
						.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

				path = getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = MediaStore.Audio.Media._ID + "=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};

				path = getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
			path = getDataColumn(context, uri, null, null);
		} else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
			path = uri.getPath();
		}

		if (path != null) {
			try {
				return URLDecoder.decode(path, "UTF-8");
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private static String getDataColumn(Context context, Uri uri, String selection, String[]
			selectionArgs) {
		Cursor cursor = null;

		final String column = MediaStore.Images.Media.DATA;
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch (Exception ignored) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}


	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	private static int calculateInSampleSize(BitmapFactory.Options options) {
		final int width = options.outWidth;
		final int height = options.outHeight;
		int inSampleSize = 1;

		if (height > 1024 || width > 1024) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((halfHeight / inSampleSize) >= 1024 && (halfWidth / inSampleSize) >= 1024) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	static Bitmap decodeSampleBitmapFromPath(String path) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = calculateInSampleSize(options);

		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
}