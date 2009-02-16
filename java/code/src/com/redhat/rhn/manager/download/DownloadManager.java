/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation. 
 */
package com.redhat.rhn.manager.download;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.security.SessionSwap;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.Patch;
import com.redhat.rhn.domain.rhnpackage.PatchSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ISOImage;
import com.redhat.rhn.manager.BaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provides methods for downloading packages and files and getting urls
 * DownloadManager
 * @version $Rev$
 */
public class DownloadManager extends BaseManager {

    public static final String DOWNLOAD_TYPE_PACKAGE = "package";
    public static final String DOWNLOAD_TYPE_SOURCE = "srpm";
    public static final String DOWNLOAD_TYPE_ISO = "iso";
    public static final String DOWNLOAD_TYPE_PATCH_README = "patchreadme";
    public static final String DOWNLOAD_TYPE_PATCH_SET_README = "patchsetreadme";    
    
    
    /**
     * Get a download path (part of the url) that is used to download a package.
     *  The url will be in the form of 
     *  /download/SHA1_TOKEN/EXPIRE_TIME/userId/packId/filename.rpm
     * @param pack the package
     * @param user the user
     * @return the path/url
     */
    public static String getPackageDownloadPath(Package pack, User user) {
        return getDownloadPath(pack.getId(), pack.getFile(), user, 
                DownloadManager.DOWNLOAD_TYPE_PACKAGE);
    }
    
    
    /**
     * Get a download path that is used to download a srpm.
     *  The url will be in the form of 
     *  /download/SHA1_TOKEN/EXPIRE_TIME/userId/packId/filename.rpm
     * @param pack the package
     * @param user the user
     * @return the path/url
     */
    public static String getPackageSourceDownloadPath(Package pack, User user) {
        return getDownloadPath(pack.getId(), pack.getFile(), user, 
                DownloadManager.DOWNLOAD_TYPE_SOURCE);
    }    
    
    
    /**
     * Get the an ISO download Path 
     * @param image the Iso Image
     * @param user the user
     * @return the path to be used to download the iso
     */
    public static String getISODownloadPath(ISOImage image, User user) {
        return getDownloadPath(image.getFileId(), image.getDownloadName(), user, 
                DownloadManager.DOWNLOAD_TYPE_PACKAGE);
    }
    
    /**
     * Get the patch readme download url/path
     * @param patch the patch 
     * @param user the user requesting
     * @return the path used to download the readme.
     */    
    public static String getPatchReadmeDownloadPath(Patch patch, User user) {
        return getDownloadPath(patch.getId(), "README", user, 
                DownloadManager.DOWNLOAD_TYPE_PATCH_README);
        
    }
    
    /**
     * Get the patch set readme download url/path
     * @param patchset the patch set
     * @param user the user requesting
     * @return the path used to download the readme.
     */
    public static String getPatchSetReadmeDownloadPath(PatchSet patchset, User user) {
        return getDownloadPath(patchset.getId(), "README", user, 
                DownloadManager.DOWNLOAD_TYPE_PATCH_SET_README);        
    }    
    
    private static String getDownloadPath(Long fileId, String filename, 
            User user, String type) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, Config.get().getInt(Config.DOWNLOAD_URL_LIFETIME));
        return "/download/" + type + "/" + getFileSHA1Token(fileId,  
                filename, user, cal.getTimeInMillis(), type) + "/" + 
                cal.getTimeInMillis() + "/" + user.getId() + "/" + fileId + "/" + 
                filename;
    }
    
    /**
     * get the Hmac SHA1 token use in constructing a package download url
     *      also useful if verifying a package download url
     * @param fileId the file id
     * @param filename the filename of the file
     * @param user the user requesting the file
     * @param expire the expire time
     * @param type the type of the download (i.e. package, iso, etc..)
     * @return a string representing the hash
     */
    public static String getFileSHA1Token(Long fileId, String filename, 
            User user, Long expire, String type) {
        
        List<String> data = new ArrayList<String>();
        data.add(expire.toString());
        data.add(user.getId().toString());
        data.add(fileId.toString());
        data.add(filename);
        data.add(type);
        
        return SessionSwap.rhnHmacData(data);
    }
    
    /**
     * Checks to see if a file exists
     * @param path the path to the file
     * @return true if available, false otherwise
     */
    public static boolean isFileAvailable(String path) {
        String file = Config.get().getString(Config.MOUNT_POINT) + "/" + path;
        return new File(file).exists();
    }    
    
    

    
}
