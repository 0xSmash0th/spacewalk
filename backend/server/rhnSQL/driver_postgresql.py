#!/usr/bin/env python
#
# Copyright (c) 2008 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#
#
# Database driver for PostgreSQL
#

import sys
import re
import pgsql

import sql_base
from server import rhnSQL
from server.rhnSQL import sql_types

from common import log_debug, log_error
from common import UserDictCase
from const import POSTGRESQL

NAMED_PARAM_REGEX = re.compile(":\w+")

POSTGRESQL_TYPE_MAPPING = [
    (sql_types.NUMBER, pgsql.INTEGER),
    (sql_types.STRING, pgsql.STRING),
    (sql_types.BINARY, pgsql.BINARY),
    (sql_types.LONG_BINARY, pgsql.LONG), # TODO: LONG_BINARY?
]

def convert_named_query_params(query):
    """ 
    Convert a query with named parameters (i.e. :id, :name, etc) into one
    that uses $1 .. $n positional parameters instead.

    python-pgsql requires parameters to be in this form, so to keep our 
    existing queries intact we'll convert them when provided to the 
    postgresql driver.

    RETURNS: tuple with:
        - the new query with parameters replaced
        - hash of each named parameter to an ordered list of the positions
          where it was used.
        - number of arguments found and replaced
    """
    log_debug(3, "Converting query for PostgreSQL: %s" % query)
    pattern = NAMED_PARAM_REGEX

    
    # List with index counter and the running param to index hash:
    index_data = [1, {}]
    f = create_replacer_function(index_data)

    new_query = pattern.sub(f, query)
    log_debug(3, "New query: %s" % new_query)
    return (new_query, index_data[1], index_data[0] - 1)

def create_replacer_function(index_data):
    """ 
    Wrapper to allow us to pass extra args to the pattern.sub() replacer
    function. Avoids the use of lambda above.

    Extra args include an index representing which arg we're on ($1, $2, etc)
    and a hash of parameter name to all the positions it's used in.

    index_data = [counter, {"name" => [1, 3], "id" => [2]}]
    """
    def param_replacer(match, index_data=index_data):
        """ 
        Helper function for replacing named query params in a string.

        Intended to be passed to sub, but only indirectly via another lambda 
        function, which must exist to pass us additional arguments as the
        call to sub would only provide us with a match object. (we need the
        positional index of the argument as well)

        index_data is a list with two elements, the first is a counter 
        representing the next argument number to be used. (increments $1 to $2, 
        etc) The second is a hash of parameter name to it's new index number.
        """
        matched_param = match.group()[1:]

        counter = index_data[0] # don't increment this var directly
        param_index = index_data[1]

        # if the index doesn't yet have this parameter, add it and hash to an
        # empty list:
        if not param_index.has_key(matched_param):
            param_index[matched_param] = []
        param_index[matched_param].append(counter)
        index_data[0] = index_data[0] + 1
        return "$%s" % counter
    return param_replacer

class Procedure(sql_base.Procedure):

    def __init__(self, name, proc):
        sql_base.Procedure.__init__(self, name, proc)
        self._type_mapping = POSTGRESQL_TYPE_MAPPING

    def __call__(self, *args):
        """
        Wrap the __call__ method from the parent class to catch Oracle specific
        actions and convert them to something generic.
        """
        retval = None

        # TODO: Replicate Oracle driver's error handling?
        #try:
        retval = sql_base.Procedure.__call__(self, *args)
        #except cx_Oracle.DatabaseError, e:
        #    if not hasattr(e, "args"):
        #        raise sql_base.SQLError(self.name, args)
        #    elif 20000 <= e[0] <= 20999: # error codes we know we raise as schema errors
        #        raise apply(sql_base.SQLSchemaError, tuple(e.args))
        #    raise apply(sql_base.SQLError, tuple(e.args))
        #except cx_Oracle.NotSupportedError, error:
        #    raise apply(sql_base.SQLError, error.args)
        return retval



class Database(sql_base.Database):
    """ Class for PostgreSQL database operations. """

    def __init__(self, host=None, port=None, username=None,
        password=None, database=None):

        self.host = host
        self.port = port

        # pgsql module prefers -1 for an unspecified port:
        if not port:
            self.port = -1

        self.username = username
        self.password = password
        self.database = database

        # Minimum requirements to connect to a PostgreSQL db:
        if not (self.username and self.database):
            raise AttributeError, "PostgreSQL requires at least a user and database name."

        sql_base.Database.__init__(self, host, port, username, password, database)

    def connect(self, reconnect=1):
        try:
            self.dbh = pgsql.connect(self.database, self.username,
                    self.password, self.host, self.port)
        except Exception, e:
            if reconnect:
                # Try one more time:
                return self.connect(reconnect=0)

            # Failed reconnect, time to error out:
            raise apply(sql_base.SQLConnectError,
                [self.database, -1, e.message])

    def is_connected_to(self, backend, host, port, username, password,
            database):
        if not port:
            adjusted_port = -1
        return (backend == POSTGRESQL) and (self.host == host) and \
                (self.port == adjusted_port) and (self.username == username) \
                and (self.password == password) and (self.database == database)

    def check_connection(self):
        try:
            c = self.prepare("select 1")
            c.execute()
        except: # try to reconnect, that one MUST WORK always
            log_error("DATABASE CONNECTION TO '%s' LOST" % self.database,
                      "Exception information: %s" % sys.exc_info()[1])
            self.connect() # only allow one try

    def prepare(self, sql, force=0):
        return Cursor(dbh=self.dbh, sql=sql, force=force)

    def commit(self):
        self.dbh.commit()

    def rollback(self, name=None):
        if name:
            # PostgreSQL doesn't support savepoints, raise exception:
            # TODO: investigate this
            raise SQLError("PostgreSQL unable to rollback to savepoint: %s" % name)
        self.dbh.rollback()

    def procedure(self, name):
        c = self.dbh.cursor()
        # Pass the cursor in so we can close it after execute()
        return Procedure(name, c)



