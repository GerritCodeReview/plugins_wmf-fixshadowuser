@PLUGIN@ fix
==============================

NAME
----
wmf-fixshadowuser - Change Account ID for username

SYNOPSIS
--------
     POST /accounts/{account-id}/@PLUGIN@~fix

DESCRIPTION
-----------
Correct accountIds in NoteDB

OPTIONS
-------

--correctaccountid
> AccountId

ACCESS
------
Server administrators

EXAMPLES
--------

Have the server say Hello to the user

>     curl -X POST --user foo:bar \
            -H 'Content-Type: application/json' \
            -d '{"correctaccountid": 1000002}' \
            'http://host:port/a/accounts/1000001/@PLUGIN@~fix

> "'username:baz' accountId changed to 1000002"

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
