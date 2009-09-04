#
# Client code for Update Agent
# Copyright (c) 1999-2002 Red Hat, Inc.  Distributed under GPL.
#
# Author: Preston Brown <pbrown@redhat.com>
#         Adrian Likins <alikins@redhat.com
#         Cristian Gafton <gafton@redhat.com>
#

import up2dateLog
import gettext
_ = gettext.gettext
import OpenSSL
import config

class Error:
    """base class for errors"""
    def __init__(self, errmsg):
        self.errmsg = errmsg
        self.log = up2dateLog.initLog()

    def __repr__(self):
        self.log.log_me(self.errmsg)
        return self.errmsg
    
class FileError(Error):
    """
    error to report when we encounter file errors (missing files/dirs,
    lack of permissions, quoat issues, etc"""
    def __repr__(self):
        msg = _("Disk error.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class RpmError(Error):
    """rpm itself raised an error condition"""
    def __repr__(self):
        msg = _("RPM error.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class RpmInstallError(Error):
    """Raise when a package fails to install properly"""
    def __init__(self, msg, pkg = None):
        self.errmsg = msg
        self.pkg = pkg
    def __repr__(self):
        msg = _("There was a fatal error installing the package:\n")
        msg = msg + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg
    
class RhnServerException(Error):
    pass

class PasswordError(RhnServerException):
    """Raise when the server responds with that a password is incorrect"""
    def __repr__(self):
        log = up2dateLog.initLog()
        msg = _("Password error. The message was:\n") + self.errmsg
        log.log_me(msg)
        return msg

class ConflictError(Error):
    """Raise when a rpm transaction set has a package conflict"""
    def __init__(self, msg, rc=None, data=None):
        self.rc = rc
        self.errmsg = msg
        self.data = data
    def __repr__(self):
        msg = _("RPM package conflict error.  The message was:\n")
        msg = msg + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class FileConflictError(Error):
    """Raise when a rpm tranaction set has a file conflict"""
    def __init__(self, msg, rc=None):
        self.rc = rc
        self.errmsg = msg
    def __repr__(self):
        msg = _("RPM file conflict error. The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg
    
class DependencyError(Error):
    """Raise when a rpm transaction set has a dependency error"""
    def __init__(self, msg, deps=None):
        self.errmsg = msg
        # just tag on the whole deps tuple, so we have plenty of info
        # to play with
        self.deps = deps
        
    def __repr__(self):
        msg = _("RPM dependency error. The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class TransactionError(Error):
    """Raise when a rpm transaction set has a dependency error"""
    def __init__(self, msg, deps=None):
        self.errmsg = msg
        # just tag on the whole deps tuple, so we have plenty of info
        # to play with
        self.deps = deps
        
    def __repr__(self):
        msg = _("RPM  error. The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg


class UnsolvedDependencyError(Error):
    """Raise when we have a dependency that the server can not find"""
    def __init__(self, msg, dep=None, pkgs=None):
        self.errmsg = msg
        self.dep = dep
        self.pkgs = pkgs 
    def __repr__(self):
        msg = _("RPM dependency error.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class DependencySenseError(Error):
    """
    Raise when a rpm transaction set has a dependency sense "\
    "we don't understand"""
    def __init__(self, msg, sense=None):
        self.errmsg = msg
        self.sense = sense
    def __repr__(self):
        msg = _("RPM dependency error.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class SkipListError(Error):
    """Raise when all the packages you want updated are on a skip list"""
    def __init__(self, msg, pkglist=None):
	self.errmsg = msg
	self.pkglist = pkglist 
    def __repr__(self):
        msg = _("Package Skip List error.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class FileConfigSkipListError(Error):
    """
    Raise when all the packages you want updated are skip
    because of config or file skip list"""
    def __init__(self, msg, pkglist=None):
        self.errmsg = msg
        self.pkglist = pkglist
    def __repr__(self):
        msg = _("File Skip List or config file overwrite error. "\
                "The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg


class CommunicationError(RhnServerException):
    """Indicates a problem doing xml-rpc http communication with the server"""
    def __repr__(self):
        msg =  _("Error communicating with server. "\
                 "The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class FileNotFoundError(Error):
    """
    Raise when a package or header that is requested returns
    a 404 error code"""
    def __repr__(self):
        msg =  _("File Not Found: \n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg


class DelayError(RhnServerException):
    """
    Raise when the expected response from a xml-rpc call
    exceeds a timeout"""
    def __repr__(self):
        msg =  _("Delay error from server.  The message was:\n") + self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class RpmRemoveSkipListError(Error):
    """Raise when we try to remove a package on the RemoveSkipList"""
    def __repr__(self):
        msg = _("Could not remove package \"%s\". "\
                "It was on the RemoveSkipList") % self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class RpmRemoveError(Error):
    """
    Raise when we can't remove a package for some reason
    (failed deps, etc)"""
    def __init__(self, args):
        self.args = args
        self.errmsg = ""
        for key in self.args.keys():
            self.errmsg = self.errmsg + "%s failed because of %s\n" % (
                key, self.args[key])
        self.data = self.args
    def __repr__(self):
        return self.errmsg

class GPGInstallationError(Error):
    """Raise when we we detect that the GPG is not installed properly"""
    def __repr__(self):
        msg = _("GPG is not installed properly.")
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class GPGKeyringError(Error):
    """
    Raise when we we detect that the gpg keyring for the user
    does not have the Red Hat Key installed"""
    def __repr__(self):
        msg = _("GPG keyring does not include the Red Hat, Inc. "\
                "public package-signing key")
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class GPGVerificationError(Error):
    """Raise when we fail to verify a package is signed with a gpg signature"""
    def __init__(self, msg):
        self.errmsg = msg
        self.pkg = msg
    def __repr__(self):
        msg = _("The package %s failed its gpg signature verification. "\
                "This means the package is corrupt." % self.errmsg)
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class GPGVerificationUnsignedPackageError(Error):
    """
    Raise when a package that is supposed to be verified has
    no gpg signature"""
    def __init__(self, msg):
        self.errmsg = msg
        self.pkg = msg
    def __repr__(self):
        msg = _("Package %s does not have a GPG signature.\n") %  self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class GPGVerificationUntrustedKeyError(Error):
    """
    Raise when a package that is supposed to be verified has an
    untrusted gpg signature"""
    def __init__(self, msg):
        self.errmsg = msg
        self.pkg = msg
    def __repr__(self):
        msg = _("Package %s has a untrusted GPG signature.\n") % self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class GPGVerificationUnknownKeyError(Error):
    """
    Raise when a package that is supposed to be verified has an
    unknown gpg signature"""
    def __init__(self, msg):
        self.errmsg = msg
        self.pkg = msg
    def __repr__(self):
        msg = _("Package %s has a unknown GPG signature.\n") % self.errmsg
        log = up2dateLog.initLog()
        log.log_me(msg)
        return msg

class OutOfSpaceError(Error):
    def __init__(self, totalSize, freeDiskSpace):
        self.ts = totalSize
        self.fds = freeDiskSpace
        self.errmsg = "The total size of the selected packages (%d kB) "\
                      "exceeds your free disk space (%d kB)." % (
            self.ts, self.fds)

    def __repr__(self):
        return self.errmsg

class ServerThrottleError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg
    
class AbuseError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class AuthenticationTicketError(RhnServerException):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class AuthenticationError(Error):
    def __init__(self, msg):
        self.errmsg = msg
 
    def __repr__(self):
        return self.errmsg

class ValidationError(RhnServerException):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)

    # indicates an error during server input validation
    def __repr__(self):
        return _("Error validating data at server:\n") + self.errmsg

class InvalidRegistrationNumberError(ValidationError):
    pass

class InvalidProductRegistrationError(Error):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)

    # indicates an error during server input validation
    def __repr__(self):
        return _("The installation number is invalid") + self.errmsg
    
class OemInfoFileError(Error):
    def __init__(self,errmsg):
        Error.__init__(self, errmsg)

    def __repr__(self):
        return _("Error parsing the oemInfo file at field:\n") + self.errmsg

class NoBaseChannelError(RhnServerException):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)

    # No valid base channel was found for this system
    def __repr__(self):
        return self.errmsg

class UnknownMethodException(RhnServerException):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)

    def __repr__(self):
        return self.errmsg

class ServiceNotEnabledException(RhnServerException):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)

    def __repr__(self):
        return self.errmsg

class RhnUuidUniquenessError(RhnServerException):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class Up2dateNeedsUpdateError(Error):
    def __init__(self, msg=""):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class ServerCapabilityError(Error):
    def __init__(self, msg, errorlist=None):
        self.errmsg = msg
        self.errorlist = []
        if errorlist:
            self.errorlist=errorlist

    def __repr__(self):
        return self.errmsg

class ServerCapabilityMissingError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class ServerCapabilityVersionError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class NoChannelsError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class PackageNotAvailableError(Error):
    def __init__(self, msg, missing_packages=None):
        self.errmsg = msg
        self.missing_packages = missing_packages
    def __repr__(self):
        errstring = "%s\n" % self.errmsg
        for i in self.missing_packages:
            errstring = errstring + "%s\n" % i
        return errstring

class PackageArchNotAvailableError(Error):
    def __init__(self, msg, missing_packages=None):
        self.errmsg = msg
        self.missing_packages = missing_packages
    def __repr__(self):
        errstring = "%s\n" % self.errmsg
        for i in self.missing_packages:
            errstring = errstring + "%s\n" % i
        return errstring

class VirtualizationError(Error):
    def __init__(self, msg):
        self.errmsg = msg
    def __repr__(self):
        return self.errmsg

class SSLCertificateVerifyFailedError(Error):
    # TODO This should be a subclass of OpenSSL.Error or whatever and raised
    # from rhnlib.
    def __init__(self):
        # Need to override __init__ because the base class requires a message arg
        # and this exception shouldn't.
        up2dateConfig = config.initUp2dateConfig()
        certFile = up2dateConfig['sslCACert']
        f = open(certFile, "r")
        buf = f.read()
        f.close()
        tempCert = OpenSSL.crypto.load_certificate(OpenSSL.crypto.FILETYPE_PEM, buf)
        if tempCert.has_expired():
            Error.__init__(self ,"The certificate is expired. Please ensure you have the correct"
                           " certificate and your system time is correct.")
        else:
            Error.__init__(self, "The SSL certificate failed verification.")

class SSLCertificateFileNotFound(Error):
    def __init__(self, errmsg):
        Error.__init__(self, errmsg)


class AuthenticationOrAccountCreationError(ValidationError):
    """Class that can represent different things depending on context:
    While logging in with an existing user it represents a username or password 
    being incorrect.
    While creating a new account, it represents the username already being 
    taken or the user not being allowed to create an account.
    Optimally these different things would be different exceptions, but there
    are single fault codes the server can return to the client that can mean
    more than one of them so we have no way of knowing which is actually 
    intended.
    
    """
    pass

class NotEntitlingError(Error):
    pass

class InvalidProtocolError(Error):
    pass

class UnableToCreateUser(Error):
     def __init__(self, msg):
        self.errmsg = msg

     def __repr__(self):
        return self.errmsg

class ActivationKeyUsageLimitError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class LoginMinLengthError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class PasswordMinLengthError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class PasswordMaxLengthError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class ServerUnavailableError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg


class InsuffMgmntEntsError(RhnServerException):
    def __init__(self, msg ):
        self.errmsg = self.changeExplanation(msg)

    def __repr__(self):
        return self.errmsg

    def changeExplanation(self, msg):
        newExpln = _("""
    Your organization does not have enough Management entitlements to register this
    system to Red Hat Network. Please notify your organization administrator of this error. 
    You should be able to register this system after your organization frees existing 
    or purchases additional entitlements. Additional entitlements may be purchased by your
    organization administrator by logging into Red Hat Network and visiting
    the 'Subscription Management' page in the 'Your RHN' section of RHN.
    
    A common cause of this error code is due to having mistakenly setup an
    Activation Key which is set as the universal default.  If an activation key is set
    on the account as a universal default, you can disable this key and retry to avoid
    requiring a Management entitlement.""")
        term = "Explanation:"
        loc = msg.rindex(term) + len(term)
        return msg[:loc] + newExpln 

class NoSystemIdError(Error):
    def __init__(self, msg):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg

class InvalidRedirectionError(Error):
    """ Raise when redirect requests could'nt return a package"""
    def __init__(self, msg ):
        self.errmsg = msg

    def __repr__(self):
        return self.errmsg
