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
package com.redhat.rhn.manager.user.test;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.user.CreateUserCommand;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.redhat.rhn.common.validator.ValidatorError;

public class CreateUserCommandTest extends RhnBaseTestCase {
    
    private CreateUserCommand command;
    
    public void setUp() {
        command = new CreateUserCommand();
        assertNotNull(command.getUser());
    }

    public void testValidate() {
        String invalidLogin = "";
        String validLogin   = TestUtils.randomString();

        String invalidPassword = "p";
        String validPassword = "password";
        
        String invalidEmail = "foobar";
        String validEmail   = "foobar@foobar.com";
        
        //Test invalid values
        command.setLogin(invalidLogin);
        command.setEmail(invalidEmail);
        command.setPassword(invalidPassword);
        command.setFirstNames("testuser");
        command.setLastName("testuser");

        //We should get 3 errors (login, email, password)
        Object[] errors = command.validate();
        assertEquals(3, errors.length);
        
        //Test valid values
        command.setLogin(validLogin);
        command.setEmail(validEmail);
        command.setPassword(validPassword);
        
        errors = command.validate();
        assertEquals(0, errors.length);
    }
    
    public void testStore() {
        Org org = UserTestUtils.findNewOrg("testorg");
        
        String login = TestUtils.randomString();
        command.setLogin(login);
        command.setPassword("password");
        command.setEmail("rhn-java-unit-tests@redhat.com");
        command.setFirstNames("Chuck Norris");
        command.setLastName("Texas Ranger");
        command.setOrg(org);
        command.setCompany("Test company");
        
        Object[] errors = command.validate();
        assertEquals(0, errors.length);
        
        command.storeNewUser();
        
        Long uid = command.getUser().getId();
        assertNotNull(uid);
        
        User result = UserFactory.lookupById(uid);
        assertEquals(login, result.getLogin());
    }
    
    public void testUsernameValidation() {
        // setup stuff required for command
        command.setEmail("validemail@mycompany.com");
        command.setFirstNames("testuser");
        command.setLastName("testuser");
        command.setPassword("validPassword");
        
        invalidUsername("foo&user", command);
        invalidUsername("joe+page", command);
        invalidUsername("joe user", command);
        invalidUsername("10%users", command);
        invalidUsername("joe'suser", command);
        invalidUsername("`eval`", command);
        invalidUsername("joe=page", command);
        invalidUsername("foo#user", command);
        invalidUsername("joe\"user", command);
        invalidUsername("機能拡張を", command); 
        invalidUsername("shughes login", command); 
        invalidUsername("shughes%login", command); 
        invalidUsername(" shughes", command); 
        invalidUsername("a p&i+u%s'e r1150586011843", command); // bug195807
        
        validUsername("john.cusack@foobar.com", command);
        validUsername("a$user", command);
        validUsername("!@$^*()-_{}[]|\\:;?", command);
        validUsername("/usr/bin/ls", command);
        validUsername("shughes_login", command);
        validUsername("shughes@redhat.com", command);
        validUsername("/shughes_login", command);
        validUsername("/\\/\\ark", command);
    }
    
    private void invalidUsername(String username, CreateUserCommand cmd) {
        // ok now validate
        cmd.setLogin(username);
        Object[] errors = cmd.validate();
        assertNotNull(errors);
        assertEquals(username + " caused failure", 1, errors.length);
    }
    
    private void validUsername(String username, CreateUserCommand cmd) {
        // ok now validate
        cmd.setLogin(username);
        Object[] errors = cmd.validate();
        assertNotNull(errors);
        assertEquals(username + " caused failure", 0, errors.length);
    }


    public void testValidatePasswordHasTabCharacter() throws Exception {
        command.setLogin("bilbo");
        command.setEmail("bilbo@baggins.com");
        command.setPassword("aaaaa\tb");
        ValidatorError [] errors = command.validate();
        assertEquals(1, errors.length);
    }

    public void testValidatePasswordHasNewlineCharacter() throws Exception {
        command.setLogin("bilbo");
        command.setEmail("bilbo@baggins.com");
        command.setPassword("aaaaa\nb");
        ValidatorError [] errors = command.validate();
        assertEquals(1, errors.length);
    }
}
