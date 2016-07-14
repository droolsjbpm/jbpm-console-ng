/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.console.ng.documents.backend.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.jbpm.console.ng.dm.model.DocumentSummary;

public class DocumentViewServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3950781302033089580L;

	@Inject
	private DocumentService documentService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		OutputStream out = response.getOutputStream();
		InputStream in = this.documentService.getDocumentContent(req
				.getParameter("documentId"));
		byte[] buffer = new byte[4096];
		int length;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}
		String documentName = req.getParameter("documentId");
		response.setHeader("Content-disposition", "attachment; filename="
				+ documentName);

		in.close();
		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			
			FileItem file = null;
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8");
			List items = upload.parseRequest(request);
			Iterator it = items.iterator();
			String folder = "/";
			while (it.hasNext()) {
				FileItem item = (FileItem) it.next();
				if (!item.isFormField()) {
					file = item;
				} else {
					if ("documentFolder".equals(item.getFieldName())) {
						folder = item.getString();
					}
				}

			}

			response.getWriter().write(processUpload(file, folder));
			response.setContentType("text/html");
		} catch (Exception e) {

		}
	}

	private String processUpload(final FileItem uploadItem, String folder)
			throws IOException {

		// If the file it doesn't exist.
		if ("".equals(uploadItem.getName())) {
			throw new IOException("No file selected.");
		}

		uploadFile(uploadItem, folder);
		uploadItem.getInputStream().close();

		return "OK";
	}

	private void uploadFile(final FileItem uploadItem, String folder)
			throws IOException {
		InputStream fileData = uploadItem.getInputStream();
		// GAV gav = uploadItem.getGav();

		try {
			// if ( gav == null ) {
			if (!fileData.markSupported()) {
				fileData = new BufferedInputStream(fileData);
			}

			// is available() safe?
			fileData.mark(fileData.available());

			byte[] bytes = IOUtils.toByteArray(fileData);
			DocumentSummary documenSummary = new DocumentSummary(
					uploadItem.getName(), "", folder);
			documenSummary.setContent(bytes);
			this.documentService.createDocument(documenSummary);
		} catch (Exception e) {

		}
	}
}
