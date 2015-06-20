/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
(defun mvn-install ()
  (interactive)
  (with-current-buffer (get-buffer "*shell*")
    (goto-char (point-max))
    (insert "mvn install")
    (comint-send-input)
    )
  )

  (add-hook 'after-save-hook 'mvn-install)
 */

package org.netpreserve.openwayback.accesscontrol.staticmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StaticMapWhitelistFilterTest extends TestCase {

	File tmpFile = null;
	StaticMapWhitelistFilterFactory factory = null;
	UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	protected void setUp() throws Exception {
		super.setUp();
		factory = new StaticMapWhitelistFilterFactory();
		tmpFile = File.createTempFile("static-map", ".tmp");
		// disable INFO-level logging from StaticMapWhitelistFilter, not useful for
		// automated test.
		Logger.getLogger(StaticMapWhitelistFilter.class.getName()).setLevel(Level.WARNING);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(tmpFile != null && tmpFile.exists()) {
			tmpFile.delete();
		}
	}

	/**
	 * @throws Exception
	 */
	public void testRealWorld() throws Exception {
		String bases[] = { "pho-c.co.jp/~clever",
				   "sf.net/pop/Roger",
				   "www.eva-stu.vn",
				   "mins.com.br/",
				   "24.ne.jp",
				   "24.ne.jp/~nekko"};
//		setTmpContents(bases);
		
		
		ObjectFilter<CaptureSearchResult> filter = getFilter(bases);
		assertTrue("should be blocked",isBlocked(filter,"24.ne.jp.idpnt.com/robots.txt"));
		assertTrue("should be blocked",isBlocked(filter,"www.nkp.cz"));
		assertFalse("should be whitelisted",isBlocked(filter,"http://24.ne.jp:80/"));
		assertFalse("should be whitelisted",isBlocked(filter,"http://www.pho-c.co.jp/~clever"));
		assertFalse("should be whitelisted",isBlocked(filter,"http://24.ne.jp"));

		
		assertFalse("should be whitelisted",isBlocked(filter,"http://www.pho-c.co.jp/~clever"));
		assertFalse("massaged",isBlocked(filter,"http://pho-c.co.jp/~clever"));
		assertFalse("trailing-slash",isBlocked(filter,"http://pho-c.co.jp/~clever/"));
		assertFalse("subpath at whitelist",isBlocked(filter,"http://pho-c.co.jp/~clever/foo.txt"));

		assertFalse("full-port at whitelist",isBlocked(filter,"http://www.mins.com.br:80"));
		assertFalse("tail-slash-port at whitelist",isBlocked(filter,"http://www.mins.com.br:80/"));
		assertFalse("full path at whitelist",isBlocked(filter,"http://www.mins.com.br"));
		assertFalse("tail-slash at whitelist",isBlocked(filter,"http://www.mins.com.br/"));
		assertFalse("full path at whitelist",isBlocked(filter,"http://mins.com.br"));
		assertFalse("tail-slash at whitelist",isBlocked(filter,"http://mins.com.br/"));
		assertFalse("longer path at whitelist",isBlocked(filter,"http://mins.com.br/foo.txt"));
		assertFalse("subpath at whitelist",isBlocked(filter,"http://www13.mins.com.br/~clever/foo.txt"));

		assertFalse("path at whitelist",isBlocked(filter,"24.ne.jp"));
		assertFalse("full",isBlocked(filter,"http://www.mins.com.br"));
		assertFalse("subpath",isBlocked(filter,"www.24.ne.jp"));
		assertFalse("tail-slash at whitelist",isBlocked(filter,"http://mins.com.br/"));
		assertFalse("subpath",isBlocked(filter,"http://www.24.ne.jp:80/"));
		
		assertFalse(isBlocked(filter,"http://sf.net/pop/Roger"));
		assertFalse(isBlocked(filter,"http://sf.net/pop/Roger/"));
		assertFalse(isBlocked(filter,"http://sf.net/pop/Roger//"));
		assertTrue(isBlocked(filter,"http://sf.net/pop/"));
		assertFalse(isBlocked(filter,"http://sf.net/pop/Roger/2"));
		assertFalse(isBlocked(filter,"http://sf.net/pop/Roger/23"));
		assertFalse(isBlocked(filter,"http://www.sf.net/pop/Roger"));
		assertFalse(isBlocked(filter,"http://www1.sf.net/pop/Roger"));
		assertFalse(isBlocked(filter,"http://www23.sf.net/pop/Roger"));

		assertFalse(isBlocked(filter,"http://www23.eva-stu.vn/"));
		assertFalse(isBlocked(filter,"http://www23.eva-stu.vn"));

		/* TODO this should fail or not? */
		assertFalse(isBlocked(filter,"http://eva-stu.vn"));

		assertFalse(isBlocked(filter,"http://www.eva-stu.vn/"));
		assertFalse(isBlocked(filter,"http://eva-stu.vn/"));
		assertFalse(isBlocked(filter,"http://www.eva-stu.vn/foo.txt"));

		/* TODO this should fail or not? */
		assertFalse(isBlocked(filter,"http://www2.eva-stu.vn/foo/bar.txt"));

		assertFalse(isBlocked(filter,"http://eva-stu.vn/foo/bar.txt"));
	}

	
	/**
	 * @throws Exception
	 */
	public void testBaseNoPrefix() throws Exception {
		String bases[] = {"http://www.peagreenboat.com/",
				  "http://peagreenboat.com/"};
//		setTmpContents(bases);
		ObjectFilter<CaptureSearchResult> filter = getFilter(bases);
		assertFalse("should be whitelisted",isBlocked(filter,"http://www.peagreenboat.com"));
		assertFalse("should be whitelisted",isBlocked(filter,"http://peagreenboat.com"));
		assertTrue("other1",isBlocked(filter,"http://peagreenboatt.com"));
		assertTrue("other2",isBlocked(filter,"http://peagreenboat.org"));
		assertTrue("other3",isBlocked(filter,"http://www.peagreenboat.org"));
		// there is a problem with the SURTTokenizer... deal with ports!
//		assertFalse("other4",isBlocked(filter,"http://www.peagreenboat.com:8080"));
		assertFalse("subpath",isBlocked(filter,"http://www.peagreenboat.com/foo"));
		assertFalse("emptypath",isBlocked(filter,"http://www.peagreenboat.com/"));
	}
	
	private boolean isBlocked(ObjectFilter<CaptureSearchResult> filter, String url) throws URIException {
		CaptureSearchResult result = new CaptureSearchResult();
		result.setOriginalUrl(url);
		result.setUrlKey(canonicalizer.urlStringToKey(url));
		int filterResult = filter.filterObject(result);
		if(filterResult == ObjectFilter.FILTER_EXCLUDE) {
			return true;
		}
		return false;
	}
	
	private ObjectFilter<CaptureSearchResult> getFilter(String lines[]) 
		throws IOException {
		
		setTmpContents(lines);
		Map<String,Object> map = factory.loadFile(tmpFile.getAbsolutePath());
		return new StaticMapWhitelistFilter(map,canonicalizer);
	}

	private void setTmpContents(String[] lines) throws IOException {
		if(tmpFile != null && tmpFile.exists()) {
			tmpFile.delete();
		}
//		tmpFile = File.createTempFile("range-map","tmp");
		FileWriter writer = new FileWriter(tmpFile);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<lines.length; i++) {
			sb.append(lines[i]).append("\n");
		}
		String contents = sb.toString();
		writer.write(contents);
		writer.close();
		//factory.reloadFile();
	}
}
