package com.exlibris.dps.delivery.vpp.filePath;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.exlibris.core.infra.common.exceptions.logging.ExLogger;
import com.exlibris.core.sdk.storage.handler.StorageHandler;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.storage.StorageHandlerLocator;
import com.exlibris.dps.sdk.access.AccessException;
import com.exlibris.dps.sdk.delivery.AbstractViewerPreProcessor;
import com.exlibris.dps.sdk.delivery.SmartFilePath;

public class FilePathVppPlugin extends AbstractViewerPreProcessor{

	private String pid = null;
	private static final ExLogger logger = ExLogger.getExLogger(FilePathVppPlugin.class);

	//This method will be called by the delivery framework before the call for the execute Method
	@Override
	public void init(DnxDocument dnx, Map<String, String> viewContext, HttpServletRequest request, String dvs,String ieParentId, String repParentId)
			throws AccessException {
		super.init(dnx, viewContext, request, dvs, ieParentId, repParentId);
		this.pid = getPid();
	}

	//Does the pre-viewer processing tasks.
	public void execute() throws Exception {
		Map<String, Object> paramMap = getAccess().getViewerDataByDVS(getDvs()).getParameters();
		// add params to session
		SmartFilePath filePath = null;
		String localFilePath = null;
		try {
			filePath = getAccess().getFilePathByDVS(dvs, this.pid);
			// try to access the file in a smarter way
			if (filePath.getStorageId() != null & filePath.getStoredEntityIdentifier() != null) {
				StorageHandler storageHandler = StorageHandlerLocator.getStorageHandlerByStorageId(filePath.getStorageId());
				localFilePath = storageHandler.getLocalFilePath(filePath.getStoredEntityIdentifier());
			}
			// default way
			else {
				localFilePath = filePath.getFilePath();
			}
		} catch (Exception e) {
			logger.error("Error In File Path VPP - cannot retreive the files to view", e, pid);
			throw new AccessException();
		}
		paramMap.put("filePath", localFilePath);
		getAccess().setParametersByDVS(getDvs(), paramMap);
	}
}