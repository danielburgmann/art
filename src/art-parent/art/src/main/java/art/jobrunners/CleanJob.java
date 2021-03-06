/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobrunners;

import art.cache.CacheHelper;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Deletes old report files and clears the mondrian cache
 *
 * @author Timothy Anyona
 */
public class CleanJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(CleanJob.class);

	@Autowired
	private CacheHelper cacheHelper;

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		//Delete old files in export directories
		cleanDirectory(Config.getReportsExportPath());
		cleanDirectory(Config.getRecordsExportPath());

		//clear mondrian cache
		clearMondrianCache();
	}

	/**
	 * Deletes old files in a given directory
	 *
	 * @param directoryPath the directory path
	 */
	private void cleanDirectory(String directoryPath) {
		logger.debug("Entering cleanDirectory: directoryPath='{}'", directoryPath);

		File directory = new File(directoryPath);
		File[] files = directory.listFiles();
		final long DELETE_FILES_MINUTES = 60; // Delete exported files older than x minutes
		final long DELETE_FILES_MILLIS = TimeUnit.MINUTES.toMillis(DELETE_FILES_MINUTES);
		long limit = System.currentTimeMillis() - DELETE_FILES_MILLIS;

		//only delete expected file types
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("pdf");
		validExtensions.add("xls");
		validExtensions.add("xlsx");
		validExtensions.add("html");
		validExtensions.add("zip");
		validExtensions.add("slk");
		validExtensions.add("gz");
		validExtensions.add("tsv");
		validExtensions.add("odt");
		validExtensions.add("ods");
		validExtensions.add("docx");
		validExtensions.add("pptx");
		validExtensions.add("csv");
		validExtensions.add("txt");
		validExtensions.add("aes");
		validExtensions.add("gpg");
		validExtensions.add("png");
		validExtensions.add("xlsm");
		validExtensions.add("json");

		for (File file : files) {
			// Delete the file if it is older than DELETE_FILES_MINUTES
			if (FileUtils.isFileOlder(file, limit)) {
				String extension = FilenameUtils.getExtension(file.getName());
				if (file.isDirectory() || ArtUtils.containsIgnoreCase(validExtensions, extension)) {
					FileUtils.deleteQuietly(file);
				}
			}
		}
	}

	/**
	 * Clears the mondrian cache
	 */
	private void clearMondrianCache() {
		logger.debug("Entering clearMondrianCache");

		int mondrianCacheExpiryHours = Config.getSettings().getMondrianCacheExpiryPeriod();
		long mondrianCacheExpiryMillis = TimeUnit.HOURS.toMillis(mondrianCacheExpiryHours);

		if (mondrianCacheExpiryMillis > 0) {
			String jpivotCacheFilePath = Config.getArtTempPath() + CacheHelper.JPIVOT_CACHE_FILE_NAME;
			File jpivotCacheFile = new File(jpivotCacheFilePath);
			long limit = System.currentTimeMillis() - mondrianCacheExpiryMillis;
			if (!jpivotCacheFile.exists() || FileUtils.isFileOlder(jpivotCacheFile, limit)) {
				cacheHelper.clearJPivot();
			}

			String saikuCacheFilePath = Config.getArtTempPath() + CacheHelper.SAIKU_CACHE_FILE_NAME;
			File saikuCacheFile = new File(saikuCacheFilePath);
			if (!saikuCacheFile.exists() || FileUtils.isFileOlder(saikuCacheFile, limit)) {
				cacheHelper.clearSaiku();
			}
		}
	}
}
