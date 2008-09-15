/**
 * Copyright (c) 2008 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart;

import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;

/**
 * KickstartTroubleshootingCommand - for editing the pre and post steps
 * in a kickstart.
 * @version $Rev$
 */
public class KickstartTroubleshootingCommand extends BaseKickstartCommand {

    /**
     * Logger for this class
     */
    private static Logger logger = Logger
            .getLogger(KickstartTroubleshootingCommand.class);

    /**
     * Constructor
     * @param ksidIn id of the Kickstart to lookup
     * @param userIn userIn who owns the Kickstart
     */
    public KickstartTroubleshootingCommand(Long ksidIn, User userIn) {
        super(ksidIn, userIn);
    }

    /**
     * Get the bootloader type from the KickstartData object
     *
     * @return String: lilo or grub
     */
    public String getBootloaderType() {
        return getKickstartData().getBootloaderType();
    }

    /**
     * Set the bootloader type
     * @param bootloaderTypeIn lilo or grub
     */
    public void setBootloaderType(String bootloaderTypeIn) {
        logger.debug("setBootloaderType(String bootloaderTypeIn=" + bootloaderTypeIn +
                     ") - start");

        KickstartCommand bootloaderCommand = getKickstartData().getCommand("bootloader");

        if (bootloaderCommand == null) {
            logger.debug("No bootloader command yet.  Creating one.");
            
            bootloaderCommand = KickstartFactory.createKickstartCommand(getKickstartData(), 
                    "bootloader");
            if (bootloaderTypeIn.equals("lilo")) {
                bootloaderCommand.setArguments("--useLilo");
            }
        }
        else {
            getKickstartData().changeBootloaderType(bootloaderTypeIn);
        }
        logger.debug("setBootloaderType(String) - end");
    }
    
    /**
     * Get the kernel parameters
     * 
     * @return String of the kernel parameters
     */
    public String getKernelParams() {
        return getKickstartData().getKernelParams();
    }

    /**
     * Set the kernel parameters.  
     * @param kernelParamsIn the kernel parameters to set
     */
    public void setKernelParams(String kernelParamsIn) {
        logger.debug("setKernelParams(String kernelParamsIn=" + kernelParamsIn +
                     ") - start");

        getKickstartData().setKernelParams(kernelParamsIn);
        
        logger.debug("setKernelParams(String) - end");
    }

}