class Cursor(sql_base.Cursor):
    """ PostgreSQL specific wrapper over sql_base.Cursor. """

    def __init__(self, dbh=None, sql=None, force=None):

        sql_base.Cursor.__init__(self, dbh, sql, force)
        self._type_mapping = POSTGRESQL_TYPE_MAPPING

        # Accept Oracle style named query params, but convert for python-pgsql
        # under the hood:
        (self.sql, self.param_indicies, self.param_count) = \
                convert_named_query_params(self.sql)

    def _prepare_sql(self):
        cursor = self.dbh.cursor()
        return cursor

    def _execute_wrapper(self, function, *p, **kw):
        params =  ','.join(["%s: %s" % (str(key), str(value)) for key, value \
                in kw.items()])
        log_debug(5, "Executing SQL: \"%s\" with bind params: {%s}"
                % (self.sql, params))
        if self.sql is None:
            raise rhnException("Cannot execute empty cursor")

        modified_params = self._munge_args(kw)
        try:
            retval = apply(function, p, kw)
        except pgsql.ProgrammingError, e:
            # TODO: Constructor for this exception expects a first arg of db,
            # and yet the Oracle driver passes it an errno? Suspect it's not
            # even used.
            raise rhnSQL.SQLStatementPrepareError(0, e.message, self.sql)
        #except Exception, e:
        #    log_error("PostgreSQL exception", e)
        #    raise e
            #ret = self._get_oracle_error_info(e)
            #if isinstance(ret, StringType):
            #    raise sql_base.SQLError(self.sql, p, kw, ret)
            #(errno, errmsg) = ret[:2]
            #if 900 <= errno <= 999:
            #    # Per Oracle's documentation, SQL parsing error
            #    args = (errno, errmsg, self.sql)
            #    raise apply(sql_base.SQLStatementPrepareError, args)
            #if errno == 1475: # statement needs to be reparsed; force a prepare again
            #    if self.reparsed: # useless, tried that already. give up
            #        log_error("Reparsing cursor did not fix it", self.sql)
            #        args = ("Reparsing tried and still got this",) + tuple(ret)
            #        raise apply(sql_base.SQLError, args)
            #    self._real_cursor = self.dbh.prepare(self.sql)
            #    self.reparsed = 1
            #    apply(self._execute_wrapper, (function, ) + p, kw)
            #elif 20000 <= errno <= 20999: # error codes we know we raise as schema errors
            #    raise apply(sql_base.SQLSchemaError, ret)
            #raise apply(sql_base.SQLError, ret)
        #else:
        #    self.reparsed = 0 # reset the reparsed counter
        # Munge back the values
        self._unmunge_args(kw, modified_params)
        return retval

    def _execute_(self, args, kwargs):
        """
        PostgreSQL specific execution of the query.
        """
        positional_args = self._get_positional_args(kwargs)

        self._real_cursor.execute(self.sql, positional_args)
        self.description = self._real_cursor.description
        return self._real_cursor.rowcount

    def _get_positional_args(self, kwargs):
        """
        Return a list of positional args based on the incoming keyword args.
        (and the information we gathered when preparing the query)
        """
        params = UserDictCase(kwargs)
        #    TODO: is this needed? params[k] = adjust_type(_p[k])

        # Assemble position list of arguments for python-pgsql:
        positional_args = []
        for i in range(self.param_count):
            positional_args.append(None)

        for key in self.param_indicies.keys():
            if not params.has_key(key):
                raise sql_base.SQLError(1008, 'Not all variables bound', key)

            positions_used = self.param_indicies[key]
            for p in positions_used:
                positional_args[p - 1] = params[key]

        return positional_args

    def _executemany(self, *args, **kwargs):
        if not kwargs:
            return 0

        params = UserDictCase(kwargs)

        # First break all the incoming keyword arg lists into individual
        # hashes:
        all_kwargs = []
        for key in params.keys():
            if len(all_kwargs) < len(params[key]):
                for i in range(len(params[key])):
                    all_kwargs.append({})

            i = 0
            for val in params[key]:
                all_kwargs[i][key] = val
                i = i + 1

        # Assemble final array of all params for each execution of the query:
        final_args = []
        for params in all_kwargs:
            positional_args = self._get_positional_args(params)
            final_args.append(positional_args)

        self._real_cursor.executemany(self.sql, final_args)
        self.description = self._real_cursor.description
        rowcount = self._real_cursor.rowcount
        return rowcount



