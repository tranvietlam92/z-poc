package com.zeus.hr.action.util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

public class FileUploadUtils {

	public static final String FOLDER_NAME_CANDIDATE_FILE = "CANDIDATE_FILE";

	/**
	 * @param userId
	 * @param groupId
	 * @param file
	 * @param sourceFileName
	 * @param serviceContext
	 * @return FileEntry
	 * @throws Exception
	 */
	public static FileEntry uploadCandidateFile(long userId, long groupId, File file, String sourceFileName,
			ServiceContext serviceContext) throws Exception {

		return uploadFile(userId, groupId, 0, file, sourceFileName, null, FOLDER_NAME_CANDIDATE_FILE, serviceContext);
	}

	/**
	 * @param userId
	 * @param groupId
	 * @param fileEntryId
	 * @param file
	 * @param sourceFileName
	 * @param fileType
	 * @param destination
	 * @param serviceContext
	 * @return FileEntry
	 * @throws Exception
	 */
	public static FileEntry uploadFile(long userId, long groupId, long fileEntryId, File file, String sourceFileName,
			String fileType, String destination, ServiceContext serviceContext) throws Exception {

		FileEntry fileEntry = null;

		if (file != null && Validator.isNotNull(sourceFileName)) {

			if (Validator.isNull(fileType)) {
				fileType = MimeTypesUtil.getContentType(sourceFileName);
			}

			String title = getFileName(sourceFileName);

			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			Calendar calendar = Calendar.getInstance();

			calendar.setTime(new Date());

			if (destination == null) {
				destination = StringPool.BLANK;
			}

			destination += calendar.get(Calendar.YEAR) + StringPool.SLASH;
			destination += calendar.get(Calendar.MONTH) + StringPool.SLASH;
			destination += calendar.get(Calendar.DAY_OF_MONTH);

			DLFolder dlFolder = DLFolderUtil.getTargetFolder(userId, groupId, groupId, false, 0, destination,
					StringPool.BLANK, false, serviceContext);

			User user = UserLocalServiceUtil.getUser(serviceContext.getUserId());

			PermissionChecker checker = PermissionCheckerFactoryUtil.create(user);
			PermissionThreadLocal.setPermissionChecker(checker);

			if (fileEntryId > 0) {
				fileEntry = DLAppLocalServiceUtil.updateFileEntry(userId, fileEntryId, sourceFileName, fileType, title,
						title, title, true, file, serviceContext);
			} else {
				fileEntry = DLAppLocalServiceUtil.addFileEntry(userId, groupId, dlFolder.getFolderId(), title, fileType,
						title, title, StringPool.BLANK, file, serviceContext);
			}
		}

		return fileEntry;
	}

	/**
	 * Auto generate filename for comunicate with multi system
	 * 
	 * @param sourceFileName
	 * @return String
	 */
	private static String getFileName(String sourceFileName) {
		String ext = FileUtil.getExtension(sourceFileName);

		return System.currentTimeMillis() + "." + ext;
	}

}